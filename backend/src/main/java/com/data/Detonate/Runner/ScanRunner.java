package com.data.Detonate.Runner;

import java.util.function.Consumer;

public interface ScanRunner {
	
	void run(
				int scanId,
				String ecosystem,
				String packageName,
				String version,
				Consumer<RunnerEvent> emit
			
			) throws Exception;
	
}
