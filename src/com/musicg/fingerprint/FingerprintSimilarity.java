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

import com.musicg.properties.FingerprintProperties;

public class FingerprintSimilarity {
	
	private FingerprintProperties fingerprintProperties=FingerprintProperties.getInstance();
	private int mostSimilarFramePosition;
	private float score;
	private float similarity;

	public FingerprintSimilarity() {
		mostSimilarFramePosition = Integer.MIN_VALUE;
		score=-1;
		similarity = -1;
	}

	public int getMostSimilarFramePosition() {
		return mostSimilarFramePosition;
	}

	public void setMostSimilarFramePosition(int mostSimilarFramePosition) {
		this.mostSimilarFramePosition = mostSimilarFramePosition;
	}

	public float getSimilarity() {
		return similarity;
	}

	public void setScore(float score) {
		this.score = score;
	}
	
	public float getScore() {
		return score;
	}

	public void setSimilarity(float similarity) {
		this.similarity = similarity;
	}
	
	public float getsetMostSimilarTimePosition(){
		return (float)mostSimilarFramePosition/fingerprintProperties.getNumRobustPointsPerFrame()/fingerprintProperties.getFps();
	}
}