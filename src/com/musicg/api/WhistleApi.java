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

import com.musicg.math.rank.ArrayRankDouble;
import com.musicg.math.statistics.StandardDeviation;
import com.musicg.math.statistics.ZeroCrossingRate;
import com.musicg.pitch.PitchHandler;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;
import com.musicg.wave.extension.Spectrogram;

/**
 * Api for detect whistle
 * 
 * @author Jacquet Wong
 *
 */
public class WhistleApi {
	
	private WaveHeader waveHeader;
	private int fftSampleSize;
	private int numFrequencyUnit;
	private double unitFrequency;
	private double passFrequency;
	private double passIntensity;
	private double passStandardDeviation;
	private int highPass;		
	private int lowPass;
	private int minNumZeroCross;
	private int maxNumZeroCross;
	private int numRobust;
		
	/**
	 * Constructor, support mono Wav only
	 * 
	 * @param sampleRate	Sample rate of the input audio byte
	 * @param bitsPerSample	Bit size of a sample of the input audio byte
	 */
	public WhistleApi(WaveHeader waveHeader){
		if (waveHeader.getChannels()==1){
			this.waveHeader=waveHeader;
			fftSampleSize=1024;	// higher resolution
			//fftSampleSize=512;	// lower resolution
			numFrequencyUnit=fftSampleSize/2;
			unitFrequency=(double)waveHeader.getSampleRate()/2/numFrequencyUnit;	// frequency could be caught within the half of nSamples according to Nyquist theory
			passFrequency=400;
			passIntensity=100;
			passStandardDeviation=0.2;
			highPass=400;		
			lowPass=4000;
			minNumZeroCross=60;
			maxNumZeroCross=90;
			numRobust=2;
		}
		else{
			System.err.println("Whistle API supports mono Wav only");
		}
	}
	
	/**
	 * Determine the audio bytes contains a whistle or not
	 * 
	 * @param audioBytes	input audio byte
	 * @return
	 */
	public boolean isWhistle(byte[] audioBytes){
				
		int bytesPerSample=waveHeader.getBitsPerSample()/8;
		int numSamples = audioBytes.length / bytesPerSample;
		
		// numSamples required to be a power of 2
		if (numSamples>0 && Integer.bitCount(numSamples)==1){
			fftSampleSize=numSamples;
			
			Wave wave=new Wave(waveHeader,audioBytes);
			
			// amplitudes of the clip
			short[] amplitudes=wave.getSampleAmplitudes();

			// spectrum for the clip
			Spectrogram spectrogram=wave.getSpectrogram(fftSampleSize, 0);
			double[][] spectrogramData=spectrogram.getAbsoluteSpectrogramData();

			// since fftSampleSize==numSamples, there're only one spectrum which is spectrogramData[0]
			double[] spectrum=spectrogramData[0];
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
			int frequencyUnitRange=upperBoundary-lowerBoundary+1;
			
			if (frequencyUnitRange<=spectrum.length){
			
				double[] temp=new double[frequencyUnitRange];			
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
							// when lengthInSecond=1, zero crossing rate is the num of zero crosses
							ZeroCrossingRate zcr=new ZeroCrossingRate(amplitudes,1);
							int numZeroCrosses=(int)zcr.evaluate();
							if (numZeroCrosses>=minNumZeroCross && numZeroCrosses<=maxNumZeroCross){
								return true;
							}
						}
					}
				}
			}
			else{
				System.err.println("isWhistle error: the wave needed to be higher sample rate");
			}
			
		}
		else{
			System.out.println("The sample size must be a power of 2");
		}
		
		return false;		
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