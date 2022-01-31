/**
 * 
 */
package jscenegraph.database.inventor.elements;

import com.jogamp.opengl.GL2;

import jscenegraph.coin3d.shaders.SoGLShaderProgram;
import jscenegraph.coin3d.shaders.inventor.elements.SoGLShaderProgramElement;
import jscenegraph.database.inventor.SbMatrix;
import jscenegraph.database.inventor.misc.SoState;

/**
 * @author Yves Boyadjian
 *
 */
public class SoGLProjectionMatrixElement extends SoProjectionMatrixElement {

	GL2 gl2;
	// java port
	public void
	init(SoState state) {
		super.init(state);
	}
	
////////////////////////////////////////////////////////////////////////
//
// Description:
//    Pops element, causing side effects in GL.
//
// Use: public

public void
pop(SoState state, SoElement prevElt)
//
////////////////////////////////////////////////////////////////////////
{
	gl2 = state.getGL2();

    //
    // If the previous element is at depth zero, don't bother
    // restoring the matrices-- they will just be setup again the next
    // time a renderAction is applied.  Essentially, the first camera
    // in a scene graph will leak outside of its separator.
    //
    //if (prevElt.getDepth() != 0) { COIN3D
        // Since popping this element has GL side effects, make sure any
        // open caches capture it
        capture(state);

        // Restore previous projection matrix
        send(state);
    //}
}

public void postPop(SoState state) {

	updateStateParameters(state);

}

////////////////////////////////////////////////////////////////////////
//
// Description:
//    Sets GLProjection matrix in element.
//
// Use: protected, virtual

protected void
setElt(final SbMatrix matrix, SoState state)
//
////////////////////////////////////////////////////////////////////////
{
    // Set matrix in element
    projectionMatrix.copyFrom(matrix);
    send(state);
}

////////////////////////////////////////////////////////////////////////
//
// Description:
//    Sends current projection matrix to GL.
//
// Use: private

private void
send(SoState state)
//
////////////////////////////////////////////////////////////////////////
{
	gl2.glMatrixMode(GL2.GL_PROJECTION);
    gl2.glLoadMatrixf((float []) projectionMatrix.toGL(),0);
    gl2.glMatrixMode(GL2.GL_MODELVIEW);

    updateStateParameters(state);
}

//java port
public void push(SoState state) {
	gl2 = state.getGL2();
	super.push(state);
}


	private void updateStateParameters(SoState state) { // CORE

		SoGLShaderProgram sp = SoGLShaderProgramElement.get(state);

		if(null!=sp &&sp.isEnabled())
		{
			// Dependent of SoModelMatrixElement
			sp.updateStateParameters(state);
		}
	}

}
