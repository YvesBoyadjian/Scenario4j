package application.targets;

import application.objects.Target;
import application.objects.TargetBase;
import application.scenegraph.SceneGraphIndexedFaceSetShader;
import jscenegraph.coin3d.inventor.lists.SbListFloat;
import jscenegraph.database.inventor.SbBox3f;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.fields.SoMFVec3f;

import java.util.Random;
import java.util.SplittableRandom;
import java.util.random.RandomGenerator;

public class GroundSquirrels extends TargetBase implements Target {

    public final static String GROUND_SQUIRREL_NAME = "Squirrel";

    @Override
    public String targetName() {
        return GROUND_SQUIRREL_NAME;
    }

    @Override
    public String getTexturePath() {
        return "ressource/Golden-Mantled_Ground_Squirrel,_Mount_Rainier,_July_2006.jpg";
    }

    @Override
    public int getNbTargets() {

        if( nbSquirrels == 0 ) {
            compute();
        }
        return nbSquirrels;
    }

    @Override
    public float[] getTarget(int marmotIndex, float[] vector) {

        if (nbSquirrels == 0) {
            compute();
        }

        vector[0] = squirrelCoords.get(marmotIndex*3);
        vector[1] = squirrelCoords.get(marmotIndex*3+1);
        vector[2] = squirrelCoords.get(marmotIndex*3+2);

        return vector;
    }

    @Override
    public float getSize() {
        return 0.5f;
    }

    @Override
    public float getRatio() {
        return 1;
    }

    @Override
    public float getViewDistance() {
        return 500;
    }

    SceneGraphIndexedFaceSetShader sg;

    int nbSquirrels = 0;

    SbListFloat squirrelCoords = new SbListFloat();

    final int HUNDRED_THOUSAND = 100000;

    int NB_SQUIRREL_BIRTHS = HUNDRED_THOUSAND;

    final static int SEED_SQUIRREL_PLACEMENT = 52;

    public GroundSquirrels( SceneGraphIndexedFaceSetShader sg ) {
        this.sg = sg;
    }

    private void compute() {

        RandomGenerator randomPlacementSquirrels = new SplittableRandom(SEED_SQUIRREL_PLACEMENT);

        int[] indices = new int[4];

        float zWater = - 150 + sg.getzTranslation() - sg.CUBE_DEPTH/2;

        float[] xyz = new float[3];
        int start;

        for( int i = 0; i < NB_SQUIRREL_BIRTHS; i++) {
            float x = getRandomX(randomPlacementSquirrels);
            float y = getRandomY(randomPlacementSquirrels);
            float z = sg.getInternalZ(x,y,indices,true) + sg.getzTranslation();

            boolean isNearWater = Math.abs(z - zWater) < 10;
            boolean isAboveWater = z > zWater;
            boolean isNotInSnow = z - zWater < 2000;

            float z1 = sg.getInternalZ(x+0.5f,y,indices,true) + sg.getzTranslation();
            float z2 = sg.getInternalZ(x-0.5f,y,indices,true) + sg.getzTranslation();
            float z3 = sg.getInternalZ(x,y+0.5f,indices,true) + sg.getzTranslation();
            float z4 = sg.getInternalZ(x,y-0.5f,indices,true) + sg.getzTranslation();
            float d1 = Math.abs(z-z1);
            float d2 = Math.abs(z-z2);
            float d3 = Math.abs(z-z3);
            float d4 = Math.abs(z-z4);
            float dzMax = 0.35f;
            boolean isNotTooSteep = (d1<dzMax) && (d2<dzMax) && (d3<dzMax) && (d4<dzMax);

            if( !isNearWater && isAboveWater && isNotTooSteep && isNotInSnow ) {
                xyz[0] = x;
                xyz[1] = y;
                xyz[2] = z - 0.1f;
                squirrelCoords.append( xyz[0]);
                squirrelCoords.append( xyz[1]);
                squirrelCoords.append( xyz[2]);

                addInstance(i);
                nbSquirrels++;
            }
        }
    }

    float getRandomX(RandomGenerator randomPlacementSquirrel) {
        SbBox3f sceneBox = sg.getChunks().getSceneBoxFullIsland();
        float xMin = sceneBox.getBounds()[0];
        float xMax = sceneBox.getBounds()[3];
        return xMin + (xMax - xMin) * randomPlacementSquirrel.nextFloat();
    }

    float getRandomY(RandomGenerator randomPlacementSquirrel) {
        SbBox3f sceneBox = sg.getChunks().getSceneBoxFullIsland();
        float yMin = sceneBox.getBounds()[1];
        float yMax = sceneBox.getBounds()[4];
        return yMin + (yMax - yMin) * randomPlacementSquirrel.nextFloat();
    }
}
