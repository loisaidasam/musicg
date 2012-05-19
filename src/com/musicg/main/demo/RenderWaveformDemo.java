package com.musicg.main.demo;

import com.musicg.graphic.GraphicRender;
import com.musicg.wave.Wave;

public class RenderWaveformDemo {
	public static void main(String[] args) {

		String filename = "cock_a_1.wav";
		String outFolder = "out";

		// create a wave object
		Wave wave = new Wave(filename);
	
		// Graphic render
		GraphicRender render=new GraphicRender();
		//render.setHorizontalMarker(1);
		//render.setVerticalMarker(1);
		render.renderWaveform(wave, outFolder+"/waveform.jpg");
		
		// change the amplitude representation
		float timeStep=0.1F;
		render.renderWaveform(wave,timeStep,outFolder+"/waveform2.jpg");
	}
}