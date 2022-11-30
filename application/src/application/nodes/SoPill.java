package application.nodes;

import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.nodes.*;

public class SoPill extends SoSeparator {

    private int instance; // -1 if oracle

    private static SoGroup commonPart;

    public final SoTranslation position = new SoTranslation();

    public final SoMaterial material = new SoMaterial();

    public SoPill(final int instance) {
        this.instance = instance;
        addChild(position);

        if(commonPart== null) {
            buildCommonPart();
        }

        material.diffuseColor.setValue(0.5f,0.5f,0.5f);

        addChild(material);
        addChild(commonPart);
    }

    public SbVec3f getCoordinates() {
        return position.translation.getValue();
    }

    public int getInstance() {
        return instance;
    }

    private void buildCommonPart() {
        commonPart = new SoGroup();
        commonPart.ref();

        SoRotation oracleRot = new SoRotation();
        oracleRot.rotation.setValue(new SbVec3f(1,0,0), (float) Math.PI/2);

        commonPart.addChild(oracleRot);

        SoComplexity complexity = new SoComplexity();
        complexity.value.setValue(1);

        commonPart.addChild(complexity);

        SoCylinder body = new SoCylinder();

        body.height.setValue(1.75f - 0.8f);
        body.radius.setValue(0.4f);
        body.parts.setValue(SoCylinder.Part.SIDES);

        commonPart.addChild(body);

        SoTranslation headPos = new SoTranslation();

        headPos.translation.setValue(0,1.75f/2 -0.4f,0);

        commonPart.addChild(headPos);

        SoSphere head = new SoSphere();
        head.radius.setValue(0.4f);
        head.subdivision.setValue(32);

        commonPart.addChild(head);

        SoTranslation footPos = new SoTranslation();

        footPos.translation.setValue(0,(-1.75f/2 +0.4f)*2,0);

        commonPart.addChild(footPos);
        commonPart.addChild(head);
    }
}
