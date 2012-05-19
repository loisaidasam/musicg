package com.musicg.main.demo;

import com.musicg.wave.Wave;
import com.musicg.wave.WaveFileManager;

public class WaveDemo {

	public static void main(String[] args) {

		String filename = "audio_work/cock_a_1.wav";
		String outFolder="out";

		// create a wave object
		Wave wave = new Wave(filename);

		// print the wave header and info
		System.out.println(wave);

		// trim the wav
		wave.leftTrim(1);
		wave.rightTrim(0.5F);

		// save the trimmed wav
		WaveFileManager waveFileManager=new WaveFileManager(wave);
		waveFileManager.saveWaveAsFile(outFolder+"/out.wav");
	}
}