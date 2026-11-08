package com.data.Detonate.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.data.Detonate.Entity.Scan;

@Repository
public interface ScanRepository extends JpaRepository<Scan, Integer> {

}
