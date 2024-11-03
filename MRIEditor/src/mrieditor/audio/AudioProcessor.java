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
		
		float[] outSamples2 = new float[length];
		
//		float[] outSamples3 = new float[length];
		
		int numChannels = inRecord.getChannels();
		
		int numSamples = length / numChannels;
		
		float sampleRate = inRecord.getSampleRate();

		double f1 = 120; // Enhance frequency Hz for high pass filter
		
		double f2 = 70; // Enhance frequency Hz for KM 184
		
		filter3(sampleRate, numSamples, numChannels, inSamples, outSamples, f1);
		filter3(sampleRate, numSamples, numChannels, outSamples, outSamples2, f2);
		//filter3(sampleRate, numSamples, numChannels, outSamples2, outSamples3, f2);

		
//		double f0 = 5;
//		
//		lowPassFirstOrder(sampleRate, numSamples, numChannels, inSamples, outSamples, f0);
//		lowPassFirstOrder(sampleRate, numSamples, numChannels, outSamples, outSamples2, f0);
//		lowPassFirstOrder(sampleRate, numSamples, numChannels, outSamples2, outSamples3, f0);
//		
//		double f1 = 160;
//		
//		double ammplificationFactor = Math.pow(f1/f0,3); // 18 dB/Octave
//		
//		for ( int i=0; i < length; i++) {
//			outSamples3[i] = (float)(outSamples3[i] * ammplificationFactor + inSamples[i]);
//		}
		
		outRecord = new AudioRecord(inRecord.getChannels(),inRecord.getSampleRate(), outSamples2);
	}
	
	private void filter(float sampleRate, int numSamples, int numChannels, float[] inSamples, float[] outSamples) {
		
		final double[] integral = new double[numChannels];
		
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
		
	}
	
	private void lowPassFirstOrder(float sampleRate, int numSamples, int numChannels, float[] inSamples, float[] outSamples, double f0) {
		
		double c = 1;
		
		double r = 1d/2d/Math.PI/f0/c;
		
		final double[] integral = new double[numChannels];		
		
		for (int sampleIndice = 0; sampleIndice<numSamples; sampleIndice++) {
			for (int channel=0; channel<numChannels; channel++) {
				
				double e = inSamples[sampleIndice*numChannels+channel];
				double s = 1d/r/c*integral[channel];
				outSamples[sampleIndice*numChannels+channel] = (float)(s);
				
				integral[channel] += (e - s)/sampleRate;
			}
		}
	}
	
	/**
	 * https://courtincpge.net/mainPhysics/cours_PSI/lectures/ElecFiltrage/TD/TD_electronique_B1.pdf
	 * 
	 * Treble enhance filter
	 * 
	 * @param sampleRate
	 * @param numSamples
	 * @param numChannels
	 * @param inSamples
	 * @param outSamples
	 */
	private void filter2(float sampleRate, int numSamples, int numChannels, float[] inSamples, float[] outSamples) {
		
		double f0 = 5; // Bass limit frequency Hz
		double f1 = 160; // Enhance frequency Hz
		double c = 1; // Capacitor Farad As/V
		double r1 = 1d/2d/Math.PI/f1/c; // Grounded resistor, in serie with c
		double r2 = 1d/2d/Math.PI/f0/c - r1; // Counter-reaction reactor
		
		final double[] currentIntegral = new double[numChannels];		
		
		for (int sampleIndice = 0; sampleIndice<numSamples; sampleIndice++) {
			for (int channel=0; channel<numChannels; channel++) {
				
				double e = inSamples[sampleIndice*numChannels+channel];				
				
				double current = e/r1 - 1d/r1/c*currentIntegral[channel];
				currentIntegral[channel] += current/sampleRate;
				
				outSamples[sampleIndice*numChannels+channel] = (float)(inSamples[sampleIndice*numChannels+channel] + current * r2); 
			}			
		}
	}
	
	/**
	 * https://courtincpge.net/mainPhysics/cours_PSI/lectures/ElecFiltrage/TD/TD_electronique_B1.pdf
	 * 
	 * Bass enhanced filter
	 * 
	 * @param sampleRate
	 * @param numSamples
	 * @param numChannels
	 * @param inSamples
	 * @param outSamples
	 */
	private void filter3(float sampleRate, int numSamples, int numChannels, float[] inSamples, float[] outSamples, double f1) {
		
		double f0 = 5; // Bass limit frequency Hz
		double c = 1; // Capacitor Farad As/V
		double r1 = 1d/2d/Math.PI/f1/c; // Grounded resistor
		double r2 = 1d/2d/Math.PI/f0/c - r1; // Counter-reaction reactor, in parallel with c
		
		final double[] integral = new double[numChannels];		
		
		for (int sampleIndice = 0; sampleIndice<numSamples; sampleIndice++) {
			for (int channel=0; channel<numChannels; channel++) {
				
				double e = inSamples[sampleIndice*numChannels+channel];				
				double s = e + 1d/c * integral[channel];
				outSamples[sampleIndice*numChannels+channel] = (float)s;
				
				integral[channel] += (e/r1 - (s-e)/r2)/sampleRate;
			}
		}
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
