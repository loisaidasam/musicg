package com.musicg.sound.pitch;

import java.util.Arrays;

public class PitchHandler{
	
	public double getToneChanged(double f1, double f2){		
		return Math.log(f1/f2)/Math.log(2)*12;		
	}
	
	public double getHarmonicProbability(double[] frequencies){
		
		int harmonicCount=0;
		int count=0;
		Arrays.sort(frequencies);
		
		for (int i=0; i<frequencies.length; i++){
			for (int j=i+1; j<frequencies.length; j++){
				if (isHarmonic(frequencies[i],frequencies[j])) harmonicCount++;
				count++;
			}
		}
		
		return (double)harmonicCount/count;
	}
	
	public boolean isHarmonic(double f1, double f2){
		
		if (Math.abs(getToneChanged(f1,f2))>=1){		
			double minF0=100;
			int minDivisor=(int)(f1/minF0);
			
			for (int i=1; i<=minDivisor; i++){
				double f0=f1/i;
				int maxMultiplier=(int)(f2/f0+1);
				for (int j=2; j<=maxMultiplier; j++){
					double f=f0*j;
					double diff=Math.abs(getToneChanged(f,f2)%12);
					if (diff>6) diff=12-diff;
					if (diff<=1) return true;
				}
			}
		}
		
		return false;
	}
}