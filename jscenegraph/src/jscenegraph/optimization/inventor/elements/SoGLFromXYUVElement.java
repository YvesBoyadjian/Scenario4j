package jscenegraph.optimization.inventor.elements;

import jscenegraph.coin3d.shaders.SoGLShaderProgram;
import jscenegraph.coin3d.shaders.inventor.elements.SoGLShaderProgramElement;
import jscenegraph.database.inventor.SbVec4f;
import jscenegraph.database.inventor.misc.SoState;

public class SoGLFromXYUVElement extends SoFromXYUVElement {

    public void postPop(SoState state) {
        updateXYUVParameters(state); // CORE
    }

    protected void setElt(SoState state, boolean isActive, SbVec4f xyuv) {
        super.setElt(state, isActive,xyuv);
        updateXYUVParameters(state);
    }

    private void updateXYUVParameters(SoState state) { // CORE

        SoGLShaderProgram sp = SoGLShaderProgramElement.get(state);

        if(null!=sp &&sp.isEnabled())
        {
            sp.updateXYUVParameters(state);
        }
    }
}
