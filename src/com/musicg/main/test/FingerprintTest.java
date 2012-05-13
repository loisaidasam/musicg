package com.musicg.main.test;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.musicg.dsp.Resampler;
import com.musicg.fingerprint.FingerprintManager;
import com.musicg.fingerprint.PairManager;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;

public class FingerprintTest{
	
	public static void main (String[] args){
		
		//String filename = "audio_work/cock_a_1.wav";
		String filename = "audio_work/cock_a_1_cut_first_1s.wav";

		// create a wave object
		Wave wave = new Wave(filename);
		byte[] fingerprint=wave.getFingerprint();
		wave.getFingerprint();

		PairManager pm=new PairManager();
		HashMap<Integer,List<Integer>> map=pm.getPair_PositionList_Table(fingerprint);
		
		Iterator<Integer> mapIterator=map.keySet().iterator();
		while (mapIterator.hasNext()){
			int hashNumber=mapIterator.next();
			System.out.print(hashNumber+": ");
			List<Integer> positionList=map.get(hashNumber);
			Iterator<Integer> positionListIterator=positionList.iterator();
			while (positionListIterator.hasNext()){
				int position=positionListIterator.next();
				System.out.print(" "+position);
			}
			System.out.println();
		}
	}
}