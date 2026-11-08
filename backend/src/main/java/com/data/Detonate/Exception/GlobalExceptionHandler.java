package com.data.Detonate.Exception;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice

public class GlobalExceptionHandler {
	@ExceptionHandler(ScanNotFoundException.class)
	public ResponseEntity<Map<String, String>> handleScanNotFound(ScanNotFoundException exc){
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
							 .body(Map.of(
									 
									 "error", "SCAN_NOT_FOUND",
									 "message", exc.getMessage()
									 
									 ));
		
	}
}
