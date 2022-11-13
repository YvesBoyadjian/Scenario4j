package jscenegraph.coin3d.inventor.rendering;

import com.jogamp.opengl.GL2;
import jscenegraph.coin3d.glue.cc_glglue;
import jscenegraph.coin3d.misc.SoGL;
import jscenegraph.mevis.inventor.misc.SoVBO;
import jscenegraph.port.Destroyable;
import jscenegraph.port.IntArrayPtr;
import jscenegraph.port.VoidPtr;
import jscenegraph.port.memorybuffer.MemoryBuffer;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

public class SoTriangleIntArrayIndexer implements Destroyable, VertexArrayIndexer {

    private SoVBO vbo;

    private VoidPtr intPtr;
    private final int numBytes;

    public static final boolean isEligible(IntArrayPtr indices) {
        final int size = indices.size();
        for (int i=3; i<size; i+=4) {
            if (indices.get(i) >= 0) {
                return false;
            }
        }
        return true;
    }

    public SoTriangleIntArrayIndexer(IntArrayPtr indices) {
        final int size = indices.size();
        int nbTriangles = size/4;
        if (nbTriangles*4 < size) {
            nbTriangles++;
        }
        numBytes = nbTriangles*3*Integer.BYTES;
        intPtr = VoidPtr.create(/*Buffers.newDirectByteBuffer(numBytes)*/MemoryBuffer.allocateBytesMalloc(numBytes));
        IntBuffer uintPtr = intPtr.toIntBuffer();
        int index = 0;
        int indiceIndex = 0;
        for (int i=0; i<nbTriangles;i++) {
            uintPtr.put(index,indices.get(indiceIndex)); index++; indiceIndex++;
            uintPtr.put(index,indices.get(indiceIndex)); index++; indiceIndex++;
            uintPtr.put(index,indices.get(indiceIndex)); index++; indiceIndex++;
            indiceIndex++;
        }
    }

    @Override
    public void addTriangle(int v0, int v1, int v2) {

    }

    @Override
    public void addQuad(int v0, int v1, int v2, int v3) {

    }

    @Override
    public void beginTarget(int targetin) {

    }

    @Override
    public void targetVertex(int targetin, int v) {

    }

    @Override
    public void endTarget(int targetin) {

    }

    @Override
    public void close() {

    }

    @Override
    public int getNumVertices() {
        return numBytes/Integer.BYTES;
    }

    @Override
    public void render(cc_glglue glue, boolean renderasvbo, int contextid) {
        // common case
        if (renderasvbo) {
            if (this.vbo == null) {
                this.vbo = new SoVBO(GL2.GL_ELEMENT_ARRAY_BUFFER);
                this.vbo.setBufferData(intPtr, numBytes);
            }
            this.vbo.bindBuffer(contextid);
            SoGL.cc_glglue_glDrawElements(glue,
                    GL2.GL_TRIANGLES,
                    numBytes/Short.BYTES,
                    GL2.GL_UNSIGNED_INT, (ByteBuffer) null);
            SoGL.cc_glglue_glBindBuffer(glue, GL2.GL_ELEMENT_ARRAY_BUFFER, 0);
        }
        else {
            ByteBuffer idxptr = intPtr.toByteBuffer();
            SoGL.cc_glglue_glDrawElements(glue,
                    GL2.GL_TRIANGLES,
                    numBytes/Integer.BYTES,
                    GL2.GL_UNSIGNED_INT,
                    idxptr);
        }
    }

    @Override
    public void destructor() {
        Destroyable.delete( this.vbo);
        vbo = null;
        intPtr = null;
    }
}
