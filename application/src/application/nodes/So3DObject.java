package application.nodes;

import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.nodes.SoSeparator;
import jscenegraph.database.inventor.nodes.SoTranslation;

public class So3DObject extends SoSeparator {

    private int instance;

    public So3DObject(int instance) {
        super();
        renderCaching.setValue(SoSeparator.CacheEnabled.OFF);
        pickCulling.setValue(CacheEnabled.OFF); // Speed up picking
        this.instance = instance;
    }

    public SbVec3f getCoordinates() {
        return ((SoTranslation)getChild(0)).translation.getValue();
    }
}
