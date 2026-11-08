package com.data.Detonate.Entity;

import java.time.Instant;

import com.data.Detonate.Enums.ScanStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "scans")

public class Scan {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY) 
	private int id;
	
	@Column(nullable = false, length = 20)
	private String ecosystem;
	
	@Column(nullable = false, length = 214)
	private String packageName;
	
	@Column(nullable = false, length = 100)
	private String version;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private ScanStatus status;
	
	@Column(nullable = false, updatable = false)
	private Instant createdAt;
	
	@Lob
	private String verdictJson;
	
	
	public Scan(){
		
	}


	public Scan(String ecosystem, String packageName, String version) {
		
		this.ecosystem = ecosystem;
		this.packageName = packageName;
		this.version = version;
		this.status = ScanStatus.QUEUED;
		this.createdAt = Instant.now();
		
	}


	public int getId() {
		return id;
	}


	public void setId(int id) {
		this.id = id;
	}


	public String getEcosystem() {
		return ecosystem;
	}


	public void setEcosystem(String ecosystem) {
		this.ecosystem = ecosystem;
	}


	public String getPackageName() {
		return packageName;
	}


	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}


	public String getVersion() {
		return version;
	}


	public void setVersion(String version) {
		this.version = version;
	}


	public ScanStatus getStatus() {
		return status;
	}


	public void setStatus(ScanStatus status) {
		this.status = status;
	}


	public Instant getCreatedAt() {
		return createdAt;
	}


	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}


	public String getVerdictJson() {
		return verdictJson;
	}


	public void setVerdictJson(String verdictJson) {
		this.verdictJson = verdictJson;
	}
	
	
	
	
	
	
	
	
}
