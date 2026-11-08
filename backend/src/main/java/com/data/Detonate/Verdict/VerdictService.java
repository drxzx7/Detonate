package com.data.Detonate.Verdict;

import com.data.Detonate.DtoResponse.ScanEventResponse;
import com.data.Detonate.DtoResponse.VerdictResponse;
import com.data.Detonate.Entity.Scan;
import com.data.Detonate.Enums.ScanStatus;
import com.data.Detonate.Exception.ScanNotFoundException;
import com.data.Detonate.Repository.ScanRepository;
import com.data.Detonate.Service.ScanEventService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;

@Service
public class VerdictService {

    private final ScanRepository scanRepository;
    private final ScanEventService eventService;
    private final RiskRuleEngine ruleEngine;
    private final JsonMapper jsonMapper;

    public VerdictService(
            ScanRepository scanRepository,
            ScanEventService eventService,
            RiskRuleEngine ruleEngine,
            JsonMapper jsonMapper
    ) {
        this.scanRepository = scanRepository;
        this.eventService = eventService;
        this.ruleEngine = ruleEngine;
        this.jsonMapper = jsonMapper;
    }

    @Transactional
    public VerdictResponse generateAndSave(int id) {

        Scan scan = scanRepository.findById(id)
                .orElseThrow(() -> new ScanNotFoundException(id));

        List<ScanEventResponse> events =
                eventService.getEvents(id);

        VerdictResponse verdict = ruleEngine.evaluate(events);

        scan.setVerdictJson(
                jsonMapper.writeValueAsString(verdict)
        );

        scan.setStatus(ScanStatus.COMPLETED);

        return verdict;
    }

    @Transactional(readOnly = true)
    public Optional<VerdictResponse> getVerdict(int id) {

        Scan scan = scanRepository.findById(id)
                .orElseThrow(() -> new ScanNotFoundException(id));

        String json = scan.getVerdictJson();

        if (json == null || json.isBlank()) {
            return Optional.empty();
        }

        return Optional.of(
                jsonMapper.readValue(json, VerdictResponse.class)
        );
    }
}