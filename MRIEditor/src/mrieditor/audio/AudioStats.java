/**
 * 
 */
package mrieditor.audio;

/**
 * 
 */
public class AudioStats {

	private float min;
	
	private float max;
	
	public AudioStats(float min, float max) {
		this.min = min;
		this.max = max;
	}

	public float getMin() {
		return min;
	}

	public float getMax() {
		return max;
	}
}
