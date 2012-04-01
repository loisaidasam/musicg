package com.musicg.math.function;

public abstract class MathFunction{
	
	protected double[] values;
	
	public void setValues(double[] values){
		this.values=values;
	}
	
	public abstract double evaluate();
}