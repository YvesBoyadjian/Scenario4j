package application.nodes;

import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.nodes.*;

public class SoPill extends SoSeparator {

    public final SoTranslation position = new SoTranslation();

    public final SoMaterial material = new SoMaterial();

    public SoPill() {
        addChild(position);

        SoRotation oracleRot = new SoRotation();
        oracleRot.rotation.setValue(new SbVec3f(1,0,0), (float) Math.PI/2);

        addChild(oracleRot);

        SoComplexity complexity = new SoComplexity();
        complexity.value.setValue(1);

        addChild(complexity);

        material.diffuseColor.setValue(0.5f,0.5f,0.5f);

        addChild(material);

        SoCylinder body = new SoCylinder();

        body.height.setValue(1.75f - 0.8f);
        body.radius.setValue(0.4f);
        body.parts.setValue(SoCylinder.Part.SIDES);

        addChild(body);

        SoTranslation headPos = new SoTranslation();

        headPos.translation.setValue(0,1.75f/2 -0.4f,0);

        addChild(headPos);

        SoSphere head = new SoSphere();
        head.radius.setValue(0.4f);
        head.subdivision.setValue(32);

        addChild(head);

        SoTranslation footPos = new SoTranslation();

        footPos.translation.setValue(0,(-1.75f/2 +0.4f)*2,0);

        addChild(footPos);
        addChild(head);
    }
}
