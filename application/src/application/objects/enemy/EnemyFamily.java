package application.objects.enemy;

import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.fields.SoMFVec3f;
import jscenegraph.database.inventor.nodes.SoNode;
import jscenegraph.database.inventor.nodes.SoSeparator;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class EnemyFamily {
    public int nbEnemies = 0;

    public final SoMFVec3f enemiesInitialCoords = new SoMFVec3f();

    public final List<Integer> enemiesInstances = new ArrayList<>();

    private final BitSet killedEnemies = new BitSet();

    SoSeparator node = new SoSeparator();

    public float[] getEnemy(int enemyIndex, float[] vector) {

        if(nbEnemies == 0) {
            compute();
        }

        SbVec3f enemyCoords = enemiesInitialCoords.getValueAt(enemyIndex);
        vector[0] = enemyCoords.getX();
        vector[1] = enemyCoords.getY();
        vector[2] = enemyCoords.getZ();
        return vector;
    }

    public boolean isKilled(int index) {
        return killedEnemies.get(index);
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

    public void kill(int instance) {
        int index = enemiesInstances.indexOf(instance);
        if (index >= 0) {
            killedEnemies.set(index);
        }
    }

    public String getKilledInstances() {
        StringBuilder builder = new StringBuilder();
        for(Integer instance : enemiesInstances) {
            if (isKilled(indexOfInstance(instance))) {
                builder.append(instance);
                builder.append(',');
            }
        }
        return builder.toString();
    }
}
