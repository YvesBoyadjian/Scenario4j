/**
 * 
 */
package jscenegraph.port;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.Objects;

import org.lwjgl.BufferUtils;

import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.SbVec3fSingle;
import jscenegraph.port.memorybuffer.FloatMemoryBuffer;

/**
 * @author Yves Boyadjian
 *
 */
public class SbVec3fArray extends Indexable<SbVec3f> implements ByteBufferAble, FloatBufferAble, Destroyable {
	
	FloatMemoryBuffer valuesArray;

	int delta;

	SbVec3f dummy;
	
	//private FloatBuffer[] floatBuffer = new FloatBuffer[1];
	
	public SbVec3fArray(SbVec3fArray other) {
		this(other,0);
	}
	
	public SbVec3fArray(SbVec3fArray other, int delta) {
		valuesArray = other.valuesArray;
		this.delta = other.delta + delta;
		//this.floatBuffer = other.floatBuffer;
	}

	public SbVec3fArray(FloatMemoryBuffer valuesArray) {
		this.valuesArray = valuesArray;
	}

	public SbVec3fArray(SbVec3fSingle singleSbVec3f) {
		valuesArray = singleSbVec3f.getValueBuffer();
	}

	public SbVec3fArray(MutableSbVec3fArray other) {
		valuesArray = other.getValuesArray();
		this.delta = other.getDelta();
	}

	public SbVec3fArray(MutableSbVec3fArray other, int delta) {
		valuesArray = other.getValuesArray();
		this.delta = other.getDelta()+ delta;
	}

	public static SbVec3fArray copyOf(SbVec3fArray other) {
		if(other == null) {
			return null;
		}
		SbVec3fArray copy = new SbVec3fArray(other,0);
		return copy;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof SbVec3fArray that)) return false;
		if (!super.equals(o)) return false;
        return delta == that.delta && valuesArray == that.valuesArray && dummy == that.dummy;
	}

	@Override
	public int hashCode() {
		return Objects.hash(super.hashCode(), valuesArray, delta, dummy);
	}

	public int getSizeFloat() {
		return valuesArray.numFloats() - delta*3;
	}

	public long sizeof() {
		return getSizeFloat()*Float.BYTES;
	}

	public SbVec3f get(int index) {
		return new SbVec3f(valuesArray, (index+delta)*3);
	}

	public SbVec3f getFast(int index) {
		if( null == dummy ) {
			dummy = new SbVec3f(valuesArray, (index+delta)*3);
		}
		else {
			dummy.setIndice((index+delta)*3);
		}
		return dummy;
	}

	public float[] get3Floats(int index, float[] values) {
		values[0] = valuesArray.getFloat((index+delta)*3);
		values[1] = valuesArray.getFloat((index+delta)*3+1);
		values[2] = valuesArray.getFloat((index+delta)*3+2);
		return values;
	}
	
	public void setValueXYZ(int index, float x, float y, float z) {
		valuesArray.setFloat((index+delta)*3, x);
		valuesArray.setFloat((index+delta)*3+1, y);
		valuesArray.setFloat((index+delta)*3+2, z);
	}

	public SbVec3fArray plus(int delta) {
		return new SbVec3fArray(this,delta);
	}

	public static SbVec3fArray allocate(int maxPoints) {
		return new SbVec3fArray(FloatMemoryBuffer.allocateFloats(maxPoints*3));
	}
	
	public FloatArray toFloatArray() {
		return new FloatArray(delta*3,valuesArray);
	}

	public float[] toFloats() {
		if (delta != 0) {
			throw new IllegalStateException();
		}
		return valuesArray.toFloatArray();
	}

	@Override
	public FloatBuffer toFloatBuffer() {
		
		FloatBuffer fb = valuesArray.toByteBuffer().asFloatBuffer();
		
		int offset = delta*3;
		
		fb.position(offset);
		
		return fb;
		
//		int length = valuesArray.length - offset;
//		if(floatBuffer[0] == null || floatBuffer[0].capacity() != length) {
//			floatBuffer[0] = BufferUtils.createFloatBuffer(length);
//		//}
//		floatBuffer[0].clear();
//		floatBuffer[0].put(valuesArray, offset, length);
//		floatBuffer[0].flip();
//		}
//		return floatBuffer[0];//FloatBuffer.wrap(valuesArray,offset, length);
	}
	
	public FloatMemoryBuffer getValuesArray() {
		return valuesArray;
	}
	
//	public FloatBuffer[] getValuesBuffer() {
//		return floatBuffer;
//	}

	int getDelta() {
		return delta;
	}

	public static SbVec3fArray fromArray(SbVec3f[] arrayPtr) {
		int length = arrayPtr.length;
		FloatMemoryBuffer valuesArray = FloatMemoryBuffer.allocateFloats(length*3);
		int indice=0;
		for(int i=0; i< length; i++) {
			valuesArray.setFloat(indice++, arrayPtr[i].getX());
			valuesArray.setFloat(indice++, arrayPtr[i].getY());
			valuesArray.setFloat(indice++, arrayPtr[i].getZ());
		}
		SbVec3fArray retVal = new SbVec3fArray(valuesArray);
		return retVal;
	}

	public void copyIn(FloatBuffer floatBuffer) {
		int offset = delta*3;
		int length = valuesArray.numFloats() - offset;
		floatBuffer.put(valuesArray.toFloatArray(), offset, length);
		floatBuffer.flip();
	}

	public FloatBuffer toFloatBuffer(int index) {
		FloatBuffer fb = valuesArray.toFloatBuffer();
		fb.position((delta+index)*3);
		return fb;
	}

	@Override
	public ByteBuffer toByteBuffer() {
		ByteBuffer bb = valuesArray.toByteBuffer();
		bb.position(delta*3*Float.BYTES);
		return bb.slice();
	}

	@Override
	public SbVec3f getO(int index) {		
		return get(index);
	}

	@Override
	public SbVec3f getOFast(int index) {
		return getFast(index);
	}

	@Override
	public int length() {
		return valuesArray.numFloats()/3 - delta;
	}

	@Override
	public void setO(int index, SbVec3f object) {
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
	public void copy(int num, Indexable<SbVec3f> source) {
		if(source instanceof  SbVec3fArray) {
			SbVec3fArray source_ = (SbVec3fArray) source;
			FloatMemoryBuffer.arraycopy(source_.valuesArray,source_.delta*3,valuesArray,delta*3,num*3);
		}
		else {
			super.copy(num,source);
		}
	}
}
