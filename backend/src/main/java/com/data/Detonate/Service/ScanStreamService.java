package com.data.Detonate.Service;

import com.data.Detonate.DtoResponse.ScanEventResponse;
import com.data.Detonate.DtoResponse.VerdictResponse;
import com.data.Detonate.Verdict.VerdictService;

import jakarta.annotation.PreDestroy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class ScanStreamService {

    private static final Logger log =
            LoggerFactory.getLogger(ScanStreamService.class);

    private static final long STREAM_TIMEOUT_MS = 300_000L;
    private static final int MAX_STREAMS = 20;

    private final ScanService scanService;
    private final ScanEventService eventService;
    private final VerdictService verdictService;

    private final ScheduledThreadPoolExecutor scheduler =
            new ScheduledThreadPoolExecutor(4);

    private final Set<StreamSession> sessions =
            ConcurrentHashMap.newKeySet();

    public ScanStreamService(
            ScanService scanService,
            ScanEventService eventService,
            VerdictService verdictService
    ) {
        this.scanService = scanService;
        this.eventService = eventService;
        this.verdictService = verdictService;

        scheduler.setRemoveOnCancelPolicy(true);
    }

    public synchronized SseEmitter open(
            int id,
            int afterSeq
    ) {
        // Validate before starting the streaming HTTP response.
        scanService.getScan(id);

        if (sessions.size() >= MAX_STREAMS) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Too many active streams. Try again shortly."
            );
        }

        SseEmitter emitter = new SseEmitter(STREAM_TIMEOUT_MS);

        StreamSession session =
                new StreamSession(id, afterSeq, emitter);

        sessions.add(session);

        emitter.onCompletion(session::cleanup);

        emitter.onTimeout(() -> {
            session.cleanup();
            emitter.complete();
        });

        emitter.onError(error -> session.cleanup());

        ScheduledFuture<?> task = scheduler.scheduleWithFixedDelay(
                session,
                0,
                500,
                TimeUnit.MILLISECONDS
        );

        session.attach(task);

        return emitter;
    }

    private class StreamSession implements Runnable {

        private final int id;
        private final SseEmitter emitter;
        private final AtomicBoolean closed = new AtomicBoolean(false);

        private volatile ScheduledFuture<?> task;

        private int lastSeq;
        private long lastHeartbeat = System.nanoTime();

        private StreamSession(
                int id,
                int afterSeq,
                SseEmitter emitter
        ) {
        	this.id = id;
            this.lastSeq = afterSeq;
            this.emitter = emitter;
        }

        private void attach(ScheduledFuture<?> task) {
            this.task = task;

            if (closed.get()) {
                task.cancel(false);
            }
        }

        @Override
        public void run() {
            if (closed.get()) {
                return;
            }

            try {
                /*
                 * Read status first. If it is terminal, the worker has
                 * already committed its earlier event writes.
                 */
                String status = scanService.getScan(id).status();

                for (ScanEventResponse event :
                        eventService.getEvents(id)) {

                    if (closed.get()) {
                        return;
                    }

                    if (event.seq() <= lastSeq) {
                        continue;
                    }

                    emitter.send(
                            SseEmitter.event()
                                    .name("scan-event")
                                    .id(Integer.toString(event.seq()))
                                    .reconnectTime(1500L)
                                    .data(event, MediaType.APPLICATION_JSON)
                    );

                    lastSeq = event.seq();
                }

                if ("COMPLETED".equals(status)) {
                    Optional<VerdictResponse> verdict =
                            verdictService.getVerdict(id);

                    if (verdict.isEmpty()) {
                        throw new IllegalStateException(
                                "Completed scan has no saved verdict"
                        );
                    }

                    emitter.send(
                            SseEmitter.event()
                                    .name("verdict")
                                    .data(
                                            verdict.get(),
                                            MediaType.APPLICATION_JSON
                                    )
                    );

                    finish();
                    return;
                }

                if ("FAILED".equals(status)) {
                    emitter.send(
                            SseEmitter.event()
                                    .name("scan-failed")
                                    .data(
                                            Map.of(
                                                    "scanId", id,
                                                    "status", "FAILED",
                                                    "message",
                                                    "Scan processing failed. "
                                                            + "Review server logs."
                                            ),
                                            MediaType.APPLICATION_JSON
                                    )
                    );

                    finish();
                    return;
                }

                long now = System.nanoTime();

                if (now - lastHeartbeat
                        >= TimeUnit.SECONDS.toNanos(10)) {

                    emitter.send(
                            SseEmitter.event()
                                    .comment("keepalive")
                    );

                    lastHeartbeat = now;
                }

            } catch (IOException exception) {
                // The servlet container handles the failed response.
                cleanup();

            } catch (IllegalStateException exception) {
                if (!closed.get()) {
                    log.warn(
                            "Stream stopped for scan {}: {}",
                            id,
                            exception.getMessage()
                    );

                    cleanup();
                    emitter.completeWithError(exception);
                }

            } catch (Exception exception) {
                log.error(
                        "Streaming failed for scan {}",
                        id,
                        exception
                );

                cleanup();
                emitter.completeWithError(exception);
            }
        }

        private void finish() {
            cleanup();
            emitter.complete();
        }

        private void cleanup() {
            if (closed.compareAndSet(false, true)) {
                sessions.remove(this);

                ScheduledFuture<?> currentTask = task;

                if (currentTask != null) {
                    currentTask.cancel(false);
                }
            }
        }
    }

    @PreDestroy
    public void shutdown() {
        for (StreamSession session : Set.copyOf(sessions)) {
            session.finish();
        }

        scheduler.shutdownNow();
    }
}