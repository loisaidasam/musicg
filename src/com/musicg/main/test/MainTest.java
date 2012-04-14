package com.musicg.main.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.musicg.graphic.GraphicRender;
import com.musicg.representation.timedomain.AmplitudeTimeDomainRepresentation;
import com.musicg.representation.timedomain.FrequencyTimeDomainRepresentation;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveInputStream;

public class MainTest {

	public static void main(String[] args) {

		String filename = "audio_work/11k8bitpcm.wav";
		String outFolder="out";

		// get the wave instance by input stream
		InputStream fis = null;
		try {
			fis = new FileInputStream(filename);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		// create a wave inputStream by inputStream
		WaveInputStream wis = new WaveInputStream(fis);

		// create a wave object by wave inputStream
		Wave wave = new Wave(wis);

		// print the wave header and info
		System.out.println(wave);

		// TimeDomainRepresentations
		AmplitudeTimeDomainRepresentation ampRp=new AmplitudeTimeDomainRepresentation(wave);
		FrequencyTimeDomainRepresentation freqRp=new FrequencyTimeDomainRepresentation(wave);
		
		// get the amplitude
		double[] amplitudes=ampRp.getNormalizedAmplitudes();
				
		// get the spectrogram
		double[][] spectrogram=freqRp.getSpectrogram();
		
		// Graphic render
		GraphicRender render=new GraphicRender();
		//render.setHorizontalMarker(1);
		//render.setVerticalMarker(1);
		render.renderWaveform(ampRp, outFolder+"/waveform.jpg");
		render.renderSpectrogram(freqRp, outFolder+"/spectrogram.jpg");
		
		// change the amplitude representation
		ampRp.setTimeStep(0.1F);
		render.renderWaveform(ampRp, outFolder+"/waveform2.jpg");

		// change the spectrogram representation
		freqRp.setFftSampleSize(512);
		freqRp.setOverlapFactor(2);
		render.renderSpectrogram(freqRp, outFolder+"/spectrogram2.jpg");
		
		// trim the wav
		wave.leftTrim(1);
		wave.rightTrim(0.5F);

		// save the trimmed wav
		wave.saveAs(outFolder+"/out.wav");
	}
}