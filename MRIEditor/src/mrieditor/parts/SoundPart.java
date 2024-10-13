 
package mrieditor.parts;

import jakarta.inject.Inject;
import mrieditor.audio.AudioRecord;
import mrieditor.audio.AudioStats;
import jakarta.annotation.PostConstruct;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Shell;

import jakarta.annotation.PreDestroy;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioFormat.Encoding;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;

public class SoundPart {
	@Inject
	public SoundPart() {
		
	}
	
	@PostConstruct
	public void postConstruct(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		Composite upperToolBar = new Composite(parent,SWT.NONE);
		upperToolBar.setLayout(new RowLayout());
		
		Button button = new Button(upperToolBar, SWT.PUSH);
		button.setText("Load Audio File");
		
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				loadAudioFile();				
			}
		});
		
	}
	
	
	@PreDestroy
	public void preDestroy() {
		
	}
	
	
	@Focus
	public void onFocus() {
		
	}
	
	
	@Persist
	public void save() {
		
	}
	
	private void loadAudioFile() {
		
		String[] extensions = new String[1];
		extensions[0] = "*.wav";
		
		Shell shell = Display.getCurrent().getActiveShell();
		FileDialog dialog = new FileDialog(shell);
		dialog.setFilterExtensions(extensions);
		String choosedFilePathString = dialog.open();
		if (choosedFilePathString == null) {
			return;
		}
		Path choosedPath = Path.of(choosedFilePathString);
		if (choosedPath == null) {
			return;
		}
		File choosedFile = choosedPath.toFile();
		if (choosedFile == null || !choosedFile.isFile()) {
			return;
		}
		
		AudioRecord audioRecord = doLoadAudioFile(choosedFile);
		if (audioRecord == null) {
			return;
		}
		
		AudioStats audioStats = audioRecord.computeStats();
		
		System.err.println("Min = " + audioStats.getMin());
		System.err.println("Max = " + audioStats.getMax());
		
		FileDialog saveDialog = new FileDialog(shell);
		saveDialog.setFilterExtensions(extensions);
		saveDialog.setOverwrite(true);
		
		String savedChoosedFilePathString = saveDialog.open();
		if (savedChoosedFilePathString == null) {
			return;
		}
		Path saveChoosedPath = Path.of(savedChoosedFilePathString);
		if (saveChoosedPath == null) {
			return;
		}
		File saveChoosedFile = saveChoosedPath.toFile();
		
		// File must not exist
		if (saveChoosedFile == null || saveChoosedFile.isDirectory() || saveChoosedFile.isFile()) {
			return;
		}
		
		doSaveAudioFile(saveChoosedFile, audioRecord);
	}

	// https://stackoverflow.com/questions/3297749/java-reading-manipulating-and-writing-wav-files
	// https://stackoverflow.com/questions/56049170/reading-24-bit-mono-pcm-in-java
	// https://hydrogenaud.io/index.php/topic,113371.0.html
	private AudioRecord doLoadAudioFile(File fileIn) {
		// constant holding the minimum value of a signed 24bit sample: -2^22.
		final int MIN_VALUE_24BIT = -2 << 22;

		// constant holding the maximum value a signed 24bit sample can have, 2^(22-1).
		final int MAX_VALUE_24BIT = -MIN_VALUE_24BIT-1;

		int totalFramesRead = 0;
		int sampleIndice = 0;
		// somePathName is a pre-existing string whose value was
		// based on a user selection.
		try {
		  AudioInputStream audioInputStream = 
		    AudioSystem.getAudioInputStream(fileIn);
		  
		  AudioFormat audioFormat = audioInputStream.getFormat();
		  
		  int bytesPerFrame = 
				  audioFormat.getFrameSize();
		    if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
		    // some audio formats may have unspecified frame size
		    // in that case we may read any amount of bytes
		    bytesPerFrame = 1;
		  } 
		    
		    long frameLength = audioInputStream.getFrameLength();
		    int channels = audioFormat.getChannels();
		    Encoding encoding = audioFormat.getEncoding();
		    float sampleRate = audioFormat.getSampleRate();
		    boolean bigEndian = audioFormat.isBigEndian();
		    int sampleSizeInBits = audioFormat.getSampleSizeInBits();
		    
		    float[] samples = new float[(int)frameLength*channels];
		    AudioRecord audioRecord = new AudioRecord(channels,sampleRate,samples);
		    
		    if (!bigEndian && sampleSizeInBits == 24 && channels == 2 && encoding.equals(Encoding.PCM_SIGNED)) {
		    	// good		    	
		    }
		    else {
		    	return null;
		    }
		    
		  // Set an arbitrary buffer size of 1024 frames.
		  int numBytes = 1024 * bytesPerFrame; 
		  byte[] audioBytes = new byte[numBytes];
		  try {
		    int numBytesRead = 0;
		    int numFramesRead = 0;
		    // Try to read numBytes bytes from the file.
		    while ((numBytesRead = 
		      audioInputStream.read(audioBytes)) != -1) {
		      // Calculate the number of frames actually read.
		      numFramesRead = numBytesRead / bytesPerFrame;
		      totalFramesRead += numFramesRead;
		      // Here, do something useful with the audio data that's 
		      // now in the audioBytes array...

		      for(int i=0; i< numFramesRead; i++) {
		      final int bytesPerSample = 3; // because 24 / 8 = 3

		   // read one sample:
		   int sampleL = 0;
		   for (int byteIndex = 0; byteIndex < bytesPerSample; byteIndex++) {
		       final int aByte = audioBytes[byteIndex + i*bytesPerFrame] & 0xff;
		       sampleL += aByte << 8 * (byteIndex);
		   }

		   // now handle the sign / valid range
		   final int threeByteSampleL = sampleL > MAX_VALUE_24BIT
		       ? sampleL + MIN_VALUE_24BIT + MIN_VALUE_24BIT
		       : sampleL;

		   // read one sample:
		   int sampleR = 0;
		   for (int byteIndex = 0; byteIndex < bytesPerSample; byteIndex++) {
		       final int aByte = audioBytes[byteIndex + i*bytesPerFrame + bytesPerSample] & 0xff;
		       sampleR += aByte << 8 * (byteIndex);
		   }

		   // now handle the sign / valid range
		   final int threeByteSampleR = sampleR > MAX_VALUE_24BIT
		       ? sampleR + MIN_VALUE_24BIT + MIN_VALUE_24BIT
		       : sampleR;
		   
		   samples[sampleIndice] = (float)threeByteSampleL/(MAX_VALUE_24BIT+1); sampleIndice++;
		   samples[sampleIndice] = (float)threeByteSampleR/(MAX_VALUE_24BIT+1); sampleIndice++;
		      }
		    }
			return audioRecord;
		  } catch (Exception ex) { 
		    // Handle the error...
		  }
		} catch (Exception e) {
		  // Handle the error...
		}
		return null;
	}
	
	// https://stackoverflow.com/questions/3297749/java-reading-manipulating-and-writing-wav-files
	private void doSaveAudioFile(File saveChoosedFile, AudioRecord audioRecord) {
		// constant holding the minimum value of a signed 24bit sample: -2^22.
		final int MIN_VALUE_24BIT = -2 << 22;

		// constant holding the maximum value a signed 24bit sample can have, 2^(22-1).
		final int MAX_VALUE_24BIT = -MIN_VALUE_24BIT-1;
		
        final double sampleRate = audioRecord.getSampleRate();

        float[] buffer = audioRecord.getSamples();
        int channels = audioRecord.getChannels();

        final int bits = 24;

        final byte[] byteBuffer = new byte[buffer.length * (bits / Byte.SIZE)];

        int bufferIndex = 0;
        for (int i = 0; i < byteBuffer.length; i++) {
            final int x = Math.round(buffer[bufferIndex++] * MAX_VALUE_24BIT);

            byteBuffer[i++] = (byte)x;
            byteBuffer[i++] = (byte)(x >>> 8);
            byteBuffer[i] = (byte)(x >>> 16);
        }

        final boolean bigEndian = false;
        final boolean signed = true;

        AudioFormat format = new AudioFormat((float)sampleRate, bits, channels, signed, bigEndian);
        ByteArrayInputStream bais = new ByteArrayInputStream(byteBuffer);
        AudioInputStream audioInputStream = new AudioInputStream(bais, format, buffer.length);
        try {
			AudioSystem.write(audioInputStream, AudioFileFormat.Type.WAVE, saveChoosedFile);
	        audioInputStream.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}