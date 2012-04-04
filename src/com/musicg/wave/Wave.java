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

package com.musicg.wave;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Read WAVE headers and data from wave input stream
 * 
 * @author Jacquet Wong
 */
public class Wave {

	private WaveHeader waveHeader;
	private byte[] data; // little endian

	/**
	 * Constructor
	 * 
	 * @param waveInputStream
	 *            Wave file input stream
	 */
	public Wave(WaveInputStream waveInputStream) {
		waveHeader = waveInputStream.getWaveHeader();
		if (waveHeader.isValid()) {
			// load data
			try {
				data = new byte[waveInputStream.available()];
				waveInputStream.read(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
			// end load data
		} else {
			System.out.println("Invalid Wave Header");
		}
	}

	/**
	 * Trim the wave data
	 * 
	 * @param leftTrimSecond
	 *            Seconds trimmed from beginning
	 * @param rightTrimSecond
	 *            Seconds trimmed from ending
	 */
	public void trim(float leftTrimSecond, float rightTrimSecond) {

		int sampleRate = waveHeader.getSampleRate();
		int bitsPerSample = waveHeader.getBitsPerSample();
		int channels = waveHeader.getChannels();
		long chunkSize = waveHeader.getChunkSize();
		long subChunk2Size = waveHeader.getSubChunk2Size();

		long numLeftTrimmed = (int) (sampleRate * bitsPerSample / 8 * channels * leftTrimSecond);
		long numRightTrimmed = (int) (sampleRate * bitsPerSample / 8 * channels * rightTrimSecond);

		long totalTrimmed = numLeftTrimmed + numRightTrimmed;

		if (totalTrimmed > subChunk2Size) {
			numLeftTrimmed = subChunk2Size;
		}

		// update wav info
		chunkSize -= totalTrimmed;
		subChunk2Size -= totalTrimmed;
		waveHeader.setChunkSize(chunkSize);
		waveHeader.setSubChunk2Size(subChunk2Size);

		byte[] trimmedData = new byte[(int) subChunk2Size];
		System.arraycopy(data, (int) numLeftTrimmed, trimmedData, 0,
				(int) subChunk2Size);
		data = trimmedData;
	}

	/**
	 * Trim the wave data from beginning
	 * 
	 * @param second
	 *            Seconds trimmed from beginning
	 */
	public void leftTrim(float second) {
		trim(second, 0);
	}

	/**
	 * Trim the wave data from ending
	 * 
	 * @param second
	 *            Seconds trimmed from ending
	 */
	public void rightTrim(float second) {
		trim(0, second);
	}

	/**
	 * Get the wave data in bytes
	 * 
	 * @return wave data
	 */
	public byte[] getBytes() {
		return data;
	}

	/**
	 * @return wave header
	 */
	public String toString() {
		return waveHeader.toString();
	}

	/**
	 * Save the wave file
	 * 
	 * @param savePath
	 *            filepath to be saved
	 */
	public void saveAs(String savePath) {

		int byteRate = waveHeader.getByteRate();
		int audioFormat = waveHeader.getAudioFormat();
		int sampleRate = waveHeader.getSampleRate();
		int bitsPerSample = waveHeader.getBitsPerSample();
		int channels = waveHeader.getChannels();
		long chunkSize = waveHeader.getChunkSize();
		long subChunk1Size = waveHeader.getSubChunk1Size();
		long subChunk2Size = waveHeader.getSubChunk2Size();
		int blockAlign = waveHeader.getBlockAlign();

		try {
			FileOutputStream fos = new FileOutputStream(savePath);
			fos.write(WaveHeader.RIFF_HEADER.getBytes());
			// little endian
			fos.write(new byte[] { (byte) (chunkSize), (byte) (chunkSize >> 8),
					(byte) (chunkSize >> 16), (byte) (chunkSize >> 24) });
			fos.write(WaveHeader.WAVE_HEADER.getBytes());
			fos.write(WaveHeader.FMT_HEADER.getBytes());
			fos.write(new byte[] { (byte) (subChunk1Size),
					(byte) (subChunk1Size >> 8), (byte) (subChunk1Size >> 16),
					(byte) (subChunk1Size >> 24) });
			fos.write(new byte[] { (byte) (audioFormat),
					(byte) (audioFormat >> 8) });
			fos.write(new byte[] { (byte) (channels), (byte) (channels >> 8) });
			fos.write(new byte[] { (byte) (sampleRate),
					(byte) (sampleRate >> 8), (byte) (sampleRate >> 16),
					(byte) (sampleRate >> 24) });
			fos.write(new byte[] { (byte) (byteRate), (byte) (byteRate >> 8),
					(byte) (byteRate >> 16), (byte) (byteRate >> 24) });
			fos.write(new byte[] { (byte) (blockAlign),
					(byte) (blockAlign >> 8) });
			fos.write(new byte[] { (byte) (bitsPerSample),
					(byte) (bitsPerSample >> 8) });
			fos.write(WaveHeader.DATA_HEADER.getBytes());
			fos.write(new byte[] { (byte) (subChunk2Size),
					(byte) (subChunk2Size >> 8), (byte) (subChunk2Size >> 16),
					(byte) (subChunk2Size >> 24) });
			fos.write(this.getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Length of the wave in second
	 * 
	 * @return length in second
	 */
	public float length() {
		return waveHeader.length();
	}

	/**
	 * Timestamp of the wave length
	 * 
	 * @return timestamp
	 */
	public String timestamp() {
		return waveHeader.timestamp();
	}

	public int getChannels() {
		return waveHeader.getChannels();
	}

	public int getSampleRate() {
		return waveHeader.getSampleRate();
	}

	public int getByteRate() {
		return waveHeader.getByteRate();
	}

	public int getBitsPerSample() {
		return waveHeader.getBitsPerSample();
	}

	public boolean isValid() {
		return waveHeader.isValid();
	}

	public String getChunkId() {
		return waveHeader.getChunkId();
	}

	public long getChunkSize() {
		return waveHeader.getChunkSize();
	}

	public String getFormat() {
		return waveHeader.getFormat();
	}

	public String getSubChunk1Id() {
		return waveHeader.getSubChunk1Id();
	}

	public long getSubChunk1Size() {
		return waveHeader.getSubChunk1Size();
	}

	public int getAudioFormat() {
		return waveHeader.getAudioFormat();
	}

	public int getBlockAlign() {
		return waveHeader.getBlockAlign();
	}

	public String getSubChunk2Id() {
		return waveHeader.getSubChunk2Id();
	}

	public long getSubChunk2Size() {
		return waveHeader.getSubChunk2Size();
	}

}