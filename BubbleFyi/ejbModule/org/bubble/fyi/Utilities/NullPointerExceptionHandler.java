/**
 * 
 */
package org.bubble.fyi.Utilities;

/**
 * @author hafiz
 *
 */
public class NullPointerExceptionHandler {

	public static boolean isNullOrEmpty(String s) {
		if(s==null) return true;
		else if(s.isEmpty()) return true;
		else return false;
	}
	public static boolean isNullOrEmpty(Integer s) {
		if(s==null) return true;
		else return false;
	}
}
