package com.data.Detonate.Service;

import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import com.data.Detonate.Queue.ScanSubmitted;

import org.springframework.transaction.annotation.Transactional;

import com.data.Detonate.DtoRequests.CreateScanRequest;
import com.data.Detonate.DtoResponse.ScanResponse;
import com.data.Detonate.Entity.Scan;
import com.data.Detonate.Exception.ScanNotFoundException;
import com.data.Detonate.Repository.ScanRepository;

@Service
public class ScanService {
	private final ScanRepository scanRepo;
	private final ApplicationEventPublisher eventPublisher;
	public ScanService(ScanRepository scanRepo, ApplicationEventPublisher eventPublisher){
		this.scanRepo = scanRepo;
		this.eventPublisher = eventPublisher;
	}
	
	@Transactional
	public Scan createScan(CreateScanRequest request) {
		Scan s = new Scan(
				
				request.ecosystem(),
				request.packageName(),
				request.version()
				
		);
		
		Scan saved = scanRepo.save(s);
		
		eventPublisher.publishEvent( new ScanSubmitted(
				
				saved.getId(),
				saved.getEcosystem(),
				saved.getPackageName(),
				saved.getVersion()
				
		));
		
		return saved;
	}
	
	
	@Transactional(readOnly = true)
	public ScanResponse getScan(int id){
		Scan s1 = scanRepo.findById(id).orElseThrow(() -> new ScanNotFoundException(id));
		return new ScanResponse(
				
				s1.getId(),
				s1.getEcosystem(),
				s1.getPackageName(),
				s1.getVersion(),
				s1.getStatus().name(),
				s1.getCreatedAt()
				

				);
		
	}
}
