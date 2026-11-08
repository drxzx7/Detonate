package com.data.Detonate.Verdict;

import com.data.Detonate.DtoResponse.ClaimResponse;
import com.data.Detonate.DtoResponse.ScanEventResponse;
import com.data.Detonate.DtoResponse.VerdictResponse;
import com.data.Detonate.Enums.EventKind;
import com.data.Detonate.Enums.EventSeverity;
import com.data.Detonate.Enums.RiskLevel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class RiskRuleEngine {

    private final JsonMapper jsonMapper;
    private final String runnerMode;

    public RiskRuleEngine(
            JsonMapper jsonMapper,
            @Value("${detonate.runner.mode}") String runnerMode
    ) {
        this.jsonMapper = jsonMapper;
        this.runnerMode = runnerMode;
    }

    public VerdictResponse evaluate(List<ScanEventResponse> events) {

        if (events.isEmpty()) {
            throw new IllegalStateException(
                    "Cannot assess a scan with no recorded events"
            );
        }

        int minimumScore = 0;

        boolean tokenInNetwork = false;
        boolean blockedEgress = false;
        boolean executionFailure = false;
        boolean destroyed = false;

        Set<String> successfulPhases = new LinkedHashSet<>();
        Set<String> categories = new LinkedHashSet<>();
        List<ClaimResponse> claims = new ArrayList<>();

        for (ScanEventResponse event : events) {

            Map<?, ?> data = jsonMapper.convertValue(
                    event.data(),
                    Map.class
            );

            Object token = data.get("honeytoken");
            boolean hasHoneytoken =
                    token instanceof String value && !value.isBlank();

            if (event.kind() == EventKind.file
                    && "read".equals(data.get("op"))
                    && hasHoneytoken) {

                minimumScore = Math.max(minimumScore, 60);
                categories.add("credential_access");

                claims.add(new ClaimResponse(
                        "A file associated with a planted honeytoken was read.",
                        List.of(event.seq()),
                        EventSeverity.critical
                ));
            }

            if (event.kind() == EventKind.net && hasHoneytoken) {

                tokenInNetwork = true;
                minimumScore = Math.max(minimumScore, 90);
                categories.add("exfiltration");

                String text = Boolean.FALSE.equals(data.get("allowed"))
                        ? "A network request contained a planted honeytoken; "
                            + "the request was blocked."
                        : "A network event recorded a planted honeytoken; "
                            + "delivery to the destination is not established.";

                claims.add(new ClaimResponse(
                        text,
                        List.of(event.seq()),
                        EventSeverity.critical
                ));
            }

            if (event.kind() == EventKind.net
                    && Boolean.FALSE.equals(data.get("allowed"))) {

                blockedEgress = true;
                categories.add("blocked_egress");

                if (!hasHoneytoken) {
                    claims.add(new ClaimResponse(
                            "A network request or connection was blocked.",
                            List.of(event.seq()),
                            EventSeverity.warn
                    ));
                }
            }

            if (event.kind() == EventKind.exit) {

                Object reason = data.get("reason");
                Object code = data.get("code");

                boolean successfulExit =
                        "ok".equals(reason)
                        && code instanceof Number number
                        && number.intValue() == 0;

                if (successfulExit) {
                    Object phase = data.get("phase");

                    if (phase instanceof String value) {
                        successfulPhases.add(value);
                    }
                } else {
                    executionFailure = true;
                    categories.add("execution_failure");

                    claims.add(new ClaimResponse(
                            "An execution phase did not report a successful exit.",
                            List.of(event.seq()),
                            EventSeverity.warn
                    ));
                }
            }

            if (event.kind() == EventKind.lifecycle
                    && "destroyed".equals(data.get("stage"))) {
                destroyed = true;
            }
        }

        boolean incomplete =
                !destroyed
                || !successfulPhases.contains("install")
                || !successfulPhases.contains("run");

        if (incomplete) {
            categories.add("incomplete_observation");
        }

        int score = minimumScore;

        if (blockedEgress) {
            score += 20;
        }

        if (executionFailure) {
            score += 10;
        }

        if (incomplete) {
            score = Math.max(score, 10);
        }

        score = Math.min(score, 100);

        RiskLevel level;

        if (tokenInNetwork) {
            level = RiskLevel.malicious;
        } else if (score > 0) {
            level = RiskLevel.suspicious;
        } else {
            level = RiskLevel.clean;
        }

        String summary;

        if (level == RiskLevel.malicious) {
            summary = "Recorded network evidence contains a planted honeytoken. "
                    + "Review the cited events for the observed action.";
        } else if (incomplete || executionFailure) {
            summary = "Observation was incomplete or execution failed. "
                    + "This result must not be treated as evidence of safety.";
        } else if (level == RiskLevel.suspicious) {
            summary = "The recorded events contain behaviour requiring review.";
        } else {
            summary = "No configured rule flagged the recorded execution. "
                    + "This is not a guarantee that the package is safe.";
        }

        if ("fixture".equals(runnerMode)) {
            summary = "SIMULATED FIXTURE RESULT. " + summary;
        }

        return new VerdictResponse(
                score,
                level,
                List.copyOf(categories),
                summary,
                List.copyOf(claims),
                runnerMode,
                "Recorded install/run phases only; "
                        + "sustained CPU analysis is not implemented"
        );
    }
}