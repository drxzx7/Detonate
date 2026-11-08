package com.data.Detonate.Controller;

import org.springframework.web.bind.annotation.GetMapping;


import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity; 


import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.data.Detonate.Service.ScanStreamService;



import java.util.List;

import com.data.Detonate.DtoResponse.ScanEventResponse;
import com.data.Detonate.Service.ScanEventService;
import com.data.Detonate.DtoResponse.VerdictResponse;
import com.data.Detonate.Verdict.VerdictService;

import com.data.Detonate.DtoRequests.CreateScanRequest;
import com.data.Detonate.DtoResponse.CreateScanResponse;
import com.data.Detonate.DtoResponse.ScanResponse;
import com.data.Detonate.Entity.Scan;
import com.data.Detonate.Service.ScanService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/scan")

public class ScanController {
	
	private final ScanService scanService;
	private final ScanEventService scanEventService;
	private final VerdictService verdictService; 
	private final ScanStreamService  scanStreamService;
	
	public ScanController(ScanService scanService, ScanEventService scanEventService, VerdictService verdictService, ScanStreamService scanStreamService) {
		this.scanService = scanService;
		this.scanEventService = scanEventService;
		this.verdictService = verdictService;
		this.scanStreamService = scanStreamService;
	}
	
	@PostMapping("/createscan")
	@ResponseStatus(HttpStatus.CREATED)
	public CreateScanResponse createScan(@Valid @RequestBody CreateScanRequest request) {
		
		Scan s = scanService.createScan(request);
		return new CreateScanResponse(s.getId(), s.getStatus().name());
		
	}
	
	@GetMapping("/getscan/{id}")
	public ScanResponse getScan(@PathVariable int id){
		return scanService.getScan(id);
	}
	
	
	@GetMapping("/{id}/events")
	public List<ScanEventResponse> getEvents(@PathVariable("id") int id){
		scanService.getScan(id);
		return scanEventService.getEvents(id);
	}
	
	@GetMapping("/{id}/verdict")
	public ResponseEntity<VerdictResponse> getVerdict(@PathVariable("id") int id){
		return verdictService.getVerdict(id)
							 .map(verdict -> ResponseEntity.ok(verdict))
							 .orElseGet(() -> ResponseEntity.noContent().build());
	}
	
	@GetMapping(
	        value = "/{id}/stream",
	        produces = MediaType.TEXT_EVENT_STREAM_VALUE
	)
	public ResponseEntity<SseEmitter> streamScan(
	        @PathVariable("id") int id,

	        @RequestHeader(
	                value = "Last-Event-ID",
	                required = false
	        ) String lastEventId,

	        @RequestParam(
	                value = "afterSeq",
	                defaultValue = "0"
	        ) int afterSeq
	) {
	    if (afterSeq < 0) {
	        throw new ResponseStatusException(
	                HttpStatus.BAD_REQUEST,
	                "afterSeq must be zero or greater"
	        );
	    }

	    int cursor = afterSeq;

	    if (lastEventId != null && !lastEventId.isBlank()) {
	        try {
	            int headerCursor = Integer.parseInt(lastEventId);

	            if (headerCursor < 0) {
	                throw new NumberFormatException();
	            }

	            cursor = Math.max(cursor, headerCursor);

	        } catch (NumberFormatException exception) {
	            throw new ResponseStatusException(
	                    HttpStatus.BAD_REQUEST,
	                    "Last-Event-ID must be a non-negative integer"
	            );
	        }
	    }

	    SseEmitter emitter = scanStreamService.open(id, cursor);

	    return ResponseEntity.ok()
	            .header("Cache-Control", "no-cache")
	            .header("X-Accel-Buffering", "no")
	            .contentType(MediaType.TEXT_EVENT_STREAM)
	            .body(emitter);
	}
	
	
	
}
