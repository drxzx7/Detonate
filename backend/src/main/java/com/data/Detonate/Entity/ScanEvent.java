package com.data.Detonate.Entity;

import java.time.Instant;

import com.data.Detonate.Enums.EventKind;
import com.data.Detonate.Enums.EventSeverity;
import com.data.Detonate.Runner.RunnerEvent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(
		name = "scan_event",
		uniqueConstraints = @UniqueConstraint(
				
					name = "uk_scan_even_sequence",
					columnNames = {"scan_id","event_Seq"}
				
	   )
)
public class ScanEvent {
	
	
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "scan_id", nullable = false)
	private Scan scan;
	
	@Column(name = "event_seq", nullable = false)
	private int seq;
	
	@Column(nullable = false)
	private Instant ts;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EventKind kind;
	
	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private EventSeverity severity;
	
	@Column(name = "data-json", nullable = false, columnDefinition = "LONGTEXT")
	private String dataJson;
	
	public ScanEvent() {
		
	}

	public ScanEvent(Scan scan, RunnerEvent event, String dataJson) {

		this.scan = scan;
		this.seq = event.seq();
		this.ts = event.ts();
		this.kind = event.kind();
		this.severity = event.severity();
		this.dataJson = dataJson;
		
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Scan getScan() {
		return scan;
	}

	public void setScan(Scan scan) {
		this.scan = scan;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}

	public Instant getTs() {
		return ts;
	}

	public void setTs(Instant ts) {
		this.ts = ts;
	}

	public EventKind getKind() {
		return kind;
	}

	public void setKind(EventKind kind) {
		this.kind = kind;
	}

	public EventSeverity getSeverity() {
		return severity;
	}

	public void setSeverity(EventSeverity severity) {
		this.severity = severity;
	}

	public String getDataJson() {
		return dataJson;
	}

	public void setDataJson(String dataJson) {
		this.dataJson = dataJson;
	}
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
}
