/*
 * Copyright (C) 2012 Jacquet Wong
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.musicg.fingerprint;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.musicg.math.rank.MapRank;
import com.musicg.math.rank.MapRankInteger;

/**
 * Compute the similarity of two fingerprints
 * 
 * @author jacquet
 *
 */
public class FingerprintSimilarityComputer{
	
	private FingerprintSimilarity fingerprintSimilarity;
	byte[] fingerprint1, fingerprint2;
	
	/**
	 * Constructor, ready to compute the similarity of two fingerprints
	 * 
	 * @param fingerprint1
	 * @param fingerprint2
	 */
	public FingerprintSimilarityComputer(byte[] fingerprint1, byte[] fingerprint2){
		
		this.fingerprint1=fingerprint1;
		this.fingerprint2=fingerprint2;
		
		fingerprintSimilarity=new FingerprintSimilarity();
	}

	/**
	 * Get fingerprint similarity of inout fingerprints
	 * 
	 * @return fingerprint similarity object
	 */
	public FingerprintSimilarity getFingerprintsSimilarity(){
		HashMap<Integer,Integer> offset_Score_Table=new HashMap<Integer,Integer>();	// offset_Score_Table<offset,count>
		int numFrames=0;
		float score=0;
		int mostSimilarFramePosition=Integer.MIN_VALUE;
		
		// one frame may contain several points, use the shorter one be the denominator
		if (fingerprint1.length>fingerprint2.length){
			numFrames=FingerprintManager.getNumFrames(fingerprint2);	
		}
		else{
			numFrames=FingerprintManager.getNumFrames(fingerprint1);
		}
		
		// get the pairs
		PairManager pairManager=new PairManager();
		HashMap<Integer,List<Integer>> this_Pair_PositionList_Table=pairManager.getPair_PositionList_Table(fingerprint1);
		HashMap<Integer,List<Integer>> compareWave_Pair_PositionList_Table=pairManager.getPair_PositionList_Table(fingerprint2);
		
		Iterator<Integer> compareWaveHashNumberIterator=compareWave_Pair_PositionList_Table.keySet().iterator();
		while (compareWaveHashNumberIterator.hasNext()){
			int compareWaveHashNumber=compareWaveHashNumberIterator.next();
			
			// if the compareWaveHashNumber doesn't exist in both tables, no need to compare
			if (!this_Pair_PositionList_Table.containsKey(compareWaveHashNumber)
				|| !compareWave_Pair_PositionList_Table.containsKey(compareWaveHashNumber)){
				continue;
			}
			
			// for each compare hash number, get the positions
			List<Integer> wavePositionList=this_Pair_PositionList_Table.get(compareWaveHashNumber);
			List<Integer> compareWavePositionList=compareWave_Pair_PositionList_Table.get(compareWaveHashNumber);
			
			Iterator<Integer> wavePositionListIterator=wavePositionList.iterator();
			while (wavePositionListIterator.hasNext()){
				int thisPosition=wavePositionListIterator.next();
				Iterator<Integer> compareWavePositionListIterator=compareWavePositionList.iterator();
				while (compareWavePositionListIterator.hasNext()){
					int compareWavePosition=compareWavePositionListIterator.next();
					int offset=thisPosition-compareWavePosition;
					
					if (offset_Score_Table.containsKey(offset)){
						offset_Score_Table.put(offset, offset_Score_Table.get(offset)+1);
					}
					else{
						offset_Score_Table.put(offset, 1);
					}
				}
			}
		}
		
		// map rank
		MapRank mapRank=new MapRankInteger(offset_Score_Table,false);
		
		// get the most similar positions and scores	
		List<Integer> orderedKeyList=mapRank.getOrderedKeyList(100, true);
		if (orderedKeyList.size()>0){
			int key=orderedKeyList.get(0);
			// get the highest score position
			if (mostSimilarFramePosition==Integer.MIN_VALUE){
				mostSimilarFramePosition=key;
				score=offset_Score_Table.get(key);
				
				// accumulate the scores from neighbours
				if (offset_Score_Table.containsKey(key-1)){
					score+=offset_Score_Table.get(key-1)/2;
				}
				if (offset_Score_Table.containsKey(key+1)){
					score+=offset_Score_Table.get(key+1)/2;
				}
			}
		}
		
		/*
		Iterator<Integer> orderedKeyListIterator=orderedKeyList.iterator();
		while (orderedKeyListIterator.hasNext()){
			int offset=orderedKeyListIterator.next();					
			System.out.println(offset+": "+offset_Score_Table.get(offset));
		}
		*/
		
		score/=numFrames;
		float similarity=score;
		// similarity >1 means in average there is at least one match in every frame
		if (similarity>1){
			similarity=1;
		}
		
		fingerprintSimilarity.setMostSimilarFramePosition(mostSimilarFramePosition);
		fingerprintSimilarity.setScore(score);
		fingerprintSimilarity.setSimilarity(similarity);
		
		return fingerprintSimilarity;
	}
}