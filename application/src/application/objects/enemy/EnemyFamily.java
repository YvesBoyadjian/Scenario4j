package application.objects.enemy;

import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.fields.SoMFVec3f;
import jscenegraph.database.inventor.nodes.SoNode;
import jscenegraph.database.inventor.nodes.SoSeparator;

import java.util.ArrayList;
import java.util.List;

public class EnemyFamily {
    public int nbEnemies = 0;

    public final SoMFVec3f enemiesInitialCoords = new SoMFVec3f();

    public final List<Integer> enemiesInstances = new ArrayList<>();

    SoSeparator node = new SoSeparator();

    public float[] getEnemy(int sealIndex, float[] vector) {

        if(nbEnemies == 0) {
            compute();
        }

        SbVec3f bootPairCoords = enemiesInitialCoords.getValueAt(sealIndex);
        vector[0] = bootPairCoords.getX();
        vector[1] = bootPairCoords.getY();
        vector[2] = bootPairCoords.getZ();
        return vector;
    }

    void compute() {
        //TODO
    }
    public int indexOfInstance(int instance) {
        return enemiesInstances.indexOf(instance);
    }
    public SoNode getNode() {
        return node;
    }
}
