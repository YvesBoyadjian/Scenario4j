package jscenegraph.database.inventor;

import org.lwjgl.BufferUtils;

import java.nio.FloatBuffer;

/**
 * @author Yves Boyadjian
 *
 */
public class SbVec3fSingleFast extends SbVec3f {

    private final float[] xyz = new float[3];

    public SbVec3fSingleFast(float f, float g, float h) {
        super(true);
        setValue(f,g,h);
    }

    public SbVec3fSingleFast() {
        super(true);
    }

    public SbVec3fSingleFast(SbVec3f other) {
        super(true);
        copyFrom(other);
    }

    public final float[] getValue() {
        return xyz;
    }
    // Sets the vector components.
    public SbVec3f setValue(float[] v) {
        xyz[0] = v[0];
        xyz[1] = v[1];
        xyz[2] = v[2];
        return this;
    }

    // Sets the vector components.
    public SbVec3f setValue(float x, float y, float z) {
        xyz[0] = x;
        xyz[1] = y;
        xyz[2] = z;
        return this;
    }

    public float getX() { // java port
        return xyz[0];
    }

    public float getY() {
        return xyz[1];
    }

    public float getZ() {
        return xyz[2];
    }

    protected float g(int i) {
        return xyz[i];
    }
    protected void s(int i, float v) {
        xyz[i] = v;
    }

    public final FloatBuffer getValueGL() {
        FloatBuffer fb = BufferUtils.createFloatBuffer(3);
        fb.put(xyz);
        fb.flip();
        return fb;
        //return FloatBuffer.wrap(vec,indice,3);
    }
}
