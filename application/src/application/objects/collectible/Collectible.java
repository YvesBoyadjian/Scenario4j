package application.objects.collectible;

import application.nodes.SoCollectibles;
import application.nodes.SoTargets;
import jscenegraph.database.inventor.nodes.SoNode;

public interface Collectible {

    int getInstance( int index);

    int indexOfInstance(int instance);

    SoCollectibles getGraphicObject();

    void setGraphicObject(SoCollectibles graphicObject);

    int getNbCollectibles();

    float[] getCollectible(int sealIndex, float[] vector);

    SoNode getNode();

    float getViewDistance();
}
