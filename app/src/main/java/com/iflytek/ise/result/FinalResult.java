/**
 * 
 */
package com.iflytek.ise.result;

/**
 * <p>Title: FinalResult</p>
 * <p>Description: </p>
 * <p>Company: www.iflytek.com</p>
 * @author iflytek
 * @date Jan 14, 2015 11:12:58
 */
public class FinalResult extends Result {
	
	public int ret;
	
	public float total_score;
	
	@Override
	public String toString() {
		return "Returned value: " + ret + ", total score: " + total_score;
	}
}
