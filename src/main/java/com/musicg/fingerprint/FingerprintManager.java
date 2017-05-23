/*
 * Copyright (C) 2012 Jacquet Wong
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

package com.musicg.fingerprint;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.musicg.dsp.Resampler;
import com.musicg.processor.TopManyPointsProcessorChain;
import com.musicg.properties.FingerprintProperties;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;
import com.musicg.wave.extension.Spectrogram;

/**
 * Audio fingerprint manager, handle fingerprint operations
 * 
 * @author jacquet
 *
 */
public class FingerprintManager{
	
	private FingerprintProperties fingerprintProperties=FingerprintProperties.getInstance();
	private int sampleSizePerFrame=fingerprintProperties.getSampleSizePerFrame();
	private int overlapFactor=fingerprintProperties.getOverlapFactor();
	private int numRobustPointsPerFrame=fingerprintProperties.getNumRobustPointsPerFrame();
	private int numFilterBanks=fingerprintProperties.getNumFilterBanks();
	
	/**
	 * Constructor
	 */
	public FingerprintManager(){
		
	}

	/**
	 * Extract fingerprint from Wave object
	 * 
	 * @param wave	Wave Object to be extracted fingerprint
	 * @return fingerprint in bytes
	 */
	public byte[] extractFingerprint(Wave wave){

		int[][] coordinates;	// coordinates[x][0..3]=y0..y3
		byte[] fingerprint=new byte[0];
				
		// resample to target rate
		Resampler resampler=new Resampler();
		int sourceRate = wave.getWaveHeader().getSampleRate();
        int targetRate = fingerprintProperties.getSampleRate();

       	byte[] resampledWaveData=resampler.reSample(wave.getBytes(), wave.getWaveHeader().getBitsPerSample(), sourceRate, targetRate);
		
        // update the wave header
        WaveHeader resampledWaveHeader=wave.getWaveHeader();
        resampledWaveHeader.setSampleRate(targetRate);
        
        // make resampled wave
        Wave resampledWave=new Wave(resampledWaveHeader,resampledWaveData);
        // end resample to target rate
        
		// get spectrogram's data
		Spectrogram spectrogram=resampledWave.getSpectrogram(sampleSizePerFrame, overlapFactor);
		double[][] spectorgramData=spectrogram.getNormalizedSpectrogramData();
		
		List<Integer>[] pointsLists=getRobustPointList(spectorgramData);
		int numFrames=pointsLists.length;
				
		// prepare fingerprint bytes
		coordinates=new int[numFrames][numRobustPointsPerFrame];
			
		for (int x=0; x<numFrames; x++){
			if (pointsLists[x].size()==numRobustPointsPerFrame){
				Iterator<Integer> pointsListsIterator=pointsLists[x].iterator();
				for (int y=0; y<numRobustPointsPerFrame; y++){
					coordinates[x][y]=pointsListsIterator.next();
				}
			}
			else{		
				// use -1 to fill the empty byte
				for (int y=0; y<numRobustPointsPerFrame; y++){
					coordinates[x][y]=-1;
				}
			}
		}		
		// end make fingerprint
			
		// for each valid coordinate, append with its intensity
		List<Byte> byteList=new LinkedList<Byte>();
		for (int i=0; i<numFrames; i++){
			for (int j=0; j<numRobustPointsPerFrame; j++){
				if (coordinates[i][j]!=-1){
					// first 2 bytes is x
					int x=i;
					byteList.add((byte)(x>>8));
					byteList.add((byte)x);
					
					// next 2 bytes is y
					int y=coordinates[i][j];
					byteList.add((byte)(y>>8));
					byteList.add((byte)y);
					
					// next 4 bytes is intensity
					int intensity=(int)(spectorgramData[x][y]*Integer.MAX_VALUE);	// spectorgramData is ranged from 0~1
					byteList.add((byte)(intensity>>24));
					byteList.add((byte)(intensity>>16));
					byteList.add((byte)(intensity>>8));
					byteList.add((byte)intensity);
				}
			}
		}
		// end for each valid coordinate, append with its intensity
			
		fingerprint=new byte[byteList.size()];
		Iterator<Byte> byteListIterator=byteList.iterator();
		int pointer=0;
		while(byteListIterator.hasNext()){
			fingerprint[pointer++]=byteListIterator.next();
		}

		return fingerprint;
	}

	/**
	 * Get bytes from fingerprint file
	 * 
	 * @param fingerprintFile	fingerprint filename
	 * @return fingerprint in bytes
	 */
	public byte[] getFingerprintFromFile(String fingerprintFile){
		byte[] fingerprint=null;
		try {
			InputStream fis=new FileInputStream(fingerprintFile);
			fingerprint=getFingerprintFromInputStream(fis);
			fis.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fingerprint;
	}
	
	/**
	 * Get bytes from fingerprint inputstream
	 * 
	 * @param fingerprintFile	fingerprint inputstream
	 * @return fingerprint in bytes
	 */
	public byte[] getFingerprintFromInputStream(InputStream inputStream){		
		byte[] fingerprint=null;
		try {
			fingerprint = new byte[inputStream.available()];
			inputStream.read(fingerprint);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return fingerprint;
	}
	
	/**
	 * Save fingerprint to a file
	 * 
	 * @param fingerprint	fingerprint bytes
	 * @param filename		fingerprint filename
	 * @see	fingerprint file saved
	 */
	public void saveFingerprintAsFile(byte[] fingerprint, String filename){

        FileOutputStream fileOutputStream;
		try {
			fileOutputStream = new FileOutputStream(filename);
			fileOutputStream.write(fingerprint);
			fileOutputStream.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	// robustLists[x]=y1,y2,y3,...
	private List<Integer>[] getRobustPointList(double[][] spectrogramData){
		
		int numX=spectrogramData.length;
		int numY=spectrogramData[0].length;
		
		double[][] allBanksIntensities=new double[numX][numY];		
		int bandwidthPerBank=numY/numFilterBanks;
		
		for (int b=0; b<numFilterBanks; b++){
			
			double[][] bankIntensities=new double[numX][bandwidthPerBank];
			
			for (int i=0; i<numX; i++){
				for (int j=0; j<bandwidthPerBank; j++){
					bankIntensities[i][j]=spectrogramData[i][j+b*bandwidthPerBank];
				}
			}
			
			// get the most robust point in each filter bank
			TopManyPointsProcessorChain processorChain=new TopManyPointsProcessorChain(bankIntensities,1);
			double[][] processedIntensities=processorChain.getIntensities();
			
			for (int i=0; i<numX; i++){
				for (int j=0; j<bandwidthPerBank; j++){
					allBanksIntensities[i][j+b*bandwidthPerBank]=processedIntensities[i][j];
				}
			}
		}
		
		List<int[]> robustPointList=new LinkedList<int[]>();
		
		// find robust points
		for (int i=0; i<allBanksIntensities.length; i++){
			for (int j=0; j<allBanksIntensities[i].length; j++){	
				if (allBanksIntensities[i][j]>0){
					
					int[] point=new int[]{i,j};
					//System.out.println(i+","+frequency);
					robustPointList.add(point);
				}
			}
		}
		// end find robust points

		List<Integer>[] robustLists=new LinkedList[spectrogramData.length];
		for (int i=0; i<robustLists.length; i++){
			robustLists[i]=new LinkedList<Integer>();
		}
		
		// robustLists[x]=y1,y2,y3,...
		Iterator<int[]> robustPointListIterator=robustPointList.iterator();
		while (robustPointListIterator.hasNext()){
			int[] coor=robustPointListIterator.next();
			robustLists[coor[0]].add(coor[1]);
		}
		
		// return the list per frame
		return robustLists;
	}

	/**
	 * Number of frames in a fingerprint
	 * Each frame lengths 8 bytes
	 * Usually there is more than one point in each frame, so it cannot simply divide the bytes length by 8
	 * Last 8 byte of thisFingerprint is the last frame of this wave
	 * First 2 byte of the last 8 byte is the x position of this wave, i.e. (number_of_frames-1) of this wave	 
	 * 
	 * @param fingerprint	fingerprint bytes
	 * @return number of frames of the fingerprint
	 */
	public static int getNumFrames(byte[] fingerprint){
		
		if (fingerprint.length<8){
			return 0;
		}
		
		// get the last x-coordinate (length-8&length-7)bytes from fingerprint
		int numFrames=((int)(fingerprint[fingerprint.length-8]&0xff)<<8 | (int)(fingerprint[fingerprint.length-7]&0xff))+1;
		return numFrames;
	}
}