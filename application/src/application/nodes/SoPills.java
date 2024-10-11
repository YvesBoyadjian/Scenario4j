/**
 * 
 */
package application.nodes;

import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.nodes.SoComplexity;
import jscenegraph.database.inventor.nodes.SoCylinder;
import jscenegraph.database.inventor.nodes.SoGroup;
import jscenegraph.database.inventor.nodes.SoRotation;
import jscenegraph.database.inventor.nodes.SoSphere;
import jscenegraph.database.inventor.nodes.SoTranslation;

/**
 * 
 */
public class SoPills {

    SoGroup commonPart;

	/**
	 * Instantiate a new SoPill
	 * @param instance
	 * @return
	 */
	public SoPill instantiate(int instance) {
		return new SoPill(instance, this);
	}

    void buildCommonPart() {
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
