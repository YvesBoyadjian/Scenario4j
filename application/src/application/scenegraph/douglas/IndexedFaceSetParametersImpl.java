package application.scenegraph.douglas;

import jscenegraph.port.memorybuffer.FloatMemoryBuffer;

public class IndexedFaceSetParametersImpl implements IndexedFaceSetParameters {

    public int[] douglasIndicesNearF;
    public FloatMemoryBuffer douglasVerticesNearF;
    public FloatMemoryBuffer douglasNormalsNearF;
    public int[] douglasColorsNearF;
    public FloatMemoryBuffer douglasTexCoordsNearF;

    @Override
    public int[] coordIndices() {
        return douglasIndicesNearF;
    }

    @Override
    public FloatMemoryBuffer vertices() {
        return douglasVerticesNearF;
    }

    @Override
    public FloatMemoryBuffer normals() {
        return douglasNormalsNearF;
    }

    @Override
    public FloatMemoryBuffer textureCoords() {
        return douglasTexCoordsNearF;
    }

    @Override
    public int[] colorsRGBA() {
        return douglasColorsNearF;
    }

    @Override
    public void markConsumed() {
        douglasIndicesNearF = null;
        douglasVerticesNearF = null;
        douglasNormalsNearF = null;
        douglasColorsNearF = null;
        douglasTexCoordsNearF = null;
    }
}
