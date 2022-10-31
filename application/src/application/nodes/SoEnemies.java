package application.nodes;

import jscenegraph.coin3d.inventor.SbBSPTree;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.nodes.SoSeparator;

public class SoEnemies extends SoSeparator {

    private final SbBSPTree bspTree = new SbBSPTree();

    /*
    register the member
     */
    public void addMember(SbVec3f enemyPosition, int instance) {
        bspTree.addPoint(enemyPosition,instance);
    }
}
