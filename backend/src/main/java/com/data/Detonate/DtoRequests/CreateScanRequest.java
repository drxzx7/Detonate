package com.data.Detonate.DtoRequests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateScanRequest(
		
		@NotBlank String ecosystem,
		@NotBlank @Size(max = 214) String packageName,
		@NotBlank @Size(max = 100) String version
	
) {
	
	
}	
