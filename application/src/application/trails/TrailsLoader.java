/**
 * 
 */
package application.trails;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * 
 */
public class TrailsLoader {

	public final static long[] loadTrails() {

	// ______________________________________________________________________________________________________ trails
	File trailsFile = new File("trails.mri");
	
	if (!trailsFile.exists()) {
		trailsFile = new File("../trails.mri");
		
		if (!trailsFile.exists()) {
			trailsFile = new File("../../trails.mri");
		}
	}

	long[] trails = null;

	DataInputStream reader = null;
	try {
		reader = new DataInputStream(new BufferedInputStream(new FileInputStream(trailsFile)));
		long version = reader.readLong();
		long size = reader.readLong();

		trails = new long[(int)size];

		for(long i =0; i<size;i++) {
			long code = reader.readLong();
			trails[(int)i] = code;
		}
		reader.close();
	} catch (FileNotFoundException e) {
		e.printStackTrace();
	} catch (IOException e) {
		e.printStackTrace();
	}
	return trails;
	}
}
