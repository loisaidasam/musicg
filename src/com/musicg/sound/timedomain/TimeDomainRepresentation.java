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

import com.musicg.sound.Wave;

public class TimeDomainRepresentation{
	
	protected Wave wave;
	protected byte[] audioBytes;
	protected int bytePerSample;
	protected int numSamples;
	protected short[] amplitudes;	// amplitudes[sampleNumber]=amplitudeInTheFrame
	
	public TimeDomainRepresentation(Wave wave){
		this.wave=wave;
		audioBytes = wave.getBytes();
		bytePerSample = wave.getBitsPerSample() / 8;
		numSamples = audioBytes.length / bytePerSample;
		amplitudes = new short[numSamples];
		
		int pointer = 0;
		for (int i = 0; i < numSamples; i++) {
			short amplitude = 0;
			for (int byteNumber = 0; byteNumber < bytePerSample; byteNumber++) {
				// little endian
				amplitude |= (short) ((audioBytes[pointer++] & 0xFF) << (byteNumber * 8));
			}
			amplitudes[i] = amplitude;
		}
	}
	
	public short[] getAmplitudes() {
		return amplitudes;
	}

	public Wave getWave() {
		return wave;
	}
}