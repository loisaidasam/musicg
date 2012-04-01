package com.musicg.math.function;

public class Mean extends MathFunction{
	
	private Sum sum=new Sum();
	
	public Mean(){
	}
	
	public Mean(double[] values){
		setValues(values);
	}
	
	public double evaluate(){	
		sum.setValues(values);
		double mean=sum.evaluate()/sum.size();
		return mean;
	}
}