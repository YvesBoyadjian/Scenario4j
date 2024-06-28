package jscenegraph.database.inventor;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class SbVec3sSingleFast extends SbVec3s {

    private final short[] xyz = new short[3];

    public SbVec3sSingleFast() {
        super(true);
    }

    public SbVec3sSingleFast(SbVec3s other) {
        super(true);
        copyFrom(other);
    }

    public final short[] getValue() {
        return xyz;
    }
    // Sets the vector components.
    public SbVec3s setValue(short[] v) {
        xyz[0] = v[0];
        xyz[1] = v[1];
        xyz[2] = v[2];
        return this;
    }

    // Sets the vector components.
    public SbVec3s setValue(short x, short y, short z) {
        xyz[0] = x;
        xyz[1] = y;
        xyz[2] = z;
        return this;
    }

    public short getX() { // java port
        return xyz[0];
    }

    public short getY() {
        return xyz[1];
    }

    public short getZ() {
        return xyz[2];
    }

    protected short g(int i) {
        return xyz[i];
    }
    protected void s(int i, short v) {
        xyz[i] = v;
    }

    public final ShortBuffer getValueGL() {
        ShortBuffer fb = BufferUtils.createShortBuffer(3);
        fb.put(xyz);
        fb.flip();
        return fb;
        //return FloatBuffer.wrap(vec,indice,3);
    }
}
