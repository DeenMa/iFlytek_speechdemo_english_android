/**
 * 
 */
package com.iflytek.ise.result;

import java.util.ArrayList;

import com.iflytek.ise.result.entity.Sentence;

/**
 * <p>Title: Result</p>
 * <p>Description: 评测结果</p>
 * <p>Company: www.iflytek.com</p>
 * @author iflytek
 * @date 2015年1月12日 下午4:58:38
 */
public class Result {
	/**
	 * Evaluation language：en (English), cn (Chinese)
	 */
	public String language;
	/**
	 * Evaluation type：read_syllable（syllable）、read_word（phrase）、read_sentence（sentence）
	 */
	public String category;
	/**
	 * The position of the beginning frame,  each frame is equivalent to 10ms
	 */
	public int beg_pos;
	/**
	 * The position of ending frame
	 */
	public int end_pos;
	/**
	 * Evaluation content
	 */
	public String content;
	/**
	 * Total score
	 */
	public float total_score;
	/**
	 * Duration (cn)
	 */
	public int time_len;
	/**
	 * Exception information (en)
	 */
	public String except_info;
	/**
	 * Whether read in chaos (cn)
	 */
	public boolean is_rejected;
	 /**
	 * the sentence label in the xml result
	 */
	public ArrayList<Sentence> sentences;
}
