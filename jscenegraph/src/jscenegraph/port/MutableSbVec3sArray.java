package jscenegraph.port;

import jscenegraph.database.inventor.SbVec3s;
import jscenegraph.database.inventor.SbVec3sSingle;
import jscenegraph.port.memorybuffer.ShortMemoryBuffer;

import java.nio.ShortBuffer;

public class MutableSbVec3sArray implements ShortBufferAble {

    private ShortMemoryBuffer valuesArray;

    private int delta;

    SbVec3s dummy;

    public MutableSbVec3sArray(MutableSbVec3sArray other, int delta) {
        valuesArray = other.valuesArray;
        this.delta = other.delta + delta;
    }

    public MutableSbVec3sArray(ShortMemoryBuffer valuesArray) {
        this.valuesArray = valuesArray;
    }

    public MutableSbVec3sArray(SbVec3sSingle singleSbVec3s) {
        valuesArray = singleSbVec3s.getValueBuffer();
    }

    private MutableSbVec3sArray(SbVec3sArray other) {
        valuesArray = other.getValuesArray();
        this.delta = other.getDelta();
    }

    public static MutableSbVec3sArray from(SbVec3sArray other) {
        if ( null == other ) {
            return null;
        }
        return new MutableSbVec3sArray(other);
    }

    public MutableSbVec3sArray(MutableSbVec3sArray other) {
        valuesArray = other.getValuesArray();
        this.delta = other.getDelta();
    }

    public SbVec3s get(int index) {
        if( null == dummy ) {
            dummy = new SbVec3s(valuesArray, (index+delta)*3);
        }
        else {
            dummy.setIndice((index+delta)*3);
        }
        return dummy;
    }

    public MutableSbVec3sArray plus(int delta) {
        return new MutableSbVec3sArray(this,delta);
    }

    public static SbVec3sArray allocate(int maxPoints) {
        return new SbVec3sArray(ShortMemoryBuffer.allocateShorts(maxPoints*3));
    }

    public ShortArray toShortArray() {
        return new ShortArray(delta*3,valuesArray);
    }

    @Override
    public ShortBuffer toShortBuffer() {
        ShortBuffer fb = valuesArray.toShortBuffer();
        fb.position(delta*3);
        return fb;
        //return ShortBuffer.wrap(valuesArray,delta*3,valuesArray.numShorts() - delta*3);
    }

    public void plusPlus() {
        delta++;
    }

    ShortMemoryBuffer getValuesArray() {
        return valuesArray;
    }

    int getDelta() {
        return delta;
    }

    public void assign(MutableSbVec3sArray other) {
        if( valuesArray != other.getValuesArray()) {
            throw new IllegalArgumentException();
        }
        delta = other.getDelta();
    }

    public void assign(MutableSbVec3sArray other, int delta) {
        if( valuesArray != other.valuesArray ) {
            throw new IllegalArgumentException();
        }
        this.delta = other.delta + delta;
    }

    public void assign(SbVec3sArray other, int delta) {
        if( valuesArray != other.valuesArray ) {
            throw new IllegalArgumentException();
        }
        this.delta = other.delta + delta;
    }

    public short[] get3Shorts(short[] dummy) {
        dummy[0] = valuesArray.getShort(delta*3);
        dummy[1] = valuesArray.getShort(delta*3+1);
        dummy[2] = valuesArray.getShort(delta*3+2);
        return dummy;
    }
}
