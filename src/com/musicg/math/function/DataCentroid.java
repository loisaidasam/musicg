package com.musicg.math.function;

public class DataCentroid extends MathFunction{
	
	public DataCentroid(){
		
	}
	
	public DataCentroid(double[] values){
		setValues(values);
	}
		
	public double evaluate(){
		double sumCentroid=0;
		double sumIntensities=0;
		int size=values.length;
		
		for (int i=0; i<size; i++){
			if (values[i]>0){
				sumCentroid+=i*values[i];
				sumIntensities+=values[i];
			}
		}
		double avgCentroid=sumCentroid/sumIntensities;
		
		return avgCentroid;
	}
}
