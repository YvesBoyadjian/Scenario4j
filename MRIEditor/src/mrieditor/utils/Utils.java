/**
 * 
 */
package mrieditor.utils;

/**
 * 
 */
public class Utils {

	public static final String formatCentimeter(float value) {
		float rounded = (float)Math.round(value * 100)/100f;
		return Float.toString(rounded);
	}
}
