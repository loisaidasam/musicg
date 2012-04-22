package com.musicg.main.test;

import com.musicg.graphic.GraphicRender;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveFileManager;
import com.musicg.wave.WaveTypeDetector;
import com.musicg.wave.extension.Spectrogram;

public class MainTest {

	public static void main(String[] args) {

		String filename = "audio_work/jac_whistle2.wav";
		String outFolder="out";

		// create a wave object
		Wave wave = new Wave(filename);

		// print the wave header and info
		System.out.println(wave);

		WaveTypeDetector waveTypeDetector=new WaveTypeDetector(wave);
		System.out.println("Whistle prob: "+waveTypeDetector.getWhistleProbability());
		
		// TimeDomainRepresentations
		Spectrogram spectrogram=new Spectrogram(wave);
		
		// get the amplitude
		double[] amplitudes=wave.getNormalizedAmplitudes();
		
		// Graphic render
		GraphicRender render=new GraphicRender();
		//render.setHorizontalMarker(1);
		//render.setVerticalMarker(1);
		render.renderWaveform(wave, outFolder+"/waveform.jpg");
		render.renderSpectrogram(spectrogram, outFolder+"/spectrogram.jpg");
		
		// change the amplitude representation
		float timeStep=0.1F;
		render.renderWaveform(wave,timeStep,outFolder+"/waveform2.jpg");

		// change the spectrogram representation
		int fftSampleSize=512;
		int overlapFactor=2;
		spectrogram=new Spectrogram(wave,fftSampleSize,overlapFactor);
		render.renderSpectrogram(spectrogram,outFolder+"/spectrogram2.jpg");
		
		// trim the wav
		wave.leftTrim(1);
		wave.rightTrim(0.5F);

		// save the trimmed wav
		WaveFileManager waveFileManager=new WaveFileManager(wave);
		waveFileManager.saveWaveAsFile(outFolder+"/out.wav");
	}
}