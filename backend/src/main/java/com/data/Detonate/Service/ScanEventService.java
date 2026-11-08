package com.data.Detonate.Service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.data.Detonate.DtoResponse.ScanEventResponse;
import com.data.Detonate.Entity.Scan;
import com.data.Detonate.Entity.ScanEvent;
import com.data.Detonate.Exception.ScanNotFoundException;
import com.data.Detonate.Repository.ScanEventRepository;
import com.data.Detonate.Repository.ScanRepository;
import com.data.Detonate.Runner.RunnerEvent;

import tools.jackson.databind.json.JsonMapper;

@Service
public class ScanEventService {
	private final ScanRepository scanRepo;
	private final ScanEventRepository eventRepo;
	private final JsonMapper jsonMapper;
	
	public ScanEventService(ScanRepository scanRepo, ScanEventRepository eventRepo, JsonMapper jsonMapper) {
		this.scanRepo = scanRepo;
		this.eventRepo = eventRepo;
		this.jsonMapper = jsonMapper;
	}
	
	
	@Transactional
	public void saveEvent(int id, RunnerEvent event) {
		if(event.seq() < 1 || event.ts() == null || event.kind() == null || event.severity() == null || event.data() == null) {
			
			throw new IllegalArgumentException(" Invalid Runner Event ");
						
		}
		
		Scan s = scanRepo.findById(id).orElseThrow(() -> new ScanNotFoundException(id));
		
		
		String dataJson = jsonMapper.writeValueAsString(event.data());
		
		eventRepo.save(new ScanEvent(s, event, dataJson));
		
		
	}
	
	@Transactional(readOnly = true)
	public List<ScanEventResponse> getEvents(int id){
		
		return eventRepo.findByScan_IdOrderBySeqAsc(id)
						.stream()
						.map(event -> new ScanEventResponse(
								
								
								event.getSeq(),
								event.getTs(),
								event.getKind(),
								event.getSeverity(),
								jsonMapper.readTree(event.getDataJson())
								
			
					
						))
						.toList();
		
	}
	

}
