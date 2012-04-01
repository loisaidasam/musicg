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
 * Read raw WAVE data from input stream, in fact it just skip the first 44 bytes of WAVE header
 *
 * @author Jacquet Wong
 */

import java.io.IOException;
import java.io.InputStream;

public class WaveInputStream extends InputStream {

	private InputStream inputStream;
	private WaveHeader waveHeader;

	public WaveInputStream(InputStream inputStream) {
		
		this.inputStream = inputStream;
		waveHeader=new WaveHeader(this.inputStream);
	}

	public int available() throws IOException {
		return inputStream.available();
	}

	public void mark(int readlimit) {
		inputStream.mark(readlimit);
	}

	public boolean markSupported() {
		return inputStream.markSupported();
	}

	public int read(byte[] b, int off, int len) throws IOException {
		return inputStream.read(b, off, len);
	}

	public int read(byte[] b) throws IOException {
		return inputStream.read(b);
	}

	public int read() throws IOException {
		return inputStream.read();
	}

	public void reset() throws IOException {
		inputStream.reset();
	}

	public long skip(long n) throws IOException {
		return inputStream.skip(n);
	}

	public void close() throws IOException {
		inputStream.close();
	}

	public WaveHeader getWaveHeader() {
		return waveHeader;
	}
}