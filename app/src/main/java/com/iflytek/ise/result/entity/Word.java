/**
 * 
 */
package com.iflytek.ise.result.entity;

import java.util.ArrayList;

/**
 * <p>Title: Word</p>
 * <p>Description: Word, corresponds to the word label in the xml</p>
 * <p>Company: www.iflytek.com</p>
 * @author iflytek
 * @date Jan 12, 2015 15:29:30
 */
public class Word {
	/**
	 * The position of the beginning frame, each frame is equivalent to 10ms
	 */
	public int beg_pos;
	/**
	 * The position of ending frame
	 */
	public int end_pos;
	/**
	 * Word content
	 */
	public String content;
	/**
	 * Adds/Misses information: 0(Correct), 16(Miss), 32(Add), 64(Readback), 128(Replace)
	 */
	public int dp_message;
	/**
	 * The index of word in full text(en)
	 */
	public int global_index;
	/**
	 * The index of word in sentence (en)
	 */
	public int index;
	/**
	 * Pinyin (cn), the number indicates tone, 5 indicates neutral tone, such as fen1
	 */
	public String symbol;
	/**
	 * Duration (Unit: frame, each frame is equivalent to 10ms) (cn)
	 */
	public int time_len;
	/**
	 * The word score (en)
	 */
	public float total_score;
	/**
	 * Syll included in word
	 */
	public ArrayList<Syll> sylls;
	
}
