package com.musicg.dsp;

import com.sun.media.sound.FFT;

public class FastFourierTransform {

	public double[] getMagnitudes(double[] amplitudes) {

		int sampleSize = amplitudes.length;
		
		// call the fft and transform the complex numbers
		FFT fft = new FFT(sampleSize/2,-1);
		fft.transform(amplitudes);
		// end call the fft and transform the complex numbers
		
		double[] complexNumbers=amplitudes;
		// even indexes (0,2,4,6,...) are real parts
		// odd indexes (1,3,5,7,...) are img parts

		// FFT produces a transformed pair of arrays where the first half of the values represent positive frequency components and the second half represents negative frequency components.
		double[] mag = new double[sampleSize/2];
		for (int i = 0; i < sampleSize; i+=2)
			mag[i/2] = Math.sqrt(complexNumbers[i] * complexNumbers[i]+ complexNumbers[i+1] * complexNumbers[i+1]);
		
		return mag;
	}
	
}
