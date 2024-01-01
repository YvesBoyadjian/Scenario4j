package jscenegraph.optimization.inventor.nodes;

import jscenegraph.coin3d.inventor.nodes.SoVertexProperty;
import jscenegraph.database.inventor.SbVec4f;
import jscenegraph.database.inventor.SbVec4fSingle;
import jscenegraph.database.inventor.SoType;
import jscenegraph.database.inventor.actions.SoGLRenderAction;
import jscenegraph.database.inventor.actions.SoRayPickAction;
import jscenegraph.database.inventor.fields.SoField;
import jscenegraph.database.inventor.fields.SoFieldData;
import jscenegraph.database.inventor.fields.SoSFBool;
import jscenegraph.database.inventor.fields.SoSFVec4f;
import jscenegraph.database.inventor.misc.SoNotList;
import jscenegraph.database.inventor.misc.SoState;
import jscenegraph.database.inventor.nodes.SoNode;
import jscenegraph.database.inventor.nodes.SoSubNode;
import jscenegraph.optimization.inventor.elements.SoFromXYUVElement;
import jscenegraph.optimization.inventor.elements.SoGLFromXYUVElement;

public class SoOptimizationFromXYUV extends SoNode {

    // ______________________________________________________________ Type and fields management
    private final SoSubNode nodeHeader = SoSubNode.SO_NODE_HEADER(SoOptimizationFromXYUV.class,this);

    public
    static SoType getClassTypeId()        /* Returns class type id */
    { return SoSubNode.getClassTypeId(SoOptimizationFromXYUV.class);  }
    public  SoType      getTypeId()      /* Returns type id      */
    {
        return nodeHeader.getClassTypeId();
    }
    public SoFieldData getFieldData()  {
        return nodeHeader.getFieldData();
    }
    public  static SoFieldData[] getFieldDataPtr()
    { return SoSubNode.getFieldDataPtr(SoOptimizationFromXYUV.class); }

    // ______________________________________________________________ Public fields
    public final SoSFBool isActive = new SoSFBool();
    /**
     * (xmin, ymin, xmax, ymax)
     */
    public final SoSFVec4f xyMinMax = new SoSFVec4f();

    // ______________________________________________________________ Private fields
    private final SbVec4fSingle xyuv = new SbVec4fSingle();

    // ______________________________________________________________ Public methods
    public SoOptimizationFromXYUV() {

        nodeHeader.SO_NODE_INTERNAL_CONSTRUCTOR(SoOptimizationFromXYUV.class);
        nodeHeader.SO_NODE_ADD_FIELD(isActive,"isActive", true);
        nodeHeader.SO_NODE_ADD_FIELD(xyMinMax,"xyMinMax", new SbVec4f(0,0, 1, 1));
    }

    public void GLRender(SoGLRenderAction action) {

        SoState state = action.getState();
        SoFromXYUVElement.set(state, this, isActive.getValue(), xyuv);
    }

    public void
    notify(SoNotList nl) {

        SoField f = nl.getLastField();
        if (f == xyMinMax) {
            SbVec4f xyMinMaxValue = xyMinMax.getValue();
            xyuv.setValue(
                    xyMinMaxValue.getX(),
                    xyMinMaxValue.getY(),
                    1.0f/(xyMinMaxValue.getZ() - xyMinMaxValue.getX()),
                    1.0f/(xyMinMaxValue.getW() - xyMinMaxValue.getY())
                    );
        }

        super.notify(nl);
    }

    public void destructor() {
        nodeHeader.destructor();
        super.destructor();
    }

    // _______________________________________________________________ Class type initialization
    // Documented in superclass.
    public static void
    initClass() {
        SoSubNode.SO__NODE_INIT_CLASS(SoOptimizationFromXYUV.class, "OptimizationFromXYUV", SoNode.class);
        SO_ENABLE(SoGLRenderAction.class, SoGLFromXYUVElement.class);
        SO_ENABLE(SoRayPickAction.class, SoGLFromXYUVElement.class);
    }
}
