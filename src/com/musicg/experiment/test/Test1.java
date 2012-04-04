package com.musicg.experiment.test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import com.musicg.graphic.GraphicRender;
import com.musicg.math.rank.ArrayRankDouble;
import com.musicg.math.statistics.SpectralCentroid;
import com.musicg.math.statistics.StandardDeviation;
import com.musicg.math.statistics.ZeroCrossingRate;
import com.musicg.pitch.PitchHandler;
import com.musicg.representation.timedomain.AmplitudeTimeDomainRepresentation;
import com.musicg.representation.timedomain.FrequencyTimeDomainRepresentation;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveInputStream;

public class Test1 {

	public static void main(String[] args) {		
		
		String filename = "audio_work/sound3.wav";

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

		// TimeDomainRepresentations
		FrequencyTimeDomainRepresentation freqRp=new FrequencyTimeDomainRepresentation(wave);
		AmplitudeTimeDomainRepresentation ampRp=new AmplitudeTimeDomainRepresentation(wave);
		freqRp.setFftSampleSize(1024);
		freqRp.setOverlapFactor(1);
		
		int fps=freqRp.getFramesPerSecond();

		// set boundary
		int highPass=100;
		int lowerBoundary=(int)(highPass/freqRp.getUnitFrequency());
		int lowPass=4000;
		int upperBoundary=(int)(lowPass/freqRp.getUnitFrequency());
		// end set boundary
		
		double[][] spectrogram=freqRp.getSpectrogram();
		double[][] absoluteSpectrogram=freqRp.getAbsoluteSpectrogram();
		double[][] boundedspectrogram=new double[spectrogram.length][];
		
		SpectralCentroid sc=new SpectralCentroid();
		StandardDeviation sd=new StandardDeviation();		
		ArrayRankDouble arrayRankDouble=new ArrayRankDouble();
		double unitFrequency=freqRp.getUnitFrequency();
		
		// zrc
		short[] amps=ampRp.getAmplitudes();
		int numFrame=amps.length/1024;
		double[] zcrs=new double[numFrame];
		
		for (int i=0; i<numFrame; i++){
			short[] temp=new short[1024];
			System.arraycopy(amps, i*1024, temp, 0, temp.length);
			
			int numZC=0;
			int size=temp.length;
			
			for (int j=0; j<size-1; j++){
				if((temp[j]>=0 && temp[j+1]<0) || (temp[j]<0 && temp[j+1]>=0)){
					numZC++;
				}
			}	
			
			zcrs[i]=numZC;
		}
		
		// end zcr
		
		for (int i=0; i<spectrogram.length; i++){
			double[] temp=new double[upperBoundary-lowerBoundary+1];
			System.arraycopy(spectrogram[i], lowerBoundary, temp, 0, temp.length);			
			
			int maxIndex=arrayRankDouble.getMaxValueIndex(temp);			
			//sc.setValues(temp);
			sd.setValues(temp);			
			double sdValue=sd.evaluate();
			
			System.out.println(i+" "+(double)i/fps+"s\t"+maxIndex+"\t"+sdValue+"\t"+zcrs[i]);
			boundedspectrogram[i]=temp;
		}
		
		// Graphic render		
		GraphicRender render=new GraphicRender();
		render.setHorizontalMarker(61);
		render.setVerticalMarker(200);
		render.renderSpectrogram(boundedspectrogram, filename+".jpg");
		
		PitchHandler ph=new PitchHandler();

		for (int frame=0; frame<absoluteSpectrogram.length; frame++){
			
			System.out.print("frame "+frame+": ");
						
			double[] temp=new double[upperBoundary-lowerBoundary+1];
			sd.setValues(temp);
			double sdValue=sd.evaluate();
			double passSd=0.1;
			
			if (sdValue<passSd){
				System.arraycopy(spectrogram[frame], lowerBoundary, temp, 0, temp.length);
				double maxFrequency=arrayRankDouble.getMaxValueIndex(temp)*unitFrequency;
				
				double passFrequency=400;
				int numRobust=2;
				
				double[] robustFrequencies=new double[numRobust];
				double nthValue=arrayRankDouble.getNthOrderedValue(temp, numRobust, false);
				int count=0;
				for (int b=lowerBoundary; b<=upperBoundary; b++){
					if (spectrogram[frame][b]>=nthValue){
						robustFrequencies[count++]=b*unitFrequency;
						if (count>=numRobust){
							break;
						}
					}
				}
				
				double passIntensity=1000;
				double intensity=0;
				for (int i=0; i<absoluteSpectrogram[frame].length; i++){
					intensity+=absoluteSpectrogram[frame][i];
				}
				intensity/=absoluteSpectrogram[frame].length;
				System.out.print(" intensity: "+intensity+" pitch: "+maxFrequency);
				if (intensity>passIntensity && maxFrequency>passFrequency){				
					double p=ph.getHarmonicProbability(robustFrequencies);				
					System.out.print(" P: "+p);
				}
			}
			System.out.print(" zcr:"+zcrs[frame]);
			System.out.println();
		}
	}
}