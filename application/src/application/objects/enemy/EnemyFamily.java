package application.objects.enemy;

import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.fields.SoMFVec3f;

public class EnemyFamily {
    public int nbEnemies = 0;

    public SoMFVec3f enemiesInitialCoords = new SoMFVec3f();

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
}
