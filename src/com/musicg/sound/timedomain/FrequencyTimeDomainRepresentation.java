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

package com.musicg.sound.timedomain;

/**
 * Handles the wave data in frequency-time domain.
 *
 * @author Jacquet Wong
 */

import com.musicg.dsp.FastFourierTransform;
import com.musicg.dsp.WindowFunction;
import com.musicg.sound.Wave;

public class FrequencyTimeDomainRepresentation extends TimeDomainRepresentation{
	
	private double[][] spectrogram;	// relative spectrogram
	private double[][] absoluteSpectrogram;	// absolute spectrogram
	private int fftSampleSize;	// number of sample in fft, the value needed to be a number to power of 2
	private int overlapFactor;	// 1/overlapFactor overlapping, e.g. 1/4=25% overlapping
	private int numFrames;	// number of frames of the spectrogram
	private int framesPerSecond;	// frame per second of the spectrogram
	private int numFrequencyUnit;	// number of y-axis unit
	private double unitFrequency;	// frequency per y-axis unit
	private boolean rebuildSpectrogram;

	public FrequencyTimeDomainRepresentation(Wave wave) {
		super(wave);
		setFftSampleSize(1024);
		setOverlapFactor(1);
	}
	
	public FrequencyTimeDomainRepresentation(Wave wave, int fftSampleSize, int overlapFactor) {
		super(wave);
		setFftSampleSize(fftSampleSize);
		setOverlapFactor(overlapFactor);
	}
	
	public void setFftSampleSize(int fftSampleSize){
		
		if (Integer.bitCount(fftSampleSize)==1){
			this.fftSampleSize=fftSampleSize;
			rebuildSpectrogram=true;
		}
		else{
			System.out.print("The input number must be a power of 2");
		}
	}
	
	public void setOverlapFactor(int overlapFactor){
		this.overlapFactor=overlapFactor;
		rebuildSpectrogram=true;
	}
	
	public double[][] getSpectrogram(){
		buildSpectrogram();
		return spectrogram;
	}
	
	public double[][] getAbsoluteSpectrogram(){
		buildSpectrogram();
		return absoluteSpectrogram;
	}
	
	public void buildSpectrogram(){

		if (rebuildSpectrogram){
			int pointer=0;
			// overlapping
			if (overlapFactor>1){
				int numOverlappedSamples=numSamples*overlapFactor;
				int backSamples=fftSampleSize*(overlapFactor-1)/overlapFactor;
				int fftSampleSize_1=fftSampleSize-1;
				short[] overlapAmp= new short[numOverlappedSamples];
				pointer=0;
				for (int i=0; i<amplitudes.length; i++){
					overlapAmp[pointer++]=amplitudes[i];
					if (pointer%fftSampleSize==fftSampleSize_1){
						// overlap
						i-=backSamples;
					}
				}
				
				numSamples=numOverlappedSamples;
				amplitudes=overlapAmp;
			}
			// end overlapping
			
			numFrames=numSamples/fftSampleSize;
			framesPerSecond=(int)(numFrames/wave.length());	
			
			// set signals for fft
			WindowFunction window = new WindowFunction();
			window.setWindowType("Hamming");
			double[] win=window.generate(fftSampleSize);
	
			double[][] signals=new double[numFrames][];
			for(int f=0; f<numFrames; f++) {
				signals[f]=new double[fftSampleSize];
				int startSample=f*fftSampleSize;
				for (int n=0; n<fftSampleSize; n++){
					signals[f][n]=amplitudes[startSample+n]*win[n];							
				}
			}
			// end set signals for fft
			
			absoluteSpectrogram=new double[numFrames][];
			// for each frame in signals, do fft on it
			FastFourierTransform fft = new FastFourierTransform();
			for (int i=0; i<numFrames; i++){			
				absoluteSpectrogram[i]=fft.getMagnitudes(signals[i]);
			}
			
			if (absoluteSpectrogram.length>0){
				
				numFrequencyUnit=absoluteSpectrogram[0].length;
				unitFrequency=(double)wave.getSampleRate()/2/numFrequencyUnit;	// frequency could be caught within the half of nSamples according to Nyquist theory
					
				// normalization of absoultSpectrogram
				spectrogram=new double[numFrames][numFrequencyUnit];
				
				// set max and min amplitudes
				double maxAmp=Double.MIN_VALUE;
			    double minAmp=Double.MAX_VALUE;	
				for (int i=0; i<numFrames; i++){
					for (int j=0; j<numFrequencyUnit; j++){
						if (absoluteSpectrogram[i][j]>maxAmp){
							maxAmp=absoluteSpectrogram[i][j];
						}
						else if(absoluteSpectrogram[i][j]<minAmp){
							minAmp=absoluteSpectrogram[i][j];
						}
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
				for (int i=0; i<numFrames; i++){
					for (int j=0; j<numFrequencyUnit; j++){
						if (absoluteSpectrogram[i][j]<minValidAmp){
							spectrogram[i][j]=0;
						}
						else{
							spectrogram[i][j]=(Math.log10(absoluteSpectrogram[i][j]/minAmp))/diff;
						}
					}
				}
				// end normalization
			}
			
			rebuildSpectrogram=false;
		}
	}
	
	public int getNumFrames(){
		buildSpectrogram();
		return numFrames;
	}
	
	public int getFramesPerSecond(){
		buildSpectrogram();
		return framesPerSecond;
	}
	
	public int getNumFrequencyUnit(){
		buildSpectrogram();
		return numFrequencyUnit;
	}
	
	public double getUnitFrequency(){
		buildSpectrogram();
		return unitFrequency;
	}

	public int getFftSampleSize() {
		return fftSampleSize;
	}

	public int getOverlapFactor() {
		return overlapFactor;
	}
}