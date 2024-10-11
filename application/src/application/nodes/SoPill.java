package application.nodes;

import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.nodes.*;

public class SoPill extends SoSeparator {

    private int instance; // -1 if oracle

    public final SoTranslation position = new SoTranslation();

    public final SoMaterial material = new SoMaterial();

    SoPill(final int instance, final SoPills pills) {
        this.instance = instance;
        addChild(position);

        if(pills.commonPart== null) {
        	pills.buildCommonPart();
        }

        material.diffuseColor.setValue(0.5f,0.5f,0.5f);

        addChild(material);
        addChild(pills.commonPart);
    }

    public SbVec3f getCoordinates() {
        return position.translation.getValue();
    }

    public int getInstance() {
        return instance;
    }
}
