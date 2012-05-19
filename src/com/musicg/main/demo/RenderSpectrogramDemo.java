package com.musicg.main.demo;

import com.musicg.graphic.GraphicRender;
import com.musicg.wave.Wave;
import com.musicg.wave.extension.Spectrogram;

public class RenderSpectrogramDemo {
	public static void main(String[] args) {

		String filename = "audio_work/cock_a_1.wav";
		String outFolder = "out";

		// create a wave object
		Wave wave = new Wave(filename);
		Spectrogram spectrogram = new Spectrogram(wave);

		// Graphic render
		GraphicRender render = new GraphicRender();
		// render.setHorizontalMarker(1);
		// render.setVerticalMarker(1);
		render.renderSpectrogram(spectrogram, outFolder + "/spectrogram.jpg");

		// change the spectrogram representation
		int fftSampleSize = 512;
		int overlapFactor = 2;
		spectrogram = new Spectrogram(wave, fftSampleSize, overlapFactor);
		render.renderSpectrogram(spectrogram, outFolder + "/spectrogram2.jpg");
	}
}