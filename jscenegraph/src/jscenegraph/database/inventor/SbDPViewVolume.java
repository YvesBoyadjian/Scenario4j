/**************************************************************************\
 *
 *  This file is part of the Coin 3D visualization library.
 *  Copyright (C) by Kongsberg Oil & Gas Technologies.
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License
 *  ("GPL") version 2 as published by the Free Software Foundation.
 *  See the file LICENSE.GPL at the root directory of this source
 *  distribution for additional information about the GNU GPL.
 *
 *  For using Coin with software that can not be combined with the GNU
 *  GPL, and for taking advantage of the additional benefits of our
 *  support services, please contact Kongsberg Oil & Gas Technologies
 *  about acquiring a Coin Professional Edition License.
 *
 *  See http://www.coin3d.org/ for more information.
 *
 *  Kongsberg Oil & Gas Technologies, Bygdoy Alle 5, 0257 Oslo, NORWAY.
 *  http://www.sim.no/  sales@sim.no  coin-support@coin3d.org
 *
\**************************************************************************/

package jscenegraph.database.inventor;

import jscenegraph.database.inventor.errors.SoDebugError;
import jscenegraph.port.Array;
import jscenegraph.port.Mutable;

/*!
  \class SbDPViewVolume SbLinear.h Inventor/SbLinear.h
  \brief The SbDPViewVolume class is a double precision viewing volume in 3D space.
  \ingroup base

  This class contains the necessary information for storing a view
  volume.  It has methods for projection of primitives from or into
  the 3D volume, doing camera transforms, view volume transforms etc.

  \COIN_CLASS_EXTENSION

  \sa SbViewportRegion
  \since Coin 2.0
*/

/**
 * @author Yves Boyadjian
 *
 */
public class SbDPViewVolume implements Mutable {
	
	//public
		  //enum ProjectionType { ORTHOGRAPHIC, PERSPECTIVE };
	
		  

		  private			  SbViewVolume.ProjectionType type;
		  private			  final SbVec3d projPoint = new SbVec3d();
		  private			  final SbVec3d projDir = new SbVec3d();
		  private			  double nearDist;
		  private			  double nearToFar;
		  private			  final SbVec3d llf = new SbVec3d();
		  private			  final SbVec3d lrf = new SbVec3d();
		  private			  final SbVec3d ulf = new SbVec3d();


		  public void constructor() {
			  type = null;
			  projPoint.constructor();
			  projDir.constructor();
			  nearDist = 0;
			  nearToFar = 0;
			  llf.constructor();
			  lrf.constructor();
			  ulf.constructor();
		  }
		  
	/*!
	  Returns the six planes defining the view volume in the following
	  order: left, bottom, right, top, near, far. Plane normals are
	  directed into the view volume.

	  This method is an extension for Coin, and is not available in the
	  original Open Inventor.
	*/
	public void
	getViewVolumePlanes(final Array<SbPlane> planes) 
	{
	  final SbVec3d far_ll = new SbVec3d();
	  final SbVec3d far_lr = new SbVec3d();
	  final SbVec3d far_ul = new SbVec3d();
	  final SbVec3d far_ur = new SbVec3d();

	  this.getPlaneRectangle(this.nearToFar, far_ll, far_lr, far_ul, far_ur);
	  SbVec3d near_ur = this.ulf.operator_add (this.lrf.operator_minus(this.llf));

	  SbVec3f f_ulf = dp_to_sbvec3f(this.ulf.operator_add (this.projPoint));
	  SbVec3f f_llf = dp_to_sbvec3f(this.llf.operator_add (this.projPoint));
	  SbVec3f f_lrf = dp_to_sbvec3f(this.lrf.operator_add (this.projPoint));
	  SbVec3f f_near_ur = dp_to_sbvec3f(near_ur.operator_add (this.projPoint));
	  SbVec3f f_far_ll = dp_to_sbvec3f(far_ll.operator_add (this.projPoint));
	  SbVec3f f_far_lr = dp_to_sbvec3f(far_lr.operator_add (this.projPoint));
	  SbVec3f f_far_ul = dp_to_sbvec3f(far_ul.operator_add (this.projPoint));
	  SbVec3f f_far_ur = dp_to_sbvec3f(far_ur.operator_add (this.projPoint));
	  
	  planes.set(0, new SbPlane(f_ulf, f_llf, f_far_ll));  // left
	  planes.set(1, new SbPlane(f_llf, f_lrf, f_far_lr)); // bottom
	  planes.set(2, new SbPlane(f_lrf, f_near_ur, f_far_ur)); // right
	  planes.set(3, new SbPlane(f_near_ur, f_ulf, f_far_ul)); // top
	  planes.set(4, new SbPlane(f_ulf, f_near_ur, f_lrf)); // near
	  planes.set(5, new SbPlane(f_far_ll, f_far_lr, f_far_ur)); // far

	  // check for inverted view volume (negative aspectRatio)
	  if (!planes.get(0).isInHalfSpace(f_lrf)) {
	    final SbVec3f n = new SbVec3f();
	    float D;

	    n.copyFrom( planes.get(0).getNormal());
	    D = planes.get(0).getDistanceFromOrigin();    
	    planes.set(0, new SbPlane(n.operator_minus(), -D));

	    n.copyFrom( planes.get(2).getNormal());
	    D = planes.get(2).getDistanceFromOrigin();    
	    planes.set(2, new SbPlane(n.operator_minus(), -D));
	  }
	  if (!planes.get(1).isInHalfSpace(f_near_ur)) {
	    final SbVec3f n = new SbVec3f();
	    float D;

	    n.copyFrom( planes.get(1).getNormal());
	    D = planes.get(1).getDistanceFromOrigin();    
	    planes.set(1, new SbPlane(n.operator_minus(), -D));

	    n.copyFrom( planes.get(3).getNormal());
	    D = planes.get(3).getDistanceFromOrigin();    
	    planes.set(3, new SbPlane(n.operator_minus(), -D));
	    
	  }

	  if (!planes.get(4).isInHalfSpace(f_far_ll)) {
	    final SbVec3f n = new SbVec3f();
	    float D;

	    n.copyFrom( planes.get(4).getNormal());
	    D = planes.get(4).getDistanceFromOrigin();    
	    planes.set(4, new SbPlane(n.operator_minus(), -D));

	    n.copyFrom( planes.get(5).getNormal());
	    D = planes.get(5).getDistanceFromOrigin();    
	    planes.set(5, new SbPlane(n.operator_minus(), -D));
	    
	  }

	}

	//
	// Returns the four points defining the view volume rectangle at the
	// specified distance from the near plane, towards the far plane. The
	// points are returned in normalized view volume coordinates
	// (projPoint is not added).
	public void
	getPlaneRectangle(final double distance, final SbVec3d  lowerleft,
	                                  final SbVec3d  lowerright,
	                                  final SbVec3d  upperleft,
	                                  final SbVec3d  upperright)
	{
	  final SbVec3d near_ur = new SbVec3d(this.ulf.operator_add(this.lrf.operator_minus(this.llf)));

	//#if COIN_DEBUG
	  if (this.llf.operator_equal_equal( new SbVec3d(0.0, 0.0, 0.0)) ||
	      this.lrf.operator_equal_equal( new SbVec3d(0.0, 0.0, 0.0)) ||
	      this.ulf.operator_equal_equal( new SbVec3d(0.0, 0.0, 0.0)) ||
	      near_ur.operator_equal_equal( new SbVec3d(0.0, 0.0, 0.0))) {
	    SoDebugError.postWarning("SbDPViewVolume::getPlaneRectangle",
	                              "Invalid frustum.");
	    
	  }
	//#endif // COIN_DEBUG

	  if (this.type == SbViewVolume.ProjectionType.PERSPECTIVE) {
	    double depth = this.nearDist + distance;
	    final SbVec3d dir = new SbVec3d();
	    dir.copyFrom(this.llf);
	    dir.normalize(); // safe to normalize here
	    lowerleft.copyFrom( dir.operator_mul( depth).operator_div( dir.dot(this.projDir)));

	    dir.copyFrom( this.lrf);
	    dir.normalize(); // safe to normalize here
	    lowerright.copyFrom( dir.operator_mul( depth).operator_div( dir.dot(this.projDir)));

	    dir.copyFrom( this.ulf);
	    dir.normalize(); // safe to normalize here
	    upperleft.copyFrom( dir.operator_mul( depth).operator_div( dir.dot(this.projDir)));
	    
	    dir.copyFrom( near_ur);
	    dir.normalize(); // safe to normalize here
	    upperright.copyFrom( dir.operator_mul( depth).operator_div( dir.dot(this.projDir)));
	  }
	  else {
	    lowerleft.copyFrom( this.llf.operator_add( this.projDir.operator_mul( distance)));
	    lowerright.copyFrom( this.lrf.operator_add( this.projDir.operator_mul( distance)));
	    upperleft.copyFrom( this.ulf.operator_add( this.projDir.operator_mul( distance)));
	    upperright.copyFrom( near_ur.operator_add( this.projDir.operator_mul( distance)));
	  }
	}

	//
	// some convenience function for converting between single precision
	// and double precision classes.
	//
	public static SbVec3f 
	dp_to_sbvec3f(final SbVec3d v)
	{
	  return new SbVec3f((float)(v.getX()), (float)(v.getY()), (float)(v.getZ()));
	}

	// FIXME: bitmap-illustration for function doc which shows how the
	// frustum is set up wrt the input arguments. 20010919 mortene.
	/*!
	  Set up the view volume for perspective projections. The line of
	  sight will be through origo along the negative z axis.

	  \sa ortho().
	*/
	public void perspective(double fovy, double aspect,
	                            double nearval, double farval)
	{
	//#if COIN_DEBUG
	  if (fovy<0.0f || fovy > Math.PI) {
	    SoDebugError.postWarning("SbDPViewVolume::perspective",
	                              "Field of View 'fovy' ("+fovy+") is out of bounds "+
	                              "[0,PI]. Clamping to be within bounds.");
	    if (fovy<0.0f) fovy=0.0f;
	    else if (fovy>Math.PI) fovy=Math.PI;
	  }

//	#if 0 // obsoleted 2003-02-03 pederb. A negative aspect ratio is ok
//	  if (aspect<0.0f) {
//	    SoDebugError::postWarning("SbDPViewVolume::perspective",
//	                              "Aspect ratio 'aspect' (%d) should be >=0.0f. "
//	                              "Clamping to 0.0f.",aspect);
//	    aspect=0.0f;
//	  }
//	#endif // obsoleted

	  if (nearval>farval) {
	    SoDebugError.postWarning("SbDPViewVolume::perspective",
	                              "far coordinate ("+farval+") should be larger than "+
	                              "near coordinate ("+nearval+"). Swapping near/far."
	                              );
	    double tmp=farval;
	    farval=nearval;
	    nearval=tmp;
	  }
	//#endif // COIN_DEBUG

	  this.type = SbViewVolume.ProjectionType.PERSPECTIVE;
	  this.projPoint.setValue(0.0f, 0.0f, 0.0f);
	  this.projDir.setValue(0.0f, 0.0f, -1.0f);
	  this.nearDist = nearval;
	  this.nearToFar = farval - nearval;

	  double top = nearval * (double)(Math.tan(fovy/2.0f));
	  double bottom = -top;
	  double left = bottom * aspect;
	  double right = -left;

	  this.llf.setValue(left, bottom, -nearval);
	  this.lrf.setValue(right, bottom, -nearval);
	  this.ulf.setValue(left, top, -nearval);
	}
	
	private final SbVec3fSingleFast dummy = new SbVec3fSingleFast();

	public void update(SbViewVolume sbViewVolume) {
		this.llf.setValue(sbViewVolume.llf.operator_minus(sbViewVolume.projPoint, dummy));
		this.lrf.setValue(sbViewVolume.lrf.operator_minus(sbViewVolume.projPoint, dummy));
		this.ulf.setValue(sbViewVolume.ulf.operator_minus(sbViewVolume.projPoint, dummy));
		
		this.type = sbViewVolume.type;
		this.projPoint.setValue(sbViewVolume.projPoint);
		this.projDir.setValue(sbViewVolume.projDir);
		this.nearDist = sbViewVolume.nearDist;
		this.nearToFar = sbViewVolume.nearToFar;
	}

	/*!
	  Returns the view up vector for this view volume. It's a vector
	  which is perpendicular to the projection direction, and parallel and
	  oriented in the same direction as the vector from the lower left
	  corner to the upper left corner of the near plane.
	*/
	public SbVec3d
	getViewUp()
	{
	  final SbVec3d v = this.ulf.operator_minus( this.llf);
	  if (v.normalize() == 0.0) {
	//#if COIN_DEBUG
	    SoDebugError.postWarning("SbDPViewVolume::getViewUp",
	                              "View volume is empty.");
	//#endif // COIN_DEBUG
	  }
	  return v;
	}
	public SbVec3d
	getViewUp(SbVec3d dummyd)
	{
		final SbVec3d v = this.ulf.operator_minus( this.llf, dummyd);
		if (v.normalize() == 0.0) {
			//#if COIN_DEBUG
			SoDebugError.postWarning("SbDPViewVolume::getViewUp",
					"View volume is empty.");
			//#endif // COIN_DEBUG
		}
		return v;
	}

/*!
  Rotate the direction which the camera is pointing in.

  \sa translateCamera().
 */
	public void rotateCamera(final SbRotationd q)
	{
		final SbMatrixd mat = new SbMatrixd();
		mat.setRotate(q);

		mat.multDirMatrix(this.projDir, this.projDir);
		mat.multDirMatrix(this.llf, this.llf);
		mat.multDirMatrix(this.lrf, this.lrf);
		mat.multDirMatrix(this.ulf, this.ulf);
	}

/*!
  Translate the camera position of the view volume.

  \sa rotateCamera().
 */
	public void
	translateCamera(final SbVec3d v)
	{
		this.projPoint.operator_add_equal(v);
	}

/*!
  Returns the combined affine and projection matrix.

  \sa getMatrices(), getCameraSpaceMatrix()
 */
	public SbMatrixd
	getMatrix()
	{
		final SbMatrixd affine = new SbMatrixd(), proj = new SbMatrixd();
		this.getMatrices(affine, proj);
		return affine.multRight(proj);
	}

	// Perspective projection matrix. From the "OpenGL Programming Guide,
// release 1", Appendix G (but with row-major mode).
	public static SbMatrixd
	get_perspective_projection(final double rightminusleft, final double rightplusleft,
                           final double topminusbottom, final double topplusbottom,
                           final double nearval, final double farval)
	{
//#if COIN_DEBUG
		if (nearval * farval <= 0.0) {
			SoDebugError.postWarning("SbDPViewVolume::get_perspective_projection",
					"Projection frustum crosses zero. Rendering is unpredictable.");
		}
//#endif // COIN_DEBUG
		final SbMatrixd proj = new SbMatrixd();
		final double[][] projv = proj.getValue();

		projv[0][0] = 2.0*nearval/rightminusleft;
		projv[0][1] = 0.0;
		projv[0][2] = 0.0;
		projv[0][3] = 0.0;
		projv[1][0] = 0.0;
		projv[1][1] = 2.0*nearval/topminusbottom;
		projv[1][2] = 0.0;
		projv[1][3] = 0.0;
		projv[2][0] = rightplusleft/rightminusleft;
		projv[2][1] = topplusbottom/topminusbottom;
		projv[2][2] = -(farval+nearval)/(farval-nearval);
		projv[2][3] = -1.0;
		projv[3][0] = 0.0;
		projv[3][1] = 0.0;
		projv[3][2] = -2.0*farval*nearval/(farval-nearval);
		projv[3][3] = 0.0;

		// special handling for reverse perspective projection (see SoPerspectiveCamera documentation)
		if (nearval < 0.0) {
			// OpenGL performs clipping in homogeneous space (before computing the perspective division).
			// i.e. instead of testing for -1 <= z/w <= +1, it checks for -w <= z <= +w. Both conditions
			// are only equivalent if w > 0.
			// In the reverse perspective case the projection matrix above leads to negative w values,
			// but this can be compensated by multiplying the whole matrix by -1.
			projv[0][0] *= -1.0;
			projv[1][1] *= -1.0;
			projv[2][0] *= -1.0;
			projv[2][1] *= -1.0;
			projv[2][2] *= -1.0;
			projv[2][3] *= -1.0;
			projv[3][2] *= -1.0;
		}

		return proj;
	}


	// Orthographic projection matrix. From the "OpenGL Programming Guide,
// release 1", Appendix G (but with row-major mode).
	public static SbMatrixd
	get_ortho_projection(final double rightminusleft, final double rightplusleft,
                     final double topminusbottom, final double topplusbottom,
                     final double nearval, final double farval)
	{
		final SbMatrixd proj = new SbMatrixd();
		double[][] projv = proj.getValue();
		projv[0][0] = 2.0/rightminusleft;
		projv[0][1] = 0.0;
		projv[0][2] = 0.0;
		projv[0][3] = 0.0;
		projv[1][0] = 0.0;
		projv[1][1] = 2.0/topminusbottom;
		projv[1][2] = 0.0;
		projv[1][3] = 0.0;
		projv[2][0] = 0.0;
		projv[2][1] = 0.0;
		projv[2][2] = -2.0/(farval-nearval);
		projv[2][3] = 0.0;
		projv[3][0] = -rightplusleft/rightminusleft;
		projv[3][1] = -topplusbottom/topminusbottom;
		projv[3][2] = -(farval+nearval)/(farval-nearval);
		projv[3][3] = 1.0;

		return proj;

	}

/*!
  Returns the view volume's affine matrix and projection matrix.

  \sa getMatrix(), getCameraSpaceMatrix()
 */
	public void getMatrices(final SbMatrixd affine, final SbMatrixd proj)
	{
		SbVec3d upvec = this.ulf.operator_minus(this.llf);
//#if COIN_DEBUG
		if (upvec.operator_equal_equal(new SbVec3d(0.0, 0.0, 0.0))) {
			SoDebugError.postWarning("SbDPViewVolume::getMatrices",
					"empty frustum!");
			affine.copyFrom(SbMatrixd.identity());
			proj.copyFrom(SbMatrixd.identity());
			return;
		}
//#endif // COIN_DEBUG
		SbVec3d rightvec = this.lrf.operator_minus(this.llf);

//#if COIN_DEBUG
		if (rightvec.operator_equal_equal(new SbVec3d(0.0, 0.0, 0.0))) {
			SoDebugError.postWarning("SbDPViewVolume::getMatrices",
					"empty frustum!");
			affine.copyFrom(SbMatrixd.identity());
			proj.copyFrom(SbMatrixd.identity());
			return;
		}
//#endif // COIN_DEBUG

			// we test vectors above, just normalize
					upvec.normalize();
		rightvec.normalize();

		// build matrix that will transform into camera coordinate system
		final SbMatrixd mat = new SbMatrixd();
		double[][] matval = mat.getValue();
		matval[0][0] = rightvec.g(0);
		matval[0][1] = rightvec.g(1);
		matval[0][2] = rightvec.g(2);
		matval[0][3] = 0.0f;

		matval[1][0] = upvec.g(0);
		matval[1][1] = upvec.g(1);
		matval[1][2] = upvec.g(2);
		matval[1][3] = 0.0f;

		matval[2][0] = -this.projDir.g(0);
		matval[2][1] = -this.projDir.g(1);
		matval[2][2] = -this.projDir.g(2);
		matval[2][3] = 0.0f;

		matval[3][0] = this.projPoint.g(0);
		matval[3][1] = this.projPoint.g(1);
		matval[3][2] = this.projPoint.g(2);
		matval[3][3] = 1.0f;

		// the affine matrix is the inverse of the camera coordinate system
		affine.copyFrom(mat.inverse());

		// rotate frustum points back to an axis-aligned view volume to
		// calculate parameters for the projection matrix
		final SbVec3d nlrf = new SbVec3d(), nllf = new SbVec3d(), nulf = new SbVec3d();

		affine.multDirMatrix(this.lrf, nlrf);
		affine.multDirMatrix(this.llf, nllf);
		affine.multDirMatrix(this.ulf, nulf);

		double rml = nlrf.g(0) - nllf.g(0);
		double rpl = nlrf.g(0) + nllf.g(0);
		double tmb = nulf.g(1) - nllf.g(1);
		double tpb = nulf.g(1) + nllf.g(1);
		double n = this.getNearDist();
		double f = n + this.getDepth();

//#if COIN_DEBUG
		if (rml <= 0.0f || tmb <= 0.0f || n >= f) {
			SoDebugError.postWarning("SbDPViewVolume::getMatrices",
					"invalid frustum");
			proj.copyFrom(SbMatrixd.identity());
			return;
		}
//#endif // COIN_DEBUG


		if(this.type == SbViewVolume.ProjectionType.ORTHOGRAPHIC)
		proj.copyFrom(get_ortho_projection(rml, rpl, tmb, tpb, n, f));
  else
		proj.copyFrom(get_perspective_projection(rml, rpl, tmb, tpb, n, f));
	}

/*!
  Returns distance from projection plane to near clipping plane.

  \sa getProjectionDirection().
 */
	public double
	getNearDist()
	{
		return this.nearDist;
	}

/*!
  Returns depth of viewing frustum, i.e. the distance from the near clipping
  plane to the far clipping plane.

  \sa getWidth(), getHeight().
 */
	public double
	getDepth()
	{
		return this.nearToFar;
	}

	@Override
	public void copyFrom(Object other) {
		SbDPViewVolume otherVV = (SbDPViewVolume)other;

		type = otherVV.type;
		projPoint.copyFrom(otherVV.projPoint);
		projDir.copyFrom(otherVV.projDir);
		nearDist = otherVV.nearDist;
		nearToFar = otherVV.nearToFar;
		llf.copyFrom(otherVV.llf);
		lrf.copyFrom(otherVV.lrf);
		ulf.copyFrom(otherVV.ulf);

	}

/*!
  Copies all values of a single precision SbViewVolume \a vv
  to the current double precision instance.
*/
	public void copyValues(final SbViewVolume vv)
	{
		vv.type = /*SbViewVolume.ProjectionType)*/this.type;
		vv.projPoint.copyFrom(dp_to_sbvec3f(this.projPoint));
		vv.projDir.copyFrom(dp_to_sbvec3f(this.projDir));
		vv.nearDist = (float)(this.nearDist);
		vv.nearToFar = (float)(this.nearToFar);
		vv.llf.copyFrom(dp_to_sbvec3f(this.llf.operator_add(this.projPoint)));
		vv.lrf.copyFrom(dp_to_sbvec3f(this.lrf.operator_add(this.projPoint)));
		vv.ulf.copyFrom(dp_to_sbvec3f(this.ulf.operator_add(this.projPoint)));

		vv.llfO.copyFrom(dp_to_sbvec3f(llf)); // For compatibility
		vv.lrfO.copyFrom(dp_to_sbvec3f(lrf));
		vv.ulfO.copyFrom(dp_to_sbvec3f(ulf));
	}
}
