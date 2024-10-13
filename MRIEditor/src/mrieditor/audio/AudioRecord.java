/**
 * 
 */
package mrieditor.audio;

/**
 * 
 */
public class AudioRecord {
	private int channels;
	private float sampleRate;
	private float [] samples;
	
	public AudioRecord(int channels, float sampleRate, float[] samples) {
		this.channels = channels;
		this.sampleRate = sampleRate;
		this.samples = samples;
	}
}
