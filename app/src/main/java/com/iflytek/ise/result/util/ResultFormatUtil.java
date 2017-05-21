/**
 * 
 */
package com.iflytek.ise.result.util;

import java.util.ArrayList;

import com.iflytek.ise.result.entity.Phone;
import com.iflytek.ise.result.entity.Sentence;
import com.iflytek.ise.result.entity.Syll;
import com.iflytek.ise.result.entity.Word;

/**
 * <p>Title: ResultFormatUtl</p>
 * <p>Description: </p>
 * <p>Company: www.iflytek.com</p>
 * @author iflytek
 * @date Jan 19, 2015 10:01:14
 */
public class ResultFormatUtil {
	
	/**
	 * Outputs the English evaluation detail by format
	 * 
	 * @param sentences
	 * @return English evaluation details
	 */
	public static String formatDetails_EN(ArrayList<Sentence> sentences) {
		StringBuffer buffer = new StringBuffer();
		if (null == sentences) {
			return buffer.toString();
		}
		
		for (Sentence sentence: sentences ) {
			if ("Noise".equals(ResultTranslateUtil.getContent(sentence.content))
					|| "Mute".equals(ResultTranslateUtil.getContent(sentence.content))) {
				continue;
			}
			
			if (null == sentence.words) {
				continue;
			}
			for (Word word: sentence.words) {
				if ("Noise".equals(ResultTranslateUtil.getContent(word.content))
						|| "Mute".equals(ResultTranslateUtil.getContent(word.content))) {
					continue;
				}
				
				buffer.append("\nWord[" + ResultTranslateUtil.getContent(word.content) + "] ")
					.append("Read: " + ResultTranslateUtil.getDpMessageInfo(word.dp_message))
					.append(" Score: " + word.total_score);
				if (null == word.sylls) {
					buffer.append("\n");
					continue;
				}
				
				for (Syll syll: word.sylls) {
					buffer.append("\n└Syllables[" + ResultTranslateUtil.getContent(syll.getStdSymbol()) + "] ");
					if (null == syll.phones) {
						continue;
					}
					
					for (Phone phone: syll.phones) {
						buffer.append("\n\t└Phoneme[" + ResultTranslateUtil.getContent(phone.getStdSymbol()) + "] ")
							.append(" Read: " + ResultTranslateUtil.getDpMessageInfo(phone.dp_message));
					}
					
				}
				buffer.append("\n");
			}
		}
		
		return buffer.toString();
	}

	/**
	 * Outputs the Chinese evaluation details by format
	 * 
	 * @param sentences
	 * @return Chinese evaluation details
	 */
	public static String formatDetails_CN(ArrayList<Sentence> sentences) {
		StringBuffer buffer = new StringBuffer();
		if (null == sentences) {
			return buffer.toString();
		}
		
		for (Sentence sentence: sentences ) {
			if (null == sentence.words) {
				continue;
			}
			
			for (Word word: sentence.words) {
				buffer.append("\nWord[" + ResultTranslateUtil.getContent(word.content) + "] " + word.symbol + " 时长：" + word.time_len);
				if (null == word.sylls) {
					continue;
				}
				
				for (Syll syll: word.sylls) {
					if ("Noice".equals(ResultTranslateUtil.getContent(syll.content))
							|| "Mute".equals(ResultTranslateUtil.getContent(syll.content))) {
						continue;
					}
					
					buffer.append("\n└Syllables[" + ResultTranslateUtil.getContent(syll.content) + "] " + syll.symbol + " 时长：" + syll.time_len);
					if (null == syll.phones) {
						continue;
					}
					
					for (Phone phone: syll.phones) {
						buffer.append("\n\t└Phoneme[" + ResultTranslateUtil.getContent(phone.content) + "] " + "时长：" + phone.time_len)
							.append(" Word: " + ResultTranslateUtil.getDpMessageInfo(phone.dp_message));
					}
					
				}
				buffer.append("\n");
			}
		}
		
		return buffer.toString();
	}
}
