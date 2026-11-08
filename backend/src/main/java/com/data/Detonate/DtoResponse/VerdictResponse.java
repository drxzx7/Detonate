package com.data.Detonate.DtoResponse;

import java.util.List;

import com.data.Detonate.Enums.RiskLevel;

public record VerdictResponse(
		
		int riskScore,
		RiskLevel level,
		List<String> categories,
		String summary,
		List<ClaimResponse> claims,
		String evidenceSource,
		String assessmentScope
		
		
){
	
}
