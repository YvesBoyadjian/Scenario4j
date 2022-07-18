package application.nodes;

import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.nodes.SoSeparator;
import jscenegraph.database.inventor.nodes.SoTranslation;

public class SoCollectible extends SoSeparator {

    private int instance;
    private SbVec3f referencePoint;

    public SoCollectible(int instance) {
        super();
        renderCaching.setValue(SoSeparator.CacheEnabled.OFF);
        pickCulling.setValue(CacheEnabled.OFF); // Speed up picking
        this.instance = instance;
    }

    public void setReferencePoint(SbVec3f referencePoint) {
        this.referencePoint = referencePoint;
    }

    public SbVec3f getCoordinates() {
        return ((SoTranslation)getChild(0)).translation.getValue();
    }
}
