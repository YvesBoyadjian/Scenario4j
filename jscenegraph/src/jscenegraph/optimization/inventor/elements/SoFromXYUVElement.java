package jscenegraph.optimization.inventor.elements;

import jscenegraph.database.inventor.SbName;
import jscenegraph.database.inventor.SbVec4f;
import jscenegraph.database.inventor.elements.SoFontNameElement;
import jscenegraph.database.inventor.elements.SoNormalElement;
import jscenegraph.database.inventor.elements.SoReplacedElement;
import jscenegraph.database.inventor.misc.SoState;
import jscenegraph.database.inventor.nodes.SoNode;

public class SoFromXYUVElement extends SoReplacedElement {

    private boolean isActive;
    private final SbVec4f xyuv = new SbVec4f();

    public void init(SoState state) {
        super.init(state);
        isActive = false;
        xyuv.setValue(0,0,1,1);
    }
    public static void set(SoState state, SoNode node, boolean isActive, SbVec4f xyuv) {

        // Get an instance we can change (pushing if necessary)
        SoFromXYUVElement elt = (SoFromXYUVElement ) getElement(state, classStackIndexMap.get(SoFromXYUVElement.class), node);
        if (elt != null) {
            elt.setElt(state, isActive,xyuv);
        }
    }

    public static SoFromXYUVElement getInstance(SoState state)
    {return ( SoFromXYUVElement )
            getConstElement(state, classStackIndexMap.get(SoFromXYUVElement.class));}

    protected void setElt(SoState state, boolean isActive, SbVec4f xyuv) {
        this.isActive = isActive;
        this.xyuv.copyFrom(xyuv);
    }

    public boolean isActive() {
        return isActive;
    }

    public SbVec4f getXYUV() {
        return xyuv;
    }
}
