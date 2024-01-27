package application.objects.collectible;

import application.scenegraph.SceneGraphIndexedFaceSetShader;
import jscenegraph.database.inventor.SbBox3f;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.engines.SoElapsedTime;
import jscenegraph.database.inventor.fields.SoMFVec3f;
import jscenegraph.database.inventor.nodes.*;

import java.io.File;
import java.util.Random;

public class BootsFamily extends ThreeDObjectFamilyBase implements ThreeDObjectFamily {

    final int FIFTY_THOUSAND = 50000;
    int NB_BOOTS_BIRTHS = FIFTY_THOUSAND;

    final int SEED_BOOTS_PLACEMENT;

    SceneGraphIndexedFaceSetShader sg;

    int nbBootPairs = 0;

    SoMFVec3f bootsCoords = new SoMFVec3f();

    SoSeparator node = new SoSeparator();

    SoElapsedTime elapsedTime = new SoElapsedTime();

    SoSwitch bootsSwitch = new SoSwitch();

    public String filePath = "ressource/hi-tec_mountain_boots_high_poly.zip";

    private boolean spin = true;

    public BootsFamily(SceneGraphIndexedFaceSetShader sg, int seed) {
        this.sg = sg;
        SEED_BOOTS_PLACEMENT = seed;

        String texturePath = filePath;

        File textureFile = new File(texturePath);

        if(!textureFile.exists()) {
            texturePath = "application/"+texturePath;
        }

        node.ref(); // To avoid destruction

        node.addChild(bootsSwitch);

        SoRotationXYZ rotXYZ = new SoRotationXYZ();

        rotXYZ.axis.setValue(SoRotationXYZ.Axis.Z);

        rotXYZ.angle.connectFrom(elapsedTime.timeOut);

        bootsSwitch.addChild(rotXYZ);

        SoRotation rot = new SoRotation();

        rot.rotation.setValue(new SbVec3f(1,0,0), (float)Math.PI/2.0f);
        bootsSwitch.addChild(rot);

        SoScale scale = new SoScale();
        scale.scaleFactor.setValue(0.3f,0.3f,0.3f);
        bootsSwitch.addChild(scale);

        SoFile file = new SoFile();

        bootsSwitch.addChild(file);

        file.name.setValue(texturePath);
    }

    @Override
    public int getNbCollectibles() {

        if (nbBootPairs == 0) {
            compute();
        }

        return nbBootPairs;
    }

    private void compute() {

        Random randomPlacementBoots = new Random(SEED_BOOTS_PLACEMENT);

        int[] indices = new int[4];

        float zWater =  - 150 + sg.getzTranslation() - sg.CUBE_DEPTH /2;

        float[] xyz = new float[3];
        int start;

        for( int i = 0; i < NB_BOOTS_BIRTHS; i++) {
            float x = getRandomX(randomPlacementBoots);
            float y = getRandomY(randomPlacementBoots);
            float z = sg.getInternalZ(x, y, indices,true) + sg.getzTranslation();

            boolean isNearWater = Math.abs(z - zWater) < 5;
            boolean isAboveWater = z > zWater;

            if(isAboveWater) {
                xyz[0] = x;
                xyz[1] = y;
                xyz[2] = z + 0.6f;
                start = nbBootPairs;//bootsCoords.getNum();
                if (start >= bootsCoords.getNum()) {
                    bootsCoords.setNum(1000+start);
                }
                bootsCoords.setValues(start, xyz);

                addInstance(i);
                nbBootPairs++;
            }
        }
        System.out.println("Boot pairs: "+nbBootPairs);
        bootsCoords.setNum(nbBootPairs);
    }

    public float[] getCollectible(int sealIndex, float[] vector) {

        if(nbBootPairs == 0) {
            compute();
        }

        SbVec3f bootPairCoords = bootsCoords.getValueAt(sealIndex);
        vector[0] = bootPairCoords.getX();
        vector[1] = bootPairCoords.getY();
        vector[2] = bootPairCoords.getZ();
        return vector;
    }

    @Override
    public SoNode getNode() {
        return node;
    }

    @Override
    public float getViewDistance() {
        return 150.0f;
    }

    float getRandomX(Random randomPlacementTrees) {
        SbBox3f sceneBox = sg.getChunks().getSceneBoxFullIsland();
        float xMin = sceneBox.getBounds()[0];
        float xMax = sceneBox.getBounds()[3];
        return xMin + (xMax - xMin) * randomPlacementTrees.nextFloat();
    }

    float getRandomY(Random randomPlacementTrees) {
        SbBox3f sceneBox = sg.getChunks().getSceneBoxFullIsland();
        float yMin = sceneBox.getBounds()[1];
        float yMax = sceneBox.getBounds()[4];
        return yMin + (yMax - yMin) * randomPlacementTrees.nextFloat();
    }

    public void setSpin(boolean b) {
        spin = b;
        elapsedTime.on.setValue(spin);
        bootsSwitch.whichChild.setValue(spin ? SoSwitch.SO_SWITCH_ALL : SoSwitch.SO_SWITCH_NONE);
    }
}