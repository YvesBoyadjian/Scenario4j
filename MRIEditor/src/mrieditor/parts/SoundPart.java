 
package mrieditor.parts;

import jakarta.inject.Inject;
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

import java.io.File;
import java.nio.file.Path;

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
		Shell shell = Display.getCurrent().getActiveShell();
		FileDialog dialog = new FileDialog(shell);
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
		
		doLoadAudioFile(choosedFile);
		
	}
	
	// https://stackoverflow.com/questions/3297749/java-reading-manipulating-and-writing-wav-files
	// https://stackoverflow.com/questions/56049170/reading-24-bit-mono-pcm-in-java
	private void doLoadAudioFile(File fileIn) {
		int totalFramesRead = 0;
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
		    }
		  } catch (Exception ex) { 
		    // Handle the error...
		  }
		} catch (Exception e) {
		  // Handle the error...
		}		
	}
}