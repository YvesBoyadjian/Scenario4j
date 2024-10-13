/**
 * 
 */
package mrieditor.audio;

/**
 * 
 */
public class AudioProcessor {

	AudioRecord inRecord;
	
	AudioRecord outRecord;
	
	public AudioProcessor(AudioRecord audioRecord) {
		inRecord = audioRecord;
	}
	
	public AudioRecord enhanceBass() {
		
		processInRecord();
		
		return outRecord;
	}
	
	private void processInRecord() {
		
		float[] inSamples = inRecord.getSamples();
		
		int length = inSamples.length;
		
		float[] outSamples = new float[length];
		
		int numChannels = inRecord.getChannels();
		
		int numSamples = length / numChannels;
		
		final double[] integral = new double[numChannels];
		
		float sampleRate = inRecord.getSampleRate();
		
		float bassLimitFrequency = 5;
		
		float enhanceFrequency = 1000;
		
		final double multiplicationFactor = (1d - bassLimitFrequency * 2 / sampleRate);
		
		for (int sampleIndice = 0; sampleIndice<numSamples; sampleIndice++) {		
			for (int channel=0; channel<numChannels; channel++) {
				integral[channel] += inSamples[sampleIndice*numChannels+channel]; 
				integral[channel] *= multiplicationFactor;
				outSamples[sampleIndice*numChannels+channel] = (float)(inSamples[sampleIndice*numChannels+channel] + enhanceFrequency * 2 * integral[channel] / sampleRate); 
			}
		}
		
		
		outRecord = new AudioRecord(inRecord.getChannels(),inRecord.getSampleRate(), outSamples);
	}

	public AudioRecord normalize() {
		
		float[] inSamples = inRecord.getSamples();
		
		int length = inSamples.length;
		
		float[] outSamples = new float[length];
		
		AudioStats audioStats = inRecord.computeStats();
		
		float max = audioStats.getMax();
		
		float min = audioStats.getMin();
		
		float maxAbs = Math.max(Math.abs(max), Math.abs(min));
		
		System.err.println("Max Abs: "+ maxAbs);
		
		maxAbs = Math.max(1, maxAbs);
		
		for (int i=0; i< length; i++) {
			outSamples[i] = inSamples[i] / maxAbs;
		}
		
		outRecord = new AudioRecord(inRecord.getChannels(),inRecord.getSampleRate(), outSamples);
		
		return outRecord;
	}
}
