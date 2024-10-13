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

	/**
	 * Compute stats
	 * @return
	 */
	public AudioStats computeStats() {
		int numSamples = samples.length;
		float min = Float.MAX_VALUE;
		float max = -Float.MAX_VALUE;
		for (int i=0; i<numSamples; i++) {
			min = Math.min(min, samples[i]);
			max = Math.max(max, samples[i]);
		}
		return new AudioStats(min,max);
	}

	public double getSampleRate() {
		return sampleRate;
	}

	public float[] getSamples() {
		return samples;
	}

	public int getChannels() {
		return channels;
	}
}
