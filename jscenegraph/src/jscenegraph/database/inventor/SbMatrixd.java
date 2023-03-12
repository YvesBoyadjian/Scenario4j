/**
 * 
 */
package jscenegraph.database.inventor;

import java.util.function.DoubleConsumer;

import jscenegraph.database.inventor.errors.SoDebugError;
import jscenegraph.port.Mutable;

/**
 * @author Yves Boyadjian
 *
 */
public class SbMatrixd implements Mutable {

    static final double[][] IDENTITYMATRIX = {
            { 1.0, 0.0, 0.0, 0.0 },
            { 0.0, 1.0, 0.0, 0.0 },
            { 0.0, 0.0, 1.0, 0.0 },
            { 0.0, 0.0, 0.0, 1.0 }
    };

    static final double DBL_EPSILON = Math.ulp(1.0);

	  private  final double[][]      matrix = new double[4][4];         //!< Storage for 4x4 matrix

	    //! Default constructor
	  public SbMatrixd() { }


/*!
  This constructor converts a single-precision matrix to a double-precision matrix.
*/
    public SbMatrixd(final SbMatrixd matrixref)
    {
        final double[][] matrixrefv = matrixref.getValue();

        this.setValue(matrixrefv);
    }

//
// Constructor from a 4x4 array of elements
//

public SbMatrixd(final double[][] m)
{
    setValue(m);
}


/*!
  Copies the elements from \a m into the matrix.

  \sa getValue().
 */
    public void
    setValue(final double[][] m)
    {
        matrix[0][0] = m[0][0];
        matrix[0][1] = m[0][1];
        matrix[0][2] = m[0][2];
        matrix[0][3] = m[0][3];
        matrix[1][0] = m[1][0];
        matrix[1][1] = m[1][1];
        matrix[1][2] = m[1][2];
        matrix[1][3] = m[1][3];
        matrix[2][0] = m[2][0];
        matrix[2][1] = m[2][1];
        matrix[2][2] = m[2][2];
        matrix[2][3] = m[2][3];
        matrix[3][0] = m[3][0];
        matrix[3][1] = m[3][1];
        matrix[3][2] = m[3][2];
        matrix[3][3] = m[3][3];
    }


	/* (non-Javadoc)
	 * @see jscenegraph.port.Mutable#copyFrom(java.lang.Object)
	 */
	@Override
	public void copyFrom(Object other) {
		final double[][] m = ((SbMatrixd)other).getValue();
	    matrix[0][0] = m[0][0];
	    matrix[0][1] = m[0][1];
	    matrix[0][2] = m[0][2];
	    matrix[0][3] = m[0][3];
	    matrix[1][0] = m[1][0];
	    matrix[1][1] = m[1][1];
	    matrix[1][2] = m[1][2];
	    matrix[1][3] = m[1][3];
	    matrix[2][0] = m[2][0];
	    matrix[2][1] = m[2][1];
	    matrix[2][2] = m[2][2];
	    matrix[2][3] = m[2][3];
	    matrix[3][0] = m[3][0];
	    matrix[3][1] = m[3][1];
	    matrix[3][2] = m[3][2];
	    matrix[3][3] = m[3][3];
	}

    //! Sets matrix to rotate by given rotation.
    public void        setRotate(final SbRotationd rotation) {
        rotation.getValue(this);    	
    }

	// java port
	public final double[][] getValue() {
		return matrix;
	}
	

/*!
  Return the identity matrix.

  \sa makeIdentity().
 */
    public static final SbMatrixd identity()
    {
        return new SbMatrixd(IDENTITYMATRIX);
    }

//
// Returns 4x4 array of elements
//

public void getValue(double[][] m)
{
    m[0][0] = matrix[0][0];
    m[0][1] = matrix[0][1];
    m[0][2] = matrix[0][2];
    m[0][3] = matrix[0][3];
    m[1][0] = matrix[1][0];
    m[1][1] = matrix[1][1];
    m[1][2] = matrix[1][2];
    m[1][3] = matrix[1][3];
    m[2][0] = matrix[2][0];
    m[2][1] = matrix[2][1];
    m[2][2] = matrix[2][2];
    m[2][3] = matrix[2][3];
    m[3][0] = matrix[3][0];
    m[3][1] = matrix[3][1];
    m[3][2] = matrix[3][2];
    m[3][3] = matrix[3][3];
}

/**
 * Java port
 * @return
 */
public DoubleConsumer[][] getRef() {
	DoubleConsumer[][] ref = new DoubleConsumer[4][4];
	ref[0][0] = value -> matrix[0][0] = value;
	ref[0][1] = value -> matrix[0][1] = value;
	ref[0][2] = value -> matrix[0][2] = value;
	ref[0][3] = value -> matrix[0][3] = value;
	ref[1][0] = value -> matrix[1][0] = value;
	ref[1][1] = value -> matrix[1][1] = value;
	ref[1][2] = value -> matrix[1][2] = value;
	ref[1][3] = value -> matrix[1][3] = value;
	ref[2][0] = value -> matrix[2][0] = value;
	ref[2][1] = value -> matrix[2][1] = value;
	ref[2][2] = value -> matrix[2][2] = value;
	ref[2][3] = value -> matrix[2][3] = value;
	ref[3][0] = value -> matrix[3][0] = value;
	ref[3][1] = value -> matrix[3][1] = value;
	ref[3][2] = value -> matrix[3][2] = value;
	ref[3][3] = value -> matrix[3][3] = value;
	return ref;
}

/**
 * java port
 * @param proj
 */
public void setValue(SbMatrix other) {
	final float[][] m = other.getValue();
    matrix[0][0] = m[0][0];
    matrix[0][1] = m[0][1];
    matrix[0][2] = m[0][2];
    matrix[0][3] = m[0][3];
    matrix[1][0] = m[1][0];
    matrix[1][1] = m[1][1];
    matrix[1][2] = m[1][2];
    matrix[1][3] = m[1][3];
    matrix[2][0] = m[2][0];
    matrix[2][1] = m[2][1];
    matrix[2][2] = m[2][2];
    matrix[2][3] = m[2][3];
    matrix[3][0] = m[3][0];
    matrix[3][1] = m[3][1];
    matrix[3][2] = m[3][2];
    matrix[3][3] = m[3][3];
}

private double MULT_LEFT(double[][] m, double[][] matrix, int i,int j)  {
	
			return 
		m[i][0]*matrix[0][j] + 
        m[i][1]*matrix[1][j] + 
        m[i][2]*matrix[2][j] + 
        m[i][3]*matrix[3][j];
}

//
// Multiplies matrix by given matrix on left
//

public SbMatrixd multLeft(final SbMatrixd m)
{
    // Trivial cases
    if (IS_IDENTITY(m.getValue()))
        return this;
    else if (IS_IDENTITY(matrix)) {
        this.copyFrom(m);
        return this;
    }
        
    final double[][]      tmp = new double[4][4];
    double[][] md = m.getValue();

    tmp[0][0] = MULT_LEFT(md,matrix,0,0);
    tmp[0][1] = MULT_LEFT(md,matrix,0,1);
    tmp[0][2] = MULT_LEFT(md,matrix,0,2);
    tmp[0][3] = MULT_LEFT(md,matrix,0,3);
    tmp[1][0] = MULT_LEFT(md,matrix,1,0);
    tmp[1][1] = MULT_LEFT(md,matrix,1,1);
    tmp[1][2] = MULT_LEFT(md,matrix,1,2);
    tmp[1][3] = MULT_LEFT(md,matrix,1,3);
    tmp[2][0] = MULT_LEFT(md,matrix,2,0);
    tmp[2][1] = MULT_LEFT(md,matrix,2,1);
    tmp[2][2] = MULT_LEFT(md,matrix,2,2);
    tmp[2][3] = MULT_LEFT(md,matrix,2,3);
    tmp[3][0] = MULT_LEFT(md,matrix,3,0);
    tmp[3][1] = MULT_LEFT(md,matrix,3,1);
    tmp[3][2] = MULT_LEFT(md,matrix,3,2);
    tmp[3][3] = MULT_LEFT(md,matrix,3,3);

    this.copyFrom(tmp);
    return this;
}

//
//Macro for checking is a matrix is idenity.
//

private static boolean IS_IDENTITY(double[][] matrix){ return (
 (matrix[0][0] == 1.0) && 
 (matrix[0][1] == 0.0) && 
 (matrix[0][2] == 0.0) && 
 (matrix[0][3] == 0.0) && 
 (matrix[1][0] == 0.0) && 
 (matrix[1][1] == 1.0) && 
 (matrix[1][2] == 0.0) && 
 (matrix[1][3] == 0.0) && 
 (matrix[2][0] == 0.0) && 
 (matrix[2][1] == 0.0) && 
 (matrix[2][2] == 1.0) && 
 (matrix[2][3] == 0.0) && 
 (matrix[3][0] == 0.0) && 
 (matrix[3][1] == 0.0) && 
 (matrix[3][2] == 0.0) && 
 (matrix[3][3] == 1.0));
}


public double[] getValueLinear() {
	double[] valueLinear = new double[16];
	valueLinear[0] = matrix[0][0];
	valueLinear[1] = matrix[0][1];
	valueLinear[2] = matrix[0][2];
	valueLinear[3] = matrix[0][3];
	valueLinear[4] = matrix[1][0];
	valueLinear[5] = matrix[1][1];
	valueLinear[6] = matrix[1][2];
	valueLinear[7] = matrix[1][3];
	valueLinear[8] = matrix[2][0];
	valueLinear[9] = matrix[2][1];
	valueLinear[10] = matrix[2][2];
	valueLinear[11] = matrix[2][3];
	valueLinear[12] = matrix[3][0];
	valueLinear[13] = matrix[3][1];
	valueLinear[14] = matrix[3][2];
	valueLinear[15] = matrix[3][3];
	return valueLinear;
}


/*!
  Multiplies \a src by the matrix. \a src is assumed to be a direction
  vector, and the translation components of the matrix are therefore
  ignored.

  Multiplication is done with the vector on the left side of the
  expression, i.e. dst = src * M.

  \sa multVecMatrix(), multMatrixVec() and multLineMatrix().
 */
    public void
    multDirMatrix(final SbVec3d src, final SbVec3d dst)
    {
        // Checks if the "this" matrix is equal to the identity matrix.  See
        // also code comments at the start of SbDPMatrix::multRight().
        if (SbDPMatrix_isIdentity(this.matrix)) { dst.copyFrom(src); return; }

  final double[] t0 = this.matrix[0];
  final double[] t1 = this.matrix[1];
  final double[] t2 = this.matrix[2];
        // Copy the src vector, just in case src and dst is the same vector.
        final SbVec3dSingle s = new SbVec3dSingle(src);

        dst.s(0, s.getValue()[0]*t0[0] + s.getValue()[1]*t1[0] + s.getValue()[2]*t2[0]);
        dst.s(1, s.getValue()[0]*t0[1] + s.getValue()[1]*t1[1] + s.getValue()[2]*t2[1]);
        dst.s(2, s.getValue()[0]*t0[2] + s.getValue()[1]*t1[2] + s.getValue()[2]*t2[2]);
    }

    static final
    boolean SbDPMatrix_isIdentity( double fm[][])
    {
//#if 0 // I would assume that the memcmp() version is faster..? Should run some profile checks.
        return ((fm[0][0] == 1.0) && (fm[0][1] == 0.0) && (fm[0][2] == 0.0) && (fm[0][3] == 0.0) &&
                (fm[1][0] == 0.0) && (fm[1][1] == 1.0) && (fm[1][2] == 0.0) && (fm[1][3] == 0.0) &&
                (fm[2][0] == 0.0) && (fm[2][1] == 0.0) && (fm[2][2] == 1.0) && (fm[2][3] == 0.0) &&
                (fm[3][0] == 0.0) && (fm[3][1] == 0.0) && (fm[3][2] == 0.0) && (fm[3][3] == 1.0));
//#else
//        // Note: as far as I know, memcmp() only compares bytes until
//        // there's a mismatch (and does *not* run over the full array and
//        // adds up a total, as it sometimes seems from documentation). So
//        // this should be very quick for non-identity matrices.
//        //
//        // Also, we check the first value on its own, to avoid the function
//        // call for the most common case.
//        return (fm[0][0]==1.0) && memcmp(&fm[0][1], &IDENTITYMATRIX[0][1], (4 * 3 + 3) * sizeof(double)) == 0;
//#endif
    }

/*!
  Let this matrix be right-multiplied by \a m. Returns reference to
  self.

  \sa multLeft()
*/
    public SbMatrixd multRight(final SbMatrixd m)
    {
        // Checks if one or the other matrix is equal to the identity matrix
        // before multiplying them. We do this because it's a major
        // optimization if one of them _is_, and the isIdentity() check
        // should be very quick in the common case where a matrix is not the
        // identity matrix.
  final double[][] mfm = m.matrix;
        if (SbDPMatrix_isIdentity(mfm)) { return this; }
        final double[][] tfm = this.matrix;
        if (SbDPMatrix_isIdentity(tfm)) { this.copyFrom(m); return this; }

        final double[][] tmp = new double[4][4];
        for (int i=0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                tmp[i][j] = tfm[i][j]; // java port
            }
        }
        //(void)memcpy(tmp, tfm, 4*4*sizeof(double));

        for (int i=0; i < 4; i++) {
            for (int j=0; j < 4; j++) {
                tfm[i][j] =
                        tmp[i][0] * mfm[0][j] +
                                tmp[i][1] * mfm[1][j] +
                                tmp[i][2] * mfm[2][j] +
                                tmp[i][3] * mfm[3][j];
            }
        }

        return this;
    }

    private void ACCUMULATE() {
        if (temp >= 0.0)
        pos += temp;
            else
        neg += temp;
    }
    double pos, neg, temp; // java port

    /*!
  Return a new matrix which is the inverse matrix of this.

  The user is responsible for checking that this is a valid operation
  to execute, by first making sure that the result of SbDPMatrix::det4()
  is not equal to zero.
 */
    public SbMatrixd
    inverse()
    {
        // check for identity matrix
        if (SbDPMatrix_isIdentity(this.matrix)) { return SbMatrixd.identity(); }

        final SbMatrixd result = new SbMatrixd();

        // use local pointers for speed
        final double[][] dst = result.matrix;
        final double[][] src = this.matrix;

        // check for affine matrix (common case)
        if (src[0][3] == 0.0 && src[1][3] == 0.0 &&
                src[2][3] == 0.0 && src[3][3] == 1.0) {

            // More or less directly from:
            // Kevin Wu, "Fast Matrix Inversion",  Graphics Gems II
            double det_1;

            /*
             * Calculate the determinant of submatrix A and determine if the
             * the matrix is singular as limited by floating-point data
             * representation.
             */
            pos = neg = 0.0;
            temp =  src[0][0] * src[1][1] * src[2][2];
            ACCUMULATE();
            temp =  src[0][1] * src[1][2] * src[2][0];
            ACCUMULATE();
            temp =  src[0][2] * src[1][0] * src[2][1];
            ACCUMULATE();
            temp = -src[0][2] * src[1][1] * src[2][0];
            ACCUMULATE();
            temp = -src[0][1] * src[1][0] * src[2][2];
            ACCUMULATE();
            temp = -src[0][0] * src[1][2] * src[2][1];
            ACCUMULATE();
            det_1 = pos + neg;

//#undef ACCUMULATE

            /* Is the submatrix A singular? */
            if ((det_1 == 0.0) || (Math.abs(det_1 / (pos - neg)) < DBL_EPSILON)) {
                /* Matrix M has no inverse */
//#if COIN_DEBUG
                SoDebugError.postWarning("SbMatrix::inverse",
                        "Matrix is singular.");
//#endif // COIN_DEBUG
                return new SbMatrixd(this);
            }
            else {
                /* Calculate inverse(A) = adj(A) / det(A) */
                det_1 = 1.0 / det_1;
                dst[0][0] = (src[1][1] * src[2][2] -
                        src[1][2] * src[2][1]) * det_1;
                dst[1][0] = - (src[1][0] * src[2][2] -
                        src[1][2] * src[2][0]) * det_1;
                dst[2][0] = (src[1][0] * src[2][1] -
                        src[1][1] * src[2][0]) * det_1;
                dst[0][1] = - (src[0][1] * src[2][2] -
                        src[0][2] * src[2][1]) * det_1;
                dst[1][1] = (src[0][0] * src[2][2] -
                        src[0][2] * src[2][0]) * det_1;
                dst[2][1] = - (src[0][0] * src[2][1] -
                        src[0][1] * src[2][0]) * det_1;
                dst[0][2] =  (src[0][1] * src[1][2] -
                        src[0][2] * src[1][1]) * det_1;
                dst[1][2] = - (src[0][0] * src[1][2] -
                        src[0][2] * src[1][0]) * det_1;
                dst[2][2] =  (src[0][0] * src[1][1] -
                        src[0][1] * src[1][0]) * det_1;

                /* Calculate -C * inverse(A) */
                dst[3][0] = - (src[3][0] * dst[0][0] +
                        src[3][1] * dst[1][0] +
                        src[3][2] * dst[2][0]);
                dst[3][1] = - (src[3][0] * dst[0][1] +
                        src[3][1] * dst[1][1] +
                        src[3][2] * dst[2][1]);
                dst[3][2] = - (src[3][0] * dst[0][2] +
                        src[3][1] * dst[1][2] +
                        src[3][2] * dst[2][2]);

                /* Fill in last column */
                dst[0][3] = dst[1][3] = dst[2][3] = 0.0;
                dst[3][3] = 1.0;
            }
        }
        else { // non-affine matrix
            double max, sum, tmp, inv_pivot;
            final int[] p = new int[4];
            int i, j, k;

            // algorithm from: Schwarz, "Numerische Mathematik"
            result.copyFrom(this);

            for (k = 0; k < 4; k++) {
                max = 0.0;
                p[k] = 0;

                for (i = k; i < 4; i++) {
                    sum = 0.0;
                    for (j = k; j < 4; j++)
                        sum += Math.abs(dst[i][j]);
                    if (sum > 0.0) {
                        tmp = Math.abs(dst[i][k]) / sum;
                        if (tmp > max) {
                            max = tmp;
                            p[k] = i;
                        }
                    }
                }

                if (max == 0.0) {
//#if COIN_DEBUG
                    SoDebugError.postWarning("SbMatrix::inverse",
                            "Matrix is singular.");
//#endif // COIN_DEBUG
                    return new SbMatrixd(this);
                }

                if (p[k] != k) {
                    for (j = 0; j < 4; j++) {
                        tmp = dst[k][j];
                        dst[k][j] = dst[p[k]][j];
                        dst[p[k]][j] = tmp;
                    }
                }

                inv_pivot = 1.0 / dst[k][k];
                for (j = 0; j < 4; j++) {
                    if (j != k) {
                        dst[k][j] = - dst[k][j] * inv_pivot;
                        for (i = 0; i < 4; i++) {
                            if (i != k) dst[i][j] += dst[i][k] * dst[k][j];
                        }
                    }
                }

                for (i = 0; i < 4; i++) dst[i][k] *= inv_pivot;
                dst[k][k] = inv_pivot;
            }

            for (k = 2; k >= 0; k--) {
                if (p[k] != k) {
                    for (i = 0; i < 4; i++) {
                        tmp = dst[i][k];
                        dst[i][k] = dst[i][p[k]];
                        dst[i][p[k]] = tmp;
                    }
                }
            }
        }
        return result;
    }

}
