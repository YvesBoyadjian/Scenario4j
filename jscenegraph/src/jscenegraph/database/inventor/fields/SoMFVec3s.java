package jscenegraph.database.inventor.fields;

import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.SbVec3s;
import jscenegraph.database.inventor.SoInput;
import jscenegraph.port.ShortArray;
import jscenegraph.port.Mutable;
import jscenegraph.port.SbVec3sArray;
import jscenegraph.port.memorybuffer.ShortMemoryBuffer;

import java.util.function.DoubleConsumer;
import java.util.function.IntConsumer;

public class SoMFVec3s extends SoMField<SbVec3s,SbVec3sArray> {

    //	private short[] valuesArray;
//
//	private ShortBuffer[] valuesBuffer = new ShortBuffer[1];
//
    //private SbVec3sArray vec3fArray;
    private ShortArray shortArray;

    private ShortMemoryBuffer valuesArray;

    public SoMFVec3s() {
        super();
        //allocValues(0);
    }

    /**
     * Sets the field to contain the given value and only the given value (if
     * the array had multiple values before, they are deleted).
     *
     * @param xyz
     */
    ////////////////////////////////////////////////////////////////////////
    //
    // Description:
    // Sets to one vector value from array of 3 shorts. (Convenience function)
    //
    // Use: public

    public void setValue(final short[] xyz) {
        setValue(new SbVec3s(xyz));
    }

    /* Get pointer into array of values */
//	@Deprecated
//	public SbVec3s[] getValues(int start) {
//		evaluate();
//
//		SbVec3s[] shiftedValues = new SbVec3s[valuesArray.numShorts()/3 - start];
//		for (int i = start; i < valuesArray.numShorts()/3; i++) {
//			shiftedValues[i - start] = new SbVec3s(valuesArray,i*3);
//		}
//		return shiftedValues;
//	}

    /* Get pointer into array of values
     *
     * Faster method
     *
     * */
    public ShortArray getValuesArray(int start) {
        evaluate();

        ShortArray shiftedValues = new ShortArray( start*3, valuesArray);
        return shiftedValues;
    }

//	@Deprecated
//	public ByteBuffer getValuesBytes(int start) {
//		ShortArray values = getValuesArray(start);
//		return Util.toByteBuffer(values);
//	}

//	// java port
//	@Deprecated
//	public short[] getValuesShort(int start) {
//		evaluate();
//
//		short[] shiftedValues = new short[(valuesArray.numShorts()/3 - start) * 3];
//		int index = 0;
//		for (int i = start; i < valuesArray.numShorts()/3; i++) {
//			shiftedValues[index] = valuesArray.getShort(i*3);
//			index++;
//			shiftedValues[index] = valuesArray.getShort(i*3+1);
//			index++;
//			shiftedValues[index] = valuesArray.getShort(i*3+2);
//			index++;
//		}
//		return shiftedValues;
//	}

    // Set values from array of arrays of 3 shorts.

    //
    // Description:
    // Sets values from array of arrays of 3 shorts. This can be useful
    // in some applications that have vectors stored in this manner and
    // want to keep them that way for efficiency.
    //
    // Use: public

    public void setValues(int start, // Starting index
                          short xyz[][]) // Array of vector values
    //
    {
        int num = xyz.length; // Number of values to set
        int newNum = start + num;
        int i;

        if (newNum > getNum())
            makeRoom(newNum);

        for (i = 0; i < num; i++) {
            valuesArray.setShort((start + i)*3, xyz[i][0]);
            valuesArray.setShort((start + i)*3+1, xyz[i][1]);
            valuesArray.setShort((start + i)*3+2, xyz[i][2]);
        }
        valueChanged();
    }

    /**
     * Java port
     * @param start
     * @param xyz
     */
    public void setValues(int start, short[] xyz) {
        int num = xyz.length/3; // Number of values to set
        int newNum = start + num;
        int i;

        if (newNum > getNum())
            makeRoom(newNum);

        for (i = 0; i < num; i++) {
            valuesArray.setShort((start + i)*3, xyz[3*i]);
            valuesArray.setShort((start + i)*3+1, xyz[3*i+1]);
            valuesArray.setShort((start + i)*3+2, xyz[3*i+2]);
        }
        valueChanged();
    }

    public void setValuesPointer(short[] userdata) {
        setValuesPointer(ShortMemoryBuffer.allocateFromShortArray(userdata),false);
    }

    /**
     * Values in buffer must be already initialized
     * @param userdata
     */
    public void setValuesPointer(ShortMemoryBuffer userdata) {
        setValuesPointer(userdata,true);
    }

    /**
     *
     * @param userdata
     * @param keepOwnership : specify false, if you want Koin3D to delete himself the userdata
     */
    public void setValuesPointer(ShortMemoryBuffer userdata, boolean keepOwnership) {
        makeRoom(0);
        if (userdata != null) {
            valuesArray = userdata;
            values = new SbVec3sArray(valuesArray);
//			    if(buffer != null && buffer.capacity() == userdata.length) {
//			    	valuesBuffer[0] = buffer;
//			    }
//			    else {
//			    	valuesBuffer[0] = BufferUtils.createShortBuffer(userdata.length);
//				    valuesBuffer[0].clear();
//				    valuesBuffer[0].put(valuesArray, 0, userdata.length);
//				    valuesBuffer[0].flip();
//			    }
            if(keepOwnership) {
                userDataIsUsed = true;
            }
            num = maxNum = userdata.numShorts()/3;
            valueChanged();
        }

    }

    ////////////////////////////////////////////////////////////////////////
    //
    // Description:
    // Sets values from array of arrays of 3 shorts. This can be useful
    // in some applications that have vectors stored in this manner and
    // want to keep them that way for efficiency.
    //
    // Use: public

    public void setValues(int start, // Starting index
                          int num, // Number of values to set
                          final short xyz[][/* 3 */]) // Array of vector values
    //
    ////////////////////////////////////////////////////////////////////////
    {
        int newNum = start + num;
        int i;

        if (newNum > getNum())
            makeRoom(newNum);

        for (i = 0; i < num; i++) {
            valuesArray.setShort((start + i)*3, xyz[i][0]);
            valuesArray.setShort((start + i)*3+1, xyz[i][1]);
            valuesArray.setShort((start + i)*3+2, xyz[i][2]);
        }
        valueChanged();
    }

    /**
     * java port
     *
     * @param start
     * @param num
     * @param xyz3d
     */
    public void setValues(int start, int num, short[][][] xyz3d) {
        int xyzLength = 0;
        int xyz3dLength = xyz3d.length;
        for (int i = 0; i < xyz3dLength; i++) {
            if (xyz3d[i] != null) {
                xyzLength += xyz3d[i].length;
            }
        }
        short[][] xyz = new short[xyzLength][];
        int j = 0;
        for (int i = 0; i < xyz3dLength; i++) {
            if (xyz3d[i] != null) {
                short[][] iArray = xyz3d[i];
                int iLength = iArray.length;
                for (int k = 0; k < iLength; k++) {
                    xyz[j] = xyz3d[i][k];
                    j++;
                }
            }
        }
        setValues(start, num, xyz);
    }

    @Override
    protected SbVec3s constructor() {
        return new SbVec3s();
    }

    @Override
    protected SbVec3sArray arrayConstructor(int length) {
        return new SbVec3sArray(ShortMemoryBuffer.allocateShorts(length*3));
    }

    ////////////////////////////////////////////////////////////////////////
    //
    // Description:
    // Reads one (indexed) value from file. Returns FALSE on error.
    //
    // Use: private

    public boolean read1Value(SoInput in, int index)
    //
    ////////////////////////////////////////////////////////////////////////
    {
        IntConsumer[] ref = getValuesSbVec3sArray().getFast(index).getRef();
        return (in.read(ref[0]) && in.read(ref[1]) && in.read(ref[2]));
    }

    //! Set the \p index'th value to the given shorting point values.
////////////////////////////////////////////////////////////////////////
//
//Description:
//Sets one vector value from 3 separate shorts. (Convenience function)
//
//Use: public

    public void set1Value(int index, short x, short y, short z) {
        set1Value(index, new SbVec3s(x, y, z));
    }

    protected void allocValues(int newnum) {
        super.allocValues(newnum);
        valuesArray = (values != null ? values.getValuesArray() : null);
    }

//	protected void allocValues(int newNum) {
//		if (valuesArray == null) {
//			//if (newNum > 0) {
//				valuesArray = arrayConstructorInternal(newNum);
//			//}
//		} else {
//			ShortMemoryBuffer oldValues = valuesArray;
//			int i;
//
//			//if (newNum > 0) {
//				valuesArray = arrayConstructorInternal(newNum);
//				for (i = 0; i < num && i < newNum; i++) { // FIXME : array optimisation
//					valuesArray.setShort(3*i, oldValues.getShort(3*i));
//					valuesArray.setShort(3*i+1, oldValues.getShort(3*i+1));
//					valuesArray.setShort(3*i+2, oldValues.getShort(3*i+2));
//				}
//			//} else
//			//	valuesArray = null;
//			if( values != null ) {
////				if( VoidPtr.has(vec3fArray)) {
////					Destroyable.delete(VoidPtr.create(vec3fArray));
////				}
//				//Destroyable.delete(vec3fArray);
//				values = null;
//			}
//			if ( shortArray != null) {
//				shortArray = null;
//			}
//			// delete [] oldValues; java port
//		}
//
//		num = maxNum = newNum;
//	}

    /* Set field to have one value */
    public void setValue(SbVec3s newValue) {
        makeRoom(1);
        Mutable dest = new SbVec3s(valuesArray,0);
        Mutable src = (Mutable) newValue;
        dest.copyFrom(src);
        valueChanged();
    }

    /* Get non-const pointer into array of values for batch edits */
//    public SbVec3s[] startEditing()
//        {
//    	evaluate();
//    	return getValues(0);
//    	}

    /* Set 1 value at given index */
    public void set1Value(int index, SbVec3s newValue) {
        if (index >= getNum())
            makeRoom(index + 1);
        valuesArray.setShort(index*3, newValue.getX());
        valuesArray.setShort(index*3+1, newValue.getY());
        valuesArray.setShort(index*3+2, newValue.getZ());
        valueChanged();
    }

    public SbVec3s operator_square_bracket(int i) {
        evaluate();
        return new SbVec3s(valuesArray,i*3);
    }

    public SbVec3sArray getValuesSbVec3sArray() {
        evaluate();

//		if( vec3fArray == null || vec3fArray.getValuesArray() != valuesArray ) {
//			vec3fArray = new SbVec3sArray(valuesArray);
//		}
//		return vec3fArray;
        return values;
    }


    public ShortArray getValuesShortArray() {
        evaluate();

        if( shortArray == null || shortArray.getValuesArray() != valuesArray ) {
            shortArray = new ShortArray(0,valuesArray);
        }
        return shortArray;
    }
}
