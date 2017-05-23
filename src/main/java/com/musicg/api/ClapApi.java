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

import com.musicg.wave.WaveHeader;

/**
 * Api for detecting clap
 * 
 * @author Jacquet Wong
 * 
 */
public class ClapApi extends DetectionApi{
	
	public ClapApi(WaveHeader waveHeader) {
		super(waveHeader);
	}

	protected void init(){
		// settings for detecting a clap
		minFrequency = 1000.0f;
		maxFrequency = Double.MAX_VALUE;
		
		// get the decay part of a clap
		minIntensity = 10000.0f;
		maxIntensity = 100000.0f;
		
		minStandardDeviation = 0.0f;
		maxStandardDeviation = 0.05f;
		
		highPass = 100;
		lowPass = 10000;
		
		minNumZeroCross = 100;
		maxNumZeroCross = 500;
		
		numRobust = 4;
	}
		
	public boolean isClap(byte[] audioBytes){
		return isSpecificSound(audioBytes);
	}
}