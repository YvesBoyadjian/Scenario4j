package jscenegraph.database.inventor;

import jscenegraph.port.memorybuffer.FloatMemoryBuffer;
import jscenegraph.port.memorybuffer.ShortMemoryBuffer;

public class SbVec3sSingle extends SbVec3s {

    public SbVec3sSingle(short f, short g, short h) {
        super(f,g,h);
    }

    public SbVec3sSingle() {
        super();
    }

    public SbVec3sSingle(SbVec3s other) {
        super(other);
    }

    public final short[] getValue() {
        return getValueRef();
    }

    public final ShortMemoryBuffer getValueBuffer() {
        return vec;
    }
}
