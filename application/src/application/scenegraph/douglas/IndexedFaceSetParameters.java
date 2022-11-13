package application.scenegraph.douglas;

import jscenegraph.port.memorybuffer.FloatMemoryBuffer;

public interface IndexedFaceSetParameters {
    int[] coordIndices();
    FloatMemoryBuffer vertices();
    FloatMemoryBuffer normals();
    FloatMemoryBuffer textureCoords();
    int[] colorsRGBA();

    void markConsumed();
}
