package application.nodes;

import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.actions.SoGLRenderAction;
import jscenegraph.database.inventor.nodes.*;

import java.io.File;

public class SoCat extends SoSeparator {

    private final float CAT_SCALE = 0.005f;

    private SoTranslation position;

    private SoRotation rotation;

    public SoCat() {
        super();
        position = new SoTranslation();
        addChild(position);

        rotation = new SoRotation();
        addChild(rotation);

        SoScale scale = new SoScale(){
            public void GLRender(SoGLRenderAction action) {
                super.GLRender(action);
            }

        };
        scale.scaleFactor.setValue(CAT_SCALE,CAT_SCALE,CAT_SCALE);
        addChild(scale);

        SoFile objFile = new SoFile();

        String filePath = "./application/ressource/Cat_v1_L3.123cb1b1943a-2f48-4e44-8f71-6bbe19a3ab64/12221_Cat_v1_l3.obj";
        if(!new File(filePath).exists()) {
            filePath = "ressource/Cat_v1_L3.123cb1b1943a-2f48-4e44-8f71-6bbe19a3ab64/12221_Cat_v1_l3.obj";
        }

        objFile.name.setValue(filePath);

        addChild(objFile);
    }

    public void setPosition(SbVec3f positionValue) {
        position.translation.setValue(positionValue);
    }

    public void setOrientation(SbVec3f orientation) {
        float angle = (float)Math.atan2(orientation.getY(),orientation.getX()) + (float)Math.PI/2.0f;
        rotation.rotation.setValue(new SbVec3f(0,0,1), angle);
    }

    public SbVec3f getPosition() {
        return new SbVec3f(position.translation.getValue());
    }
}
