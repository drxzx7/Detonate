package com.data.Detonate.Service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.data.Detonate.Entity.Scan;
import com.data.Detonate.Enums.ScanStatus;
import com.data.Detonate.Exception.ScanNotFoundException;
import com.data.Detonate.Repository.ScanRepository;

@Service

public class ScanExecutionService {
	
	private final ScanRepository scanRepo;
	
	public ScanExecutionService(ScanRepository scanRepo){
		this.scanRepo = scanRepo;
	}
	
	@Transactional
	public void updateStatus(int id, ScanStatus status) {
		Scan s = scanRepo.findById(id).orElseThrow(() -> new ScanNotFoundException(id));
		s.setStatus(status);
	}
	
}
