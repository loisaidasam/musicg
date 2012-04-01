package com.musicg.main.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import com.musicg.sound.WaveHeader;
import com.musicg.sound.WaveInputStream;
import com.musicg.sound.api.WhistleApi;

public class WhistleApiTest{
	public static void main(String[] args){		
		
		String filename = "audio_work/soft_whistle.wav";

		// get the wave instance by input stream
		InputStream fis = null;
		try {
			fis = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// create a wave inputStream by inputStream
		WaveInputStream wis = new WaveInputStream(fis);
		WaveHeader wavHeader=wis.getWaveHeader();
		
		// fft size 1024, no overlap
		int fftSampleSize=1024;
		int fftSignalByteLength=fftSampleSize*wavHeader.getBitsPerSample()/8;				
		
		WhistleApi whistleApi=new WhistleApi(wavHeader.getSampleRate(),wavHeader.getBitsPerSample(),wavHeader.getChannels());
		
		// read the byte signals
		try {
			int numFrames = wis.available()/fftSignalByteLength;
			byte[] bytes=new byte[fftSignalByteLength];			
			int checkLength=10;
			int passScore=10;
			
			ArrayList<Boolean> bufferList=new ArrayList<Boolean>();
			int numWhistles=0;
			int numPasses=0;
			
			for (int frameNumber=0; frameNumber<checkLength; frameNumber++){
				wis.read(bytes);
				boolean isWhistle=whistleApi.isWhistle(bytes);
				bufferList.add(isWhistle);
				if (isWhistle){
					numWhistles++;
				}
				
				if (numWhistles>=passScore){
					numPasses++;
				}
				
				System.out.println(frameNumber+": "+numWhistles);
			}
			
			for (int frameNumber=checkLength; frameNumber<numFrames; frameNumber++){
				wis.read(bytes);
				boolean isWhistle=whistleApi.isWhistle(bytes);
				if (bufferList.get(0)){
					numWhistles--;
				}
				
				bufferList.remove(0);
				bufferList.add(isWhistle);
				
				if (isWhistle){
					numWhistles++;
				}			
				
				if (numWhistles>=passScore){
					numPasses++;
				}
				
				System.out.println(frameNumber+": "+numWhistles);
			}
			
			System.out.println("num passes: "+numPasses);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}