package application.objects.collectible;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.SplittableRandom;
import java.util.random.RandomGenerator;

import application.scenegraph.SceneGraphIndexedFaceSetShader;
import jscenegraph.coin3d.shaders.inventor.nodes.SoShaderProgram;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.engines.SoElapsedTime;
import jscenegraph.database.inventor.fields.SoMFVec3f;
import jscenegraph.database.inventor.nodes.SoFile;
import jscenegraph.database.inventor.nodes.SoNode;
import jscenegraph.database.inventor.nodes.SoRotationXYZ;
import jscenegraph.database.inventor.nodes.SoScale;
import jscenegraph.database.inventor.nodes.SoSeparator;

public class BananaFamily extends ThreeDObjectFamilyBase implements ThreeDObjectFamily {

    private static final float MUSHROOM_SCALE_FACTOR = 0.01f;

    SceneGraphIndexedFaceSetShader sg;

    List<SbVec3f> polylinePoints;

    public String filePath = "ressource/Banana_v01_L3.123c02dda9fc-71ec-4599-b568-288bb4a85510.zip";
    
    SoFile file = new SoFile();

    private int nbMushrooms;

    boolean computed;

    List<SbVec3f> polylinePointsOnLand;

    float[] scaleFactors;

    SoNode[] nodes;

    SoElapsedTime elapsedTime = new SoElapsedTime();

    SoMFVec3f mushroomsCoords = new SoMFVec3f();

    public BananaFamily(SceneGraphIndexedFaceSetShader sg, List<SbVec3f> polylinePoints, SoShaderProgram program2) {
        this.sg = sg;
        int nbPolylinePoints = polylinePoints.size();
        this.polylinePoints = new ArrayList<>(nbPolylinePoints);
        for (int i=0; i<nbPolylinePoints; i++) {
            this.polylinePoints.add(new SbVec3f(polylinePoints.get(i)));
        }

        String mushroomPath = filePath;

        File mushroomFile = new File(mushroomPath);

        if(!mushroomFile.exists()) {
            mushroomPath = "application/"+mushroomPath;
        }

        file.ref();

        file.name.setValue(mushroomPath);

    }

	@Override
	public int getNbCollectibles() {

        if (!computed) {
            computed = true;
            doCompute();
        }
        return nbMushrooms;
	}
    private void doCompute() {
        int nbPolylinePoints = polylinePoints.size();
        polylinePointsOnLand = new ArrayList<>();

        int[] indices = new int[4];

        RandomGenerator random = new SplittableRandom(42);
        
        float remainingCurviligne = 0;

        for (int i=0; i<nbPolylinePoints-1; i++) {
            SbVec3f p1 = polylinePoints.get(i);
            SbVec3f p2 = polylinePoints.get(i+1);

            float distance = p2.operator_minus(p1).length();

            float lambda = 1.5f;

            float curviligne;
            for (curviligne = remainingCurviligne; curviligne<distance;curviligne += 5.0f) {

                if(random.nextFloat() > 0.2f) {
                    continue;
                }

                float alpha = curviligne/distance;
                float beta = 1 - alpha;

                float dxy = -(1 / lambda) * (float)Math.log( 1.0 - random.nextDouble() * 0.9 );
                float angle = (float)Math.PI*2*random.nextFloat();
                float dx = dxy * (float)Math.sin(angle);
                float dy = dxy * (float)Math.cos(angle);

                SbVec3f p = p1.operator_mul(alpha).operator_add(p2.operator_mul(beta)).operator_add(new SbVec3f(dx,dy,0));
                float ppZ = sg.getInternalZ(p.x(),p.y(),indices,true) + sg.getzTranslation();
                SbVec3f pp = new SbVec3f(p.x(),p.y(),ppZ);
                polylinePointsOnLand.add(pp);
            }
            
            remainingCurviligne = curviligne - distance;

//            float pp1Z = sg.getInternalZ(p1.x(),p1.y(),indices,true) + sg.getzTranslation();
//            float pp2Z = sg.getInternalZ(p2.x(),p2.y(),indices,true) + sg.getzTranslation();
//
//            SbVec3f pp1 = new SbVec3f(p1.x(),p1.y(),pp1Z+0.1f);
//            SbVec3f pp2 = new SbVec3f(p2.x(),p2.y(),pp2Z+0.1f);
//
//            polylinePointsOnLand.add(pp1);
//            if (i == nbPolylinePoints-2) {
//                polylinePointsOnLand.add(pp2);
//            }
        }

//        SoVertexProperty vertexProperty = new SoVertexProperty();
//
        int polylinePointsSize = polylinePointsOnLand.size();
//
//        for (int i = 0; i < polylinePointsSize; i++) {
//            vertexProperty.vertex.set1Value(i, polylinePointsOnLand.get(i));
//        }
//        vertexProperty.vertex.setNum(polylinePointsSize);
//
//        polylineLineSet.numVertices.set1Value(0, polylinePointsSize);
//        polylineLineSet.vertexProperty.setValue(vertexProperty);

//        node.addChild(polylineLineSet);

        RandomGenerator randomS = new SplittableRandom(43);

        scaleFactors = new float[polylinePointsSize];
        nodes = new SoNode[polylinePointsSize];

        SoRotationXYZ rotXYZ = new SoRotationXYZ();

        rotXYZ.axis.setValue(SoRotationXYZ.Axis.Z);

        rotXYZ.angle.connectFrom(elapsedTime.timeOut);

        for (int i=0; i<polylinePointsSize; i++) {
            mushroomsCoords.set1Value(i,polylinePointsOnLand.get(i));
            addInstance(i);
            scaleFactors[i] = ((float)Math.pow(randomS.nextFloat(),10)+1.0f) * MUSHROOM_SCALE_FACTOR;
            SoSeparator node = new SoSeparator();

            node.ref();

            SoScale scale = new SoScale();
            scale.scaleFactor.setValue(scaleFactors[i],scaleFactors[i],scaleFactors[i]);

            node.addChild(scale);

            node.addChild(rotXYZ);

            node.addChild(file);

            nodes[i] = node;
        }

        nbMushrooms = polylinePointsSize;
    }


	@Override
	public float[] getCollectible(int sealIndex, float[] vector) {
        SbVec3f mushroomCoords = mushroomsCoords.getValueAt(sealIndex);
        vector[0] = mushroomCoords.getX();
        vector[1] = mushroomCoords.getY();
        vector[2] = mushroomCoords.getZ();
        return vector;
	}

	@Override
	public SoNode getNode(int index) {
        return nodes[index];
	}

	@Override
	public float getViewDistance() {
        return 350;
	}

}
