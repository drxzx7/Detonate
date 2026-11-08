package com.data.Detonate.Queue;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

import com.data.Detonate.Enums.ScanStatus;
import com.data.Detonate.Runner.ScanRunner;
import com.data.Detonate.Service.ScanEventService;
import com.data.Detonate.Service.ScanExecutionService;
import com.data.Detonate.Verdict.VerdictService;


import jakarta.annotation.PreDestroy;

@Component
public class ScanQueueWorker {
	
		private static final Logger log = LoggerFactory.getLogger(ScanQueueWorker.class);
		
		private final ScanRunner runner;
		private final ScanEventService eventService;
		private final ScanExecutionService executionService;
		private final VerdictService verdictService;
	
		private final ThreadPoolExecutor executor = new ThreadPoolExecutor(
				
				1,
				1,
				0L,
				TimeUnit.MILLISECONDS,
				new ArrayBlockingQueue<>(20),
				new ThreadPoolExecutor.AbortPolicy()
					
		);
		
		
		public ScanQueueWorker(ScanRunner runner, ScanEventService eventService, ScanExecutionService executionService, VerdictService verdictService) {
			
			this.runner = runner;
			this.eventService = eventService;
			this.executionService = executionService;
			this.verdictService = verdictService;
			
		}
		
		@TransactionalEventListener
		public void onScanSubmitted(ScanSubmitted submitted) {
			
			try {
				executor.execute(() -> execute(submitted));
			}catch(RejectedExecutionException exception) {
				log.error(
							"Scan {} not queued",
							submitted.id(),
							exception	
						);
			
			}
			
		}
		
		
		private void execute(ScanSubmitted submitted) {
			try {
				executionService.updateStatus(submitted.id(), ScanStatus.RUNNING);
				
				runner.run(
						
						submitted.id(),
						submitted.ecosystem(),
						submitted.packageName(),
						submitted.version(),
						event -> eventService.saveEvent(submitted.id(), event)
							
				);
				
				
				
				executionService.updateStatus(
						
						submitted.id(),
						ScanStatus.ANALYZING
						
				);
				
				verdictService.generateAndSave(submitted.id());
				
			}catch(Exception exception) {
				if(exception instanceof InterruptedException) {
					Thread.currentThread().interrupt();
				}
				
				log.error(
							"Scan {} failed",
							submitted.id(),
							exception
						
				);
			}
			
			
			try {
				executionService.updateStatus(
						submitted.id(),
						ScanStatus.FAILED
						
				);
			}catch(Exception statusException){
				log.error(
						
						"Could not mark scan {} as failed",
						submitted.id(),
						statusException
						
				);
			}
		}
		
		@PreDestroy
		public void shutDown() {
			executor.shutdown();
		}
		
}
