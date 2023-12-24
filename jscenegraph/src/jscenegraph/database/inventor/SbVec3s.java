/**
 * 
 */
package jscenegraph.database.inventor;

import java.util.function.IntConsumer;

import jscenegraph.port.Mutable;
import jscenegraph.port.memorybuffer.FloatMemoryBuffer;
import jscenegraph.port.memorybuffer.ShortMemoryBuffer;

/**
 * @author Yves Boyadjian
 *
 */
public class SbVec3s implements Mutable {
//	protected final short[] vec = new short[3];

	//protected float[] vec;
	protected ShortMemoryBuffer vec;
	protected int indice;

    //! Default constructor
    public SbVec3s()                                           {

		vec = ShortMemoryBuffer.allocateShorts(3);
		indice = 0;

	}

    //! Constructor given an array of 3 components
    public SbVec3s( short v[])                           { setValue(v); }

    //! Constructor given 3 individual components
    public SbVec3s(short x, short y, short z)                  {

		vec = ShortMemoryBuffer.allocateShorts(3);
		indice = 0;

		setValue(x, y, z);
	}

//
// Sets value of vector from array of 3 components
//

public SbVec3s(SbVec3s other) {

//		KDebug.count("SbVec3f");

	vec = ShortMemoryBuffer.allocateShorts(3);
	indice = 0;
	vec.setShort(0, other.g(0));
	vec.setShort(1, other.g(1));
	vec.setShort(2, other.g(2));
	}

	/**
	 * Internal contructor
	 * @param array
	 * @param indice
	 */
	public SbVec3s(ShortMemoryBuffer array, int indice) {

//		KDebug.count("SbVec3f");

		vec = array;
		this.indice = indice;
	}

	/**
	 * Internal method
	 * @param i
	 * @return
	 */
	protected short g(int i) {
		return vec.getShort(indice+i);
	}

	/**
	 * Internal method
	 * @param i
	 * @param v
	 */
	protected void s(int i, short v) {
		vec.setShort(indice+i, v);
	}

	public void setIndice(int indice) {
		this.indice = indice;
	}

	public static int sizeof() {
		return 2*3;
	}

	public short getX() { // java port
		return g(0);
	}

	public short getY() {
		return g(1);
	}

	public short getZ() {
		return g(2);
	}

public SbVec3s 
setValue(final short v[])     
{
	s(0, v[0]); s(1, v[1]); s(2, v[2]); return this;
}

//
// Sets value of vector from 3 individual components
//

public SbVec3s 
setValue(short x, short y, short z)    
{

	s(0, x); s(1, y); s(2, z);
	return this;
}

	/**
	 * for internal use
	 * @return
	 */
	protected final short[] getValueRef() {
		if( indice != 0) {
			throw new IllegalStateException();
		}
		return vec.toShortArray();
	}


	/* (non-Javadoc)
	 * @see jscenegraph.port.Mutable#copyFrom(java.lang.Object)
	 */
	@Override
	public void copyFrom(Object other) {
		operator_assign((SbVec3s)other);
	}

	// java port
	public void operator_assign(SbVec3s other) {
		s(0, other.g(0));
		s(1, other.g(1));
		s(2, other.g(2));
	}

	public IntConsumer[] getRef() {
		IntConsumer[] ret = new IntConsumer[3];
		ret[0] = value -> s(0, (short) value);
		ret[1] = value -> s(1, (short) value);
		ret[2] = value -> s(2, (short) value);
		return ret;
	}

//	public short[] getValue() {
//		return vec;
//	}

	public boolean operator_not_equal(SbVec3s other) {
		SbVec3s v1 = this;
		return !(v1.operator_equal_equal(other));
	}
	
	public boolean operator_equal_equal(final SbVec3s v2) {
		SbVec3s v1 = this;
		return (v1.g(0) == v2.g(0) &&
				v1.g(1) == v2.g(1) &&
				v1.g(2) == v2.g(2));
		}

	public void setValue(int index, short value) {
		s(index, value);
	}
}
