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

package com.musicg.representation.timedomain;

import com.musicg.wave.Wave;

/**
 * Handles the wave data in amplitude-time domain.
 *
 * @author Jacquet Wong
 */
public class AmplitudeTimeDomainRepresentation extends TimeDomainRepresentation{

	private double[] normalizedAmplitudes; // normalizedAmplitudes[sampleNumber]=normalizedAmplitudeInTheFrame
	private float timeStep;

	/**
	 * Constructor
	 * 
	 * @param wave
	 */
	public AmplitudeTimeDomainRepresentation(Wave wave) {
		super(wave);
			
		// default setting
		setTimeStep(0.1F);
	}
	
	/**
	 * Constructor
	 * 
	 * @param wave
	 * @param timeStep	time interval in second, as known as 1/fps
	 */
	public AmplitudeTimeDomainRepresentation(Wave wave, float timeStep) {
		super(wave);
		setTimeStep(timeStep);
	}

	/**
	 * Get absolute amplitude of each frame
	 * 
	 * @return	array of amplitudes (signed 16 bit): amplitudes[frame]=amplitude
	 */
	public short[] getAmplitudes() {
		return amplitudes;
	}
	
	/**
	 * 
	 * Get normalized amplitude of each frame
	 * 
	 * @return	array of normalized amplitudes(signed 16 bit): normalizedAmplitudes[frame]=amplitude
	 */
	public double[] getNormalizedAmplitudes() {

		if (normalizedAmplitudes == null) {

			boolean signed=true;
			
			// usually 8bit is unsigned
			if (wave.getBitsPerSample()==8){
				signed=false;
			}
			
			int numSamples = amplitudes.length;
			int maxAmplitude = 1 << (wave.getBitsPerSample() - 1);
			
			if (!signed){	// one more bit for unsigned value
				maxAmplitude<<=1;
			}
			
			normalizedAmplitudes = new double[numSamples];
			for (int i = 0; i < numSamples; i++) {
				normalizedAmplitudes[i] = (double) amplitudes[i] / maxAmplitude;
			}
		}

		return normalizedAmplitudes;
	}

	public float getTimeStep() {
		return timeStep;
	}

	public void setTimeStep(float timeStep) {
		this.timeStep = timeStep;
	}
}