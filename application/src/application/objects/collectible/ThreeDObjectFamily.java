package application.objects.collectible;

import application.nodes.So3DObjects;
import jscenegraph.database.inventor.nodes.SoNode;

public interface ThreeDObjectFamily {

    int getInstance( int index);

    int indexOfInstance(int instance);

    So3DObjects getGraphicObject();

    void setGraphicObject(So3DObjects graphicObject);

    int getNbCollectibles();

    float[] getCollectible(int sealIndex, float[] vector);

    SoNode getNode(int index);

    float getViewDistance();

	void distanceCallBack(float distance, int index);
}
