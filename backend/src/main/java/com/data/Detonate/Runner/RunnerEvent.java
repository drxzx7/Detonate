package com.data.Detonate.Runner;

import java.time.Instant;
import java.util.Map;

import com.data.Detonate.Enums.EventKind;
import com.data.Detonate.Enums.EventSeverity;

public record RunnerEvent(
		
		int seq,
		Instant ts,
		EventKind kind,
		EventSeverity severity,
		Map<String, Object> data
		
		
) {
	
}
