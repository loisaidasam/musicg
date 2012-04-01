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
 * Handles the wave data in amplitude-time domain.
 *
 * @author Jacquet Wong
 */

import com.musicg.sound.Wave;

public class AmplitudeTimeDomainRepresentation extends TimeDomainRepresentation{

	private double[] normalizedAmplitudes; // normalizedAmplitudes[sampleNumber]=normalizedAmplitudeInTheFrame
	private float timeStep;

	public AmplitudeTimeDomainRepresentation(Wave wave) {
		super(wave);
		setTimeStep(0.01F);
	}
	
	public AmplitudeTimeDomainRepresentation(Wave wave, float timeStep) {
		super(wave);
		setTimeStep(timeStep);
	}

	public double[] getNormalizedAmplitudes() {

		if (normalizedAmplitudes == null) {

			int numSamples = amplitudes.length;
			int maxAmplitude = 1 << (wave.getBitsPerSample() - 1);

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