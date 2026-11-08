package com.data.Detonate.DtoResponse;

import java.util.List;

import com.data.Detonate.Enums.EventSeverity;

public record ClaimResponse(
		
		String text,
		List<Integer> evidence,
		EventSeverity severity
		
		

) {
	
}
