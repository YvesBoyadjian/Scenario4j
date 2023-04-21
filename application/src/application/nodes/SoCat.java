package application.nodes;

import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.actions.SoGLRenderAction;
import jscenegraph.database.inventor.nodes.*;

public class SoCat extends SoSeparator {

    private final float CAT_SCALE = 0.0025f;

    private SoTranslation position;

    public SoCat() {
        super();
        position = new SoTranslation();
        addChild(position);

        SoScale scale = new SoScale(){
            public void GLRender(SoGLRenderAction action) {
                super.GLRender(action);
            }

        };
        scale.scaleFactor.setValue(CAT_SCALE,CAT_SCALE,CAT_SCALE);
        addChild(scale);

        SoFile objFile = new SoFile();

        objFile.name.setValue("./application/ressource/Cat_v1_L3.123cb1b1943a-2f48-4e44-8f71-6bbe19a3ab64/12221_Cat_v1_l3.obj");

        addChild(objFile);
    }

    public void setPosition(SbVec3f positionValue) {
        position.translation.setValue(positionValue);
    }
}
