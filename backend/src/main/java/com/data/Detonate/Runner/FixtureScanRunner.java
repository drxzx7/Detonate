package com.data.Detonate.Runner;

import java.time.Instant;
import java.util.Map;
import java.util.function.Consumer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.data.Detonate.Enums.EventKind;
import com.data.Detonate.Enums.EventSeverity;

@Component
@ConditionalOnProperty(
			
		name = "detonate.runner.mode",
		havingValue = "fixture"
		
		)
public class FixtureScanRunner implements ScanRunner {
	
	
	
	@Override
	public void run(int id, String ecosystem, String packageName, 
	String version, Consumer<RunnerEvent> emit) throws Exception {
		int seq = 1;
		
		
		emit.accept(event(
							seq++,
							EventKind.log,
							EventSeverity.info,
							Map.of(
									
									"stream","stdout",
									"line","FIXTURE MODE : simulated events; no code executed"
									
									
						    )
							 			 
		));
		
		
		emit.accept(event(
						
							seq++,
							EventKind.lifecycle,
							EventSeverity.info,
							Map.of(
									"stage", "created",
									"detail", "Simulated sandbox creation"
									
							)
				
        ));
		
		
		Thread.sleep(1000);
		
		
		emit.accept(event(
				
							seq++,
							EventKind.lifecycle,
							EventSeverity.info,
							Map.of(	
									"stage" , "installing",
									"detail" , "Simulated package installation"
									
							)
							
		 ));
		
		Thread.sleep(1000);
		
		
		emit.accept(event(
		        seq++,
		        EventKind.lifecycle,
		        EventSeverity.info,
		        Map.of(
		                "stage", "running",
		                "detail", "Simulated package import; no code executed"
		        )
		));

		Thread.sleep(1000);

		emit.accept(event(
		        seq++,
		        EventKind.exit,
		        EventSeverity.info,
		        Map.of(
		                "phase", "run",
		                "code", 0,
		                "reason", "ok"
		        )
		));
		
		
		if("colorjs-pretty".equals(packageName)){
			emit.accept(event(
					
					seq++,
					EventKind.file,
					EventSeverity.critical,
					Map.of(
							
							"path", "/home/scan/.aws/credentials",
							"op", "read",
							"honeytoken", "aws_secret"
											
								
				    )	
					
			));
			
			Thread.sleep(1000);
			
			
			emit.accept(event(
							  
					seq++,
					EventKind.net,
					EventSeverity.critical,
					Map.of(
							  "method", "POST",
							  "host", "collect.evil.example",
							  "path", "/collect",
							  "allowed", false,
							  "honeytoken", "aws_secret",
							  "bytes" , 64
							
					)	
			));
			
			
		}
		
		
		emit.accept(event(
					
							seq++,
							EventKind.exit,
							EventSeverity.info,
							Map.of(
									
									
									"phase", "install",
									"code", 0,
									"reason", "ok"
									
									
									
							)
							
				
		));
		
		
		emit.accept(event(
							
					seq++,
					EventKind.lifecycle,
					EventSeverity.info,
					Map.of(
							
							"stage", "destroyed",
							"detail", "Simulated cleanup; no actual sandbox existed"
							
					)
		));
		
		
	}
	
	
	private RunnerEvent event(int seq, EventKind kind, EventSeverity severity, Map<String, Object> data){
	
		return new RunnerEvent(
								seq,
								Instant.now(),
								kind,
								severity,
								data	
		);
	
	}
}
