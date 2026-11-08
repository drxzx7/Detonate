package com.data.Detonate.Exception;


public class ScanNotFoundException extends RuntimeException {
		
	public ScanNotFoundException(int scanId){
		super(" Scan Not Found with Id : " + scanId);
	}



}
