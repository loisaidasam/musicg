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

package com.musicg.sound;

/**
 * Read WAVE headers and data from wave input stream
 *
 * @author Jacquet Wong
 */

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class Wave {

	private WaveHeader waveHeader;
		
	// wave header uses
	private String chunkId;
	private long chunkSize; // unsigned 4-bit, little endian
	private String format;
	private String subChunk1Id;
	private long subChunk1Size; // unsigned 4-bit, little endian
	private int audioFormat; // unsigned 2-bit, little endian
	private int channels; // unsigned 2-bit, little endian
	private long sampleRate; // unsigned 4-bit, little endian
	private long byteRate; // unsigned 4-bit, little endian
	private int blockAlign; // unsigned 2-bit, little endian
	private int bitsPerSample; // unsigned 2-bit, little endian
	private String subChunk2Id;
	private long subChunk2Size; // unsigned 4-bit, little endian
	private byte[] data;	// little endian
	
	public Wave(WaveInputStream waveInputStream) {
		waveHeader=waveInputStream.getWaveHeader();
		if (waveHeader.isValid()){
			
			// set header
			chunkSize=waveHeader.getChunkSize();
			subChunk1Size=waveHeader.getSubChunk1Size();
			audioFormat=waveHeader.getAudioFormat();
			channels=waveHeader.getChannels();
			sampleRate=waveHeader.getSampleRate();
			byteRate=waveHeader.getByteRate();
			blockAlign=waveHeader.getBlockAlign();
			bitsPerSample=waveHeader.getBitsPerSample();
			subChunk2Size=waveHeader.getSubChunk2Size();
			// end set header
			
			// load data
			try {
				data=new byte[waveInputStream.available()];
				waveInputStream.read(data);
			} catch (IOException e) {
				e.printStackTrace();
			}
			// end load data
		}
		else{
			System.out.println("Invalid Wave Header");
		}
	}

	public void trim(float leftTrimSecond, float rightTrimSecond) {
		
		long numLeftTrimmed = (int) (sampleRate * bitsPerSample / 8 * channels * leftTrimSecond);
		long numRightTrimmed = (int) (sampleRate * bitsPerSample / 8 * channels * rightTrimSecond);

		long totalTrimmed = numLeftTrimmed + numRightTrimmed;

		if (totalTrimmed > subChunk2Size) {
			numLeftTrimmed = subChunk2Size;
		}

		// update wav info
		chunkSize -= totalTrimmed;
		subChunk2Size -= totalTrimmed;

		byte[] trimmedData = new byte[(int) subChunk2Size];
		System.arraycopy(data, (int) numLeftTrimmed, trimmedData, 0,
				(int) subChunk2Size);
		data = trimmedData;
	}

	public void leftTrim(float second) {
		trim(second, 0);
	}

	public void rightTrim(float second) {
		trim(0, second);
	}

	public byte[] getBytes() {
		return data;
	}

	public String toString() {
		return waveHeader.toString();
	}

	public void saveAs(String savePath) {
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
	
	public float length() {
		return waveHeader.length();
	}

	public String timestamp() {
		return waveHeader.timestamp();
	}

	public int getChannels() {
		return channels;
	}

	public int getSampleRate() {
		return (int)sampleRate;
	}

	public int getByteRate() {
		return (int)byteRate;
	}

	public int getBitsPerSample() {
		return bitsPerSample;
	}

	public boolean isValid() {
		return waveHeader.isValid();
	}

	public String getChunkId() {
		return chunkId;
	}

	public long getChunkSize() {
		return chunkSize;
	}

	public String getFormat() {
		return format;
	}

	public String getSubChunk1Id() {
		return subChunk1Id;
	}

	public long getSubChunk1Size() {
		return subChunk1Size;
	}

	public int getAudioFormat() {
		return audioFormat;
	}

	public int getBlockAlign() {
		return blockAlign;
	}

	public String getSubChunk2Id() {
		return subChunk2Id;
	}

	public long getSubChunk2Size() {
		return subChunk2Size;
	}
	
}