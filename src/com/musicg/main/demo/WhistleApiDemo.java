package com.musicg.main.demo;

import com.musicg.wave.Wave;
import com.musicg.wave.WaveTypeDetector;

public class WhistleApiDemo{
	public static void main(String[] args){		
		String filename = "audio_work/whistle.wav";

		// create a wave object
		Wave wave = new Wave(filename);

		WaveTypeDetector waveTypeDetector=new WaveTypeDetector(wave);
		System.out.println("Is whistle probability: "+waveTypeDetector.getWhistleProbability());
	}
}