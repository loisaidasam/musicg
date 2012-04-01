package com.musicg.math.function;

public class ZeroCrossingRate{
	
	private double[] signals;
	private double lengthInSecond;
	
	public ZeroCrossingRate(){
		lengthInSecond=1;
	}
	
	public ZeroCrossingRate(double[] signals){
		setSignals(signals,1);
	}
	
	public ZeroCrossingRate(double[] signals, double lengthInSecond){
		setSignals(signals,1);
	}
	
	public void setSignals(double[] signals, double lengthInSecond){		
		this.signals=signals;
		this.lengthInSecond=lengthInSecond;
	}
	
	public double evaluate(){
		int numZC=0;
		int size=signals.length;
		
		for (int i=0; i<size-1; i++){
			if((signals[i]>=0 && signals[i+1]<0) || (signals[i]<0 && signals[i+1]>=0)){
				numZC++;
			}
		}			

		return numZC/lengthInSecond;
	}
}