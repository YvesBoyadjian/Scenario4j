package application.scenegraph.douglas;

import jscenegraph.port.memorybuffer.FloatMemoryBuffer;
import jscenegraph.port.memorybuffer.ShortMemoryBuffer;

public interface IndexedFaceSetParameters {
    int[] coordIndices();
    FloatMemoryBuffer vertices();
    FloatMemoryBuffer normals();
    ShortMemoryBuffer normalsShort();

    FloatMemoryBuffer textureCoords();
    int[] colorsRGBA();

    boolean keepOwnership();

    void markConsumed();
}
