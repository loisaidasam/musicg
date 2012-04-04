package com.musicg.graphic;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import com.musicg.representation.timedomain.AmplitudeTimeDomainRepresentation;
import com.musicg.representation.timedomain.FrequencyTimeDomainRepresentation;

public class GraphicRender{
	
	private int xMarker=-1;
	private int yMarker=-1;
	
	public GraphicRender(){		
	}
	
	/**
	 * Render a waveform of a wave file
	 * 
	 * @param ampRp	amplitude against time representation
	 * @param filename	output file
	 * @see	RGB graphic rendered
	 */
	public void renderWaveform(AmplitudeTimeDomainRepresentation ampRp, String filename) {

		double[] nAmplitudes = ampRp.getNormalizedAmplitudes();
		int width = (int) (nAmplitudes.length / ampRp.getWave().getSampleRate() / ampRp.getTimeStep());
		int height = 500;
		int middle = height / 2;
		int magnifier = 1000;

		int numSamples = nAmplitudes.length;
		int numSamplePerTimeFrame = numSamples / width;

		int[] scaledPosAmplitudes = new int[width];
		int[] scaledNegAmplitudes = new int[width];

		// width scaling
		for (int i = 0; i < width; i++) {
			double sumPosAmplitude = 0;
			double sumNegAmplitude = 0;
			int startSample=i * numSamplePerTimeFrame;
			for (int j = 0; j < numSamplePerTimeFrame; j++) {
				double a = nAmplitudes[startSample + j];
				if (a > 0) {
					sumPosAmplitude += a;
				} else {
					sumNegAmplitude += a;
				}
			}

			int scaledPosAmplitude = (int) (sumPosAmplitude
					/ numSamplePerTimeFrame * magnifier + middle);
			int scaledNegAmplitude = (int) (sumNegAmplitude
					/ numSamplePerTimeFrame * magnifier + middle);

			scaledPosAmplitudes[i] = scaledPosAmplitude;
			scaledNegAmplitudes[i] = scaledNegAmplitude;
		}

		// render wave form image
		BufferedImage bufferedImage = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);

		// set default white background
		Graphics2D graphics = bufferedImage.createGraphics();
		graphics.setPaint(new Color(255, 255, 255));
		graphics.fillRect(0, 0, bufferedImage.getWidth(),
				bufferedImage.getHeight());
		// end set default white background

		for (int i = 0; i < width; i++) {
			for (int j = scaledNegAmplitudes[i]; j < scaledPosAmplitudes[i]; j++) {
				int y = height - j;
				if (y < 0) {
					y = 0;
				} else if (y >= height) {
					y = height - 1;
				}
				bufferedImage.setRGB(i, y, 0);
			}
		}
		// end render wave form image

		// export image
		try {
			int dotPos = filename.lastIndexOf(".");
			String extension=filename.substring(dotPos + 1);
			ImageIO.write(bufferedImage, extension,
					new File(filename));
		} catch (IOException e) {
			e.printStackTrace();
		}
		// end export image
	}
	
	/**
	 * Set the vertical marker
	 * 
	 * @param x	x-offset pixel of the marker
	 */
	public void setVerticalMarker(int x){
		this.xMarker=x;
	}
	
	/**
	 * Set the horizontal marker
	 * 
	 * @param y	y-offset pixel of the marker
	 */
	public void setHorizontalMarker(int y){
		this.yMarker=y;
	}
	
	/**
	 * Reset the markers
	 */
	public void resetMarkers(){
		xMarker=-1;
		yMarker=-1;
	}
	
	/**
	 * 
	 * Render a spectrogram of a wave file
	 * 	 
	 * @param freqRp	frequency against time representation
	 * @param filename	output file
	 * @see	RGB graphic rendered
	 */
	public void renderSpectrogram(FrequencyTimeDomainRepresentation freqRp, String filename){
		freqRp.buildSpectrogram();
		renderSpectrogram(freqRp.getSpectrogram(),filename);
	}
	
	/**
	 * 
	 * Render a spectrogram of a wave file
	 * 	 
	 * @param spectrogram	spectrogram[time][frequency]=intensity, which time is the x-axis, frequency is the y-axis, intensity is the color darkness
	 * @param filename	output file
	 * @see	RGB graphic rendered
	 */
	public void renderSpectrogram(double[][] spectrogram, String filename) {
		
		int width=spectrogram.length;
		int height=spectrogram[0].length;
		
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int i=0; i<width; i++){
			if (i==xMarker){
				for (int j=0; j<height; j++){
					bufferedImage.setRGB(i, j, 65280);	// green
				}
			}
			else{
				for (int j=0; j<height; j++){
					int value;
					if (j==yMarker){
						value=16711680;	// red
					}
					else{
						value=255-(int)(spectrogram[i][j]*255);
					}
					bufferedImage.setRGB(i, height-1-j, value<<16|value<<8|value);
				}
			}
		}
				
		try {
			int dotPos = filename.lastIndexOf(".");
			String extension=filename.substring(dotPos + 1);
			ImageIO.write(bufferedImage, extension, new File(filename));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}