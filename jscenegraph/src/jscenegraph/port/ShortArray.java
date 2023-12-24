/**
 * 
 */
package jscenegraph.port;

import jscenegraph.database.inventor.SbRotation;
import jscenegraph.port.memorybuffer.FloatMemoryBuffer;
import jscenegraph.port.memorybuffer.ShortMemoryBuffer;

import java.nio.ByteBuffer;

/**
 * @author Yves Boyadjian
 *
 */
public class ShortArray extends Indexable<Short> implements ByteBufferAble {

	private int start;
	private ShortMemoryBuffer values;

	public ShortArray(int start, ShortMemoryBuffer values) {
		this.start = start;
		this.values = values;
	}

	public ShortArray(int length) {
		values = ShortMemoryBuffer.allocateShorts(length);
	}

	public ShortArray(int start2, ShortArray values2) {
		this.start = start2 + values2.start;
		this.values = values2.values;
	}

	@Override
	public Short getO(int index) {
		return values.getShort(index+start);
	}

	@Override
	public int length() {
		return size();
	}
	public int size() {
		return values.numShorts()-start;
	}

	@Override
	public void setO(int i, Short object) {
		set(i,(short)object);
	}

	public void set(int index, short value) {
		values.setShort(index+start, value);
	}

	public ShortArray plus(int start) {
		return new ShortArray(start,this);
	}

	@Override
	public int delta() {
		return start;
	}

	@Override
	public Object values() {
		return values;
	}

	@Override
	public ByteBuffer toByteBuffer() {
		return null;
	}

	public ShortMemoryBuffer getValuesArray() {
		return values;
	}
}
