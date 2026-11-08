package com.data.Detonate.DtoResponse;

import java.time.Instant;

public record ScanResponse(
		
		int scanId,
		String ecosystem,
		String packageName,
		String version,
		String status,
		Instant createdAt
		
){
	
	
}
