package jscenegraph.port;

import jscenegraph.database.inventor.SbVec3s;
import jscenegraph.database.inventor.SbVec3sSingle;
import jscenegraph.port.memorybuffer.ShortMemoryBuffer;

import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

public class SbVec3sArray extends Indexable<SbVec3s> implements ByteBufferAble, ShortBufferAble, Destroyable {

    ShortMemoryBuffer valuesArray;

    int delta;

    SbVec3s dummy;

    //private ShortBuffer[] shortBuffer = new ShortBuffer[1];

    public SbVec3sArray(SbVec3sArray other) {
        this(other,0);
    }

    public SbVec3sArray(SbVec3sArray other, int delta) {
        valuesArray = other.valuesArray;
        this.delta = other.delta + delta;
        //this.shortBuffer = other.shortBuffer;
    }

    public SbVec3sArray(ShortMemoryBuffer valuesArray) {
        this.valuesArray = valuesArray;
    }

    public SbVec3sArray(SbVec3sSingle singleSbVec3s) {
        valuesArray = singleSbVec3s.getValueBuffer();
    }

    public SbVec3sArray(MutableSbVec3sArray other) {
        valuesArray = other.getValuesArray();
        this.delta = other.getDelta();
    }

    public SbVec3sArray(MutableSbVec3sArray other, int delta) {
        valuesArray = other.getValuesArray();
        this.delta = other.getDelta()+ delta;
    }

    public static SbVec3sArray copyOf(SbVec3sArray other) {
        if(other == null) {
            return null;
        }
        SbVec3sArray copy = new SbVec3sArray(other,0);
        return copy;
    }

    public int getSizeShort() {
        return valuesArray.numShorts() - delta*3;
    }

    public long sizeof() {
        return getSizeShort()*Short.BYTES;
    }

    public SbVec3s get(int index) {
        return new SbVec3s(valuesArray, (index+delta)*3);
    }

    public SbVec3s getFast(int index) {
        if( null == dummy ) {
            dummy = new SbVec3s(valuesArray, (index+delta)*3);
        }
        else {
            dummy.setIndice((index+delta)*3);
        }
        return dummy;
    }

    public short[] get3Shorts(int index, short[] values) {
        values[0] = valuesArray.getShort((index+delta)*3);
        values[1] = valuesArray.getShort((index+delta)*3+1);
        values[2] = valuesArray.getShort((index+delta)*3+2);
        return values;
    }

    public void setValueXYZ(int index, short x, short y, short z) {
        valuesArray.setShort((index+delta)*3, x);
        valuesArray.setShort((index+delta)*3+1, y);
        valuesArray.setShort((index+delta)*3+2, z);
    }

    public SbVec3sArray plus(int delta) {
        return new SbVec3sArray(this,delta);
    }

    public static SbVec3sArray allocate(int maxPoints) {
        return new SbVec3sArray(ShortMemoryBuffer.allocateShorts(maxPoints*3));
    }

    public ShortArray toShortArray() {
        return new ShortArray(delta*3,valuesArray);
    }

    @Override
    public ShortBuffer toShortBuffer() {

        ShortBuffer fb = valuesArray.toByteBuffer().asShortBuffer();

        int offset = delta*3;

        fb.position(offset);

        return fb;

//		int length = valuesArray.length - offset;
//		if(shortBuffer[0] == null || shortBuffer[0].capacity() != length) {
//			shortBuffer[0] = BufferUtils.createShortBuffer(length);
//		//}
//		shortBuffer[0].clear();
//		shortBuffer[0].put(valuesArray, offset, length);
//		shortBuffer[0].flip();
//		}
//		return shortBuffer[0];//ShortBuffer.wrap(valuesArray,offset, length);
    }

    public ShortMemoryBuffer getValuesArray() {
        return valuesArray;
    }

//	public ShortBuffer[] getValuesBuffer() {
//		return shortBuffer;
//	}

    int getDelta() {
        return delta;
    }

    public static SbVec3sArray fromArray(SbVec3s[] arrayPtr) {
        int length = arrayPtr.length;
        ShortMemoryBuffer valuesArray = ShortMemoryBuffer.allocateShorts(length*3);
        int indice=0;
        for(int i=0; i< length; i++) {
            valuesArray.setShort(indice++, arrayPtr[i].getX());
            valuesArray.setShort(indice++, arrayPtr[i].getY());
            valuesArray.setShort(indice++, arrayPtr[i].getZ());
        }
        SbVec3sArray retVal = new SbVec3sArray(valuesArray);
        return retVal;
    }

    public void copyIn(ShortBuffer shortBuffer) {
        int offset = delta*3;
        int length = valuesArray.numShorts() - offset;
        shortBuffer.put(valuesArray.toShortArray(), offset, length);
        shortBuffer.flip();
    }

    public ShortBuffer toShortBuffer(int index) {
        ShortBuffer fb = valuesArray.toShortBuffer();
        fb.position((delta+index)*3);
        return fb;
    }

    @Override
    public ByteBuffer toByteBuffer() {
        ByteBuffer bb = valuesArray.toByteBuffer();
        bb.position(delta*3*Short.BYTES);
        return bb.slice();
    }

    @Override
    public SbVec3s getO(int index) {
        return get(index);
    }

    @Override
    public SbVec3s getOFast(int index) {
        return getFast(index);
    }

    @Override
    public int length() {
        return valuesArray.numShorts()/3 - delta;
    }

    @Override
    public void setO(int index, SbVec3s object) {
        /*getO*/getFast(index).copyFrom(object);

    }

    @Override
    public int delta() {
        return delta;
    }

    @Override
    public Object values() {
        return valuesArray;
    }

    @Override
    public void destructor() {
        if (delta != 0) {
            throw new IllegalArgumentException();
        }
        Destroyable.delete(valuesArray);
        valuesArray = null;
        dummy = null;
    }

    /**
     * @param num
     * @param source
     */
    public void copy(int num, Indexable<SbVec3s> source) {
        if(source instanceof  SbVec3sArray) {
            SbVec3sArray source_ = (SbVec3sArray) source;
            ShortMemoryBuffer.arraycopy(source_.valuesArray,source_.delta*3,valuesArray,delta*3,num*3);
        }
        else {
            super.copy(num,source);
        }
    }
}
