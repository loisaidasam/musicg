package com.musicg.main.test;

import com.musicg.dsp.Resampler;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveFileManager;
import com.musicg.wave.WaveHeader;
public class resample
{
    public static void main( String[] args )
    {
    	String filename = "audio_work/jac_whistle2.wav";
    	Wave wave=new Wave(filename);

    	// resample to 
		Resampler resampler=new Resampler();
		int sourceRate = wave.getWaveHeader().getSampleRate();
        int targetRate = 10240;
       	byte[] resampledWaveData=resampler.reSample(wave.getBytes(), wave.getWaveHeader().getBitsPerSample(), sourceRate, targetRate);
		
        // update the wave header
        WaveHeader resampledWaveHeader=wave.getWaveHeader();
        resampledWaveHeader.updateSampleRate(targetRate);
        
        // make resampled wave
        Wave resampledWave=new Wave(resampledWaveHeader,resampledWaveData);
        
        System.out.println(resampledWave);
        
        WaveFileManager wfm=new WaveFileManager(resampledWave);
        wfm.saveWaveAsFile("out/jac_whistle2_10240.wav");
    }
    
}