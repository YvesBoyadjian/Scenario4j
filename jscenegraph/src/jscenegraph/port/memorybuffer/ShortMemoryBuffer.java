package jscenegraph.port.memorybuffer;

import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

public class ShortMemoryBuffer extends MemoryBuffer {

    public final static int MINIMUM_SHORTS_FOR_BUFFER = 12;//SoVBO.getVertexCountMinLimit() * 3;

    private short[] shortArray;

    private boolean byteBufferDirty;

    private ShortBuffer dummyFloatBuffer;

    ShortMemoryBuffer() {
        super();
    }

    public int numBytes() {
        if(byteBuffer != null) {
            return byteBuffer.limit();// capacity();
        }
        else {
            return shortArray.length * Short.BYTES;
        }
    }

    public int numShorts() {
        return numBytes()/Short.BYTES;
    }

    public static final ShortMemoryBuffer allocateShorts(int numShorts) {

        ShortMemoryBuffer memoryBuffer = new ShortMemoryBuffer();

        if( numShorts >= MINIMUM_SHORTS_FOR_BUFFER) {

            int numBytes = numShorts*Short.BYTES;

            memoryBuffer.byteBuffer = MemoryBufferPool.pool.createByteBuffer(numBytes);//BufferUtils.createByteBuffer(numBytes);
        }
        else {
            memoryBuffer.shortArray = new short[numShorts];
        }

        return memoryBuffer;
    }

    public static final ShortMemoryBuffer allocateShortsMalloc(int numShorts) {

        ShortMemoryBuffer memoryBuffer = new ShortMemoryBuffer();

        if( numShorts >= MINIMUM_SHORTS_FOR_BUFFER) {

            int numBytes = numShorts*Short.BYTES;

            memoryBuffer.byteBuffer = MemoryBufferPool.pool.memAlloc(numBytes);//MemoryUtil.memAlloc(numBytes);
            memoryBuffer.malloc = true;
        }
        else {
            memoryBuffer.shortArray = new short[numShorts];
        }

        return memoryBuffer;
    }

    /**
     * Note : array data is copied
     * @param array
     * @return
     */
    public static ShortMemoryBuffer allocateFromShortArray(short[] array) {

        int numShorts = array.length;
        //int numBytes = numShorts * Short.BYTES;

        ShortMemoryBuffer memoryBuffer = allocateShorts(numShorts);
        memoryBuffer.setShorts(array, numShorts);

        return memoryBuffer;
    }

    public static final void arraycopy(
            ShortMemoryBuffer src,
            int srcPos,
            ShortMemoryBuffer dest,
            int destPos,
            int length
    ) {
        ShortBuffer destSlice = dest.toShortBuffer().position(destPos).slice();
        ShortBuffer srcSlice = src.toShortBuffer().position(srcPos).slice().limit(length);
        destSlice.put(srcSlice);

        dest.toShortBuffer().position(0);
        src.toShortBuffer().position(0);

//		for(int i=0; i<length; i++) {
//			dest.setShort(i+destPos,src.getShort(i+srcPos));
//		}
    }

    public void setShorts(short[] srcShorts, int numShorts) {

        if( byteBuffer != null ) {
            updateByteBuffer();
            for( int index = 0; index < numShorts; index++) {
                byteBuffer.putShort(index * Short.BYTES,srcShorts[index]);
            }
        }
        else {
            for( int index = 0; index < numShorts; index++) {
                shortArray[index] = srcShorts[index];
            }
        }
    }

    public void setShort(int shortIndex, short value) {

        if( byteBuffer != null ) {
            updateByteBuffer();
            byteBuffer.putShort(shortIndex * Short.BYTES, value);
        }
        else {
            shortArray[shortIndex] = value;
        }
    }

    public short getShort(int shortIndex) {

        if( byteBuffer != null ) {
            updateByteBuffer();
            return byteBuffer.getShort(shortIndex * Short.BYTES);
        }
        else {
            return shortArray[shortIndex];
        }
    }

    public short[] toShortArray() {
        //updateByteBuffer();

        if( shortArray == null ) {
            shortArray = new short[numShorts()];
        }

        if(byteBuffer != null) {
            if(!byteBufferDirty) {
                // copy from buffer to array
                byteBuffer.asShortBuffer().get(shortArray);
                byteBufferDirty = true;
            }
        }
        return shortArray;
    }

    protected void updateByteBuffer() {
        if(byteBuffer == null) {
            int numBytes = shortArray.length * Short.BYTES;
            byteBuffer = BufferUtils.createByteBuffer(numBytes);
            byteBuffer.asShortBuffer().put(shortArray);
        }
        if(byteBufferDirty) {
            byteBufferDirty = false;
            // copy from array to buffer
            byteBuffer.asShortBuffer().put(shortArray);
        }
    }

    /**
     * Position of short buffer is not guaranteed
     * @return
     */
    public ShortBuffer toShortBuffer() {
        if(dummyFloatBuffer == null) {
            ByteBuffer dummyByteBuffer = toByteBuffer();
            dummyByteBuffer.position(0);
            dummyFloatBuffer = dummyByteBuffer.asShortBuffer();
        }
        return dummyFloatBuffer;
    }
}
