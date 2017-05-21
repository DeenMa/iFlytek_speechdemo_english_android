/**
 * 
 */
package com.iflytek.ise.result.entity;

import java.util.ArrayList;

/**
 * <p>Title: Sentence</p>
 * <p>Description: sentence, corresponds to the sentence label in the xml result</p>
 * <p>Company: www.iflytek.com</p>
 * @author iflytek
 * @date Jan 12, 2015 16:10:09
 */
public class Sentence {
	/**
	 * The position of the beginning frame,  each frame is equivalent to 10ms
	 */
	public int beg_pos;
	/**
	 * The position of the ending frame
	 */
	public int end_pos;
	/**
	 * Sentence content
	 */
	public String content;
	/**
	 * Total score
	 */
	public float total_score;
	/**
	 * Duration (Unit: Frame, each frame is equivalent to 10ms) (cn)
	 */
	public int time_len;
	/**
	 * The index of sentence (en)
	 */
	public int index;
	/**
	 * The number of words (en)
	 */
	public int word_count;
	/**
	 * words included in sentence
	 */
	public ArrayList<Word> words;
}
