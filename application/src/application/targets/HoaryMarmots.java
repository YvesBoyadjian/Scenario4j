package application.targets;

import application.objects.Target;
import application.objects.TargetBase;
import application.scenegraph.SceneGraphIndexedFaceSetShader;
import jscenegraph.coin3d.inventor.lists.SbListFloat;
import jscenegraph.database.inventor.SbBox3f;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.fields.SoMFVec3f;

import java.util.Random;

public class HoaryMarmots extends TargetBase implements Target {

    public static final String HOARY_MARMOT_NAME = "Marmot";

    @Override
    public String targetName() {
        return HOARY_MARMOT_NAME;
    }

    @Override
    public String getTexturePath() {
        return "ressource/Hoary_marmot_rainier_2008.jpg";
    }

    @Override
    public int getNbTargets() {

        if( nbMarmots == 0 ) {
            compute();
        }
        return nbMarmots;
    }

    @Override
    public float[] getTarget(int marmotIndex, float[] vector) {

        if (nbMarmots == 0) {
            compute();
        }

        vector[0] = marmotCoords.get(marmotIndex*3);
        vector[1] = marmotCoords.get(marmotIndex*3+1);
        vector[2] = marmotCoords.get(marmotIndex*3+2);

        return vector;
    }

    @Override
    public float getSize() {
        return 0.6f;
    }

    @Override
    public float getRatio() {
        return 1707.0f/1219.0f;
    }

    @Override
    public float getViewDistance() {
        return 500;
    }

    SceneGraphIndexedFaceSetShader sg;

    int nbMarmots = 0;

    SbListFloat marmotCoords = new SbListFloat();

    final int HUNDRED_THOUSAND = 100000;

    int NB_MARMOT_BIRTHS = HUNDRED_THOUSAND;

    final static int SEED_MARMOT_PLACEMENT = 51;

    public HoaryMarmots( SceneGraphIndexedFaceSetShader sg ) {
        this.sg = sg;
    }

    private void compute() {

        Random randomPlacementBigFoots = new Random(SEED_MARMOT_PLACEMENT);

        int[] indices = new int[4];

        float zWater = - 150 + sg.getzTranslation() - sg.CUBE_DEPTH/2;

        for( int i = 0; i < NB_MARMOT_BIRTHS; i++) {
            float x = getRandomX(randomPlacementBigFoots);
            float y = getRandomY(randomPlacementBigFoots);
            float z = sg.getInternalZ(x,y,indices,true) + sg.getzTranslation();

            boolean isNearWater = Math.abs(z - zWater) < 150;
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
                marmotCoords.append(x);
                marmotCoords.append(y);
                marmotCoords.append(z);

                addInstance(i);
                nbMarmots++;
            }
        }
    }

    float getRandomX(Random randomPlacementMarmot) {
        SbBox3f sceneBox = sg.getChunks().getSceneBoxFullIsland();
        float xMin = sceneBox.getBounds()[0];
        float xMax = sceneBox.getBounds()[3];
        return xMin + (xMax - xMin) * randomPlacementMarmot.nextFloat();
    }

    float getRandomY(Random randomPlacementMarmot) {
        SbBox3f sceneBox = sg.getChunks().getSceneBoxFullIsland();
        float yMin = sceneBox.getBounds()[1];
        float yMax = sceneBox.getBounds()[4];
        return yMin + (yMax - yMin) * randomPlacementMarmot.nextFloat();
    }
}
