/*
 * Copyright (C) 2011 Jacquet Wong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.musicg.api;

import com.musicg.dsp.FastFourierTransform;
import com.musicg.dsp.WindowFunction;
import com.musicg.math.rank.ArrayRankDouble;
import com.musicg.math.statistics.StandardDeviation;
import com.musicg.pitch.PitchHandler;

/**
 * Api for detect whistle
 * 
 * @author Jacquet Wong
 *
 */
public class WhistleApi {
	
	private int sampleRate=44100;
	private int bitsPerSample=16;
	private int fftSampleSize=1024;
	private int numFrequencyUnit=fftSampleSize/2;
	private double unitFrequency=(double)sampleRate/2/numFrequencyUnit;	// frequency could be caught within the half of nSamples according to Nyquist theory
	private double passFrequency=400;
	private double passIntensity=100;
	private double passStandardDeviation=0.2;
	private int highPass=400;		
	private int lowPass=4000;
	private int minNumZeroCross=60;
	private int maxNumZeroCross=90;
	private int numRobust=2;
	
	/**
	 * Constructor, support mono Wav only
	 * 
	 * @param sampleRate	Sample rate of the input audio byte
	 * @param bitsPerSample	Bit size of a sample of the input audio byte
	 */
	public WhistleApi(int sampleRate, int bitsPerSample){
		this.sampleRate=sampleRate;
		this.bitsPerSample=bitsPerSample;
	}
	
	/**
	 * Determine the audio bytes contains a whistle or not
	 * 
	 * @param audioBytes	input audio byte
	 * @return
	 */
	public boolean isWhistle(byte[] audioBytes){
				
		int bytesPerSample=bitsPerSample/8;
		int numSamples = audioBytes.length / bytesPerSample;
		
		if (Integer.bitCount(numSamples)==1){
			this.fftSampleSize=numSamples;

			// amplitudes of the clip
			short[] amplitudes=getAmplitudes(audioBytes,bitsPerSample);
			
			// spectrum for the clip
			double[] spectrum=getSpectrum(amplitudes);

			// get the average intensity of the signal
			double intensity=0;
			for (int i=0; i<spectrum.length; i++){
				intensity+=spectrum[i];
			}
			intensity/=spectrum.length;
			// end get the average intensity of the signal
			
			// normalize the spectrum
			normalizeSpectrum(spectrum);
			
			// set boundary
			int lowerBoundary=(int)(highPass/unitFrequency);
			int upperBoundary=(int)(lowPass/unitFrequency);
			// end set boundary
			
			// copy the significant range of the spectrum to a temp spectrum
			double[] temp=new double[upperBoundary-lowerBoundary+1];
			System.arraycopy(spectrum, lowerBoundary, temp, 0, temp.length);
			
			StandardDeviation standardDeviation=new StandardDeviation();
			standardDeviation.setValues(temp);			
			
			double sd=standardDeviation.evaluate();
			
			// rule 1: clear whistle has a range of standard deviation 
			if (sd<passStandardDeviation){
			
				// find the robust frequency
				ArrayRankDouble arrayRankDouble=new ArrayRankDouble();
				double maxFrequency=arrayRankDouble.getMaxValueIndex(temp)*unitFrequency;
				
				// find top most robust frequencies
				double[] robustFrequencies=new double[numRobust];
				double nthValue=arrayRankDouble.getNthOrderedValue(temp, numRobust, false);
				int count=0;
				for (int b=lowerBoundary; b<=upperBoundary; b++){
					if (spectrum[b]>=nthValue){
						robustFrequencies[count++]=b*unitFrequency;
						if (count>=numRobust){
							break;
						}
					}
				}
				// end find top most robust frequencies
				
				
				PitchHandler pitchHandler=new PitchHandler();
				
				// rule2: frequency of the whistle should not be too low and soft
				if (maxFrequency>=passFrequency && intensity>passIntensity){
					
					double probability=pitchHandler.getHarmonicProbability(robustFrequencies);
					//System.out.println(maxFrequency+" "+intensity+" "+probability);
					
					// rule3: whistle doesn't have obvious harmonics
					if (probability<0.5){
						
						// rule4: whistle has a range of zero crossing value
						int zc=getNumZeroCrosses(amplitudes);
						if (zc>=minNumZeroCross && zc<=maxNumZeroCross){
							return true;
						}
					}
				}
			}
			
		}
		else{
			System.out.print("The sample size must be a power of 2");
		}
		
		return false;		
	}
	
	private short[] getAmplitudes(byte[] audioBytes, int bitsPerSample) {

		int bytesPerSample=bitsPerSample/8;
		int numSamples = audioBytes.length / bytesPerSample;
		short[] amplitudes = new short[numSamples];

		int pointer = 0;
		for (int i = 0; i < numSamples; i++) {
			short amplitude = 0;
			for (int byteNumber = 0; byteNumber < bytesPerSample; byteNumber++) {
				// little endian
				amplitude |= (short) ((audioBytes[pointer++] & 0xFF) << (byteNumber * 8));
			}
			amplitudes[i] = amplitude;
		}

		return amplitudes;
	}
	
	private double[] getSpectrum(short[] amplitudes){
		
		int sampleSize=amplitudes.length;
		WindowFunction window = new WindowFunction();
		window.setWindowType("Hamming");
		double[] win=window.generate(sampleSize);
		
		// signals for fft input
		double[] signals=new double[sampleSize];		
		for (int i=0; i<sampleSize; i++){
			signals[i]=amplitudes[i]*win[i];							
		}
		
		FastFourierTransform fft = new FastFourierTransform();
		double[] spectrum=new double[sampleSize];
		spectrum=fft.getMagnitudes(signals);
		
		return spectrum;
	}
	
	private int getNumZeroCrosses(short[] amplitudes){
		
		int numZC=0;
		int size=amplitudes.length;
		
		for (int i=0; i<size-1; i++){
			if((amplitudes[i]>=0 && amplitudes[i+1]<0) || (amplitudes[i]<0 && amplitudes[i+1]>=0)){
				numZC++;
			}
		}	
		
		return numZC;
	}
	
	private void normalizeSpectrum(double[] spectrum){

		// normalization of absoultSpectrogram
		// set max and min amplitudes
		double maxAmp=Double.MIN_VALUE;
	    double minAmp=Double.MAX_VALUE;	
		for (int i=0; i<spectrum.length; i++){
			if (spectrum[i]>maxAmp){
				maxAmp=spectrum[i];
			}
			else if(spectrum[i]<minAmp){
				minAmp=spectrum[i];
			}
		}
		// end set max and min amplitudes
		
		// normalization
		// avoiding divided by zero 
		double minValidAmp=0.00000000001F;
		if (minAmp==0){
			minAmp=minValidAmp;
		}
		
		double diff=Math.log10(maxAmp/minAmp);	// perceptual difference
		for (int i=0; i<spectrum.length; i++){
			if (spectrum[i]<minValidAmp){
				spectrum[i]=0;
			}
			else{
				spectrum[i]=(Math.log10(spectrum[i]/minAmp))/diff;
			}
		}
		// end normalization
	}
}