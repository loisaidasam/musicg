package com.musicg.math.function;

public class Sum extends MathFunction{

	public Sum(){		
	}
	
	public Sum(double[] values){		
		setValues(values);
	}
	
	public double evaluate(){
		double sum=0;
		int size=values.length;
		for (int i=0 ;i<size; i++){
			sum+=values[i];
		}
		return sum;
	}
	
	public int size(){
		return values.length;
	}
}