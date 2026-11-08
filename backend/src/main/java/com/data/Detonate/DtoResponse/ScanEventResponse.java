package com.data.Detonate.DtoResponse;

import java.time.Instant;

import com.data.Detonate.Enums.EventKind;
import com.data.Detonate.Enums.EventSeverity;

import tools.jackson.databind.JsonNode;

public record ScanEventResponse(
		
		int seq,
		Instant ts,
		EventKind kind,
		EventSeverity severity,
		JsonNode data
		
		
		
) {
	
}
