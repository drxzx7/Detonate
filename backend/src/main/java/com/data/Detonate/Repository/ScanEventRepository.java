package com.data.Detonate.Repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.data.Detonate.Entity.ScanEvent;

@Repository
public interface ScanEventRepository extends JpaRepository<ScanEvent, Integer>{
	
	List<ScanEvent> findByScan_IdOrderBySeqAsc(int id);
	
}
