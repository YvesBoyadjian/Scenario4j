package application.objects.collectible;

import application.scenegraph.SceneGraphIndexedFaceSetShader;
import jscenegraph.coin3d.inventor.nodes.SoVertexProperty;
import jscenegraph.coin3d.shaders.inventor.nodes.SoShaderProgram;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.actions.SoGLRenderAction;
import jscenegraph.database.inventor.fields.SoMFVec3f;
import jscenegraph.database.inventor.nodes.SoFile;
import jscenegraph.database.inventor.nodes.SoLineSet;
import jscenegraph.database.inventor.nodes.SoNode;
import jscenegraph.database.inventor.nodes.SoSeparator;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MushroomsFamily extends ThreeDObjectFamilyBase implements ThreeDObjectFamily {

    SceneGraphIndexedFaceSetShader sg;

    List<SbVec3f> polylinePoints;

    List<SbVec3f> polylinePointsOnLand;

    private int nbMushrooms;

    SoMFVec3f mushroomsCoords = new SoMFVec3f();

    boolean computed;

    SoSeparator node = new SoSeparator();
    private final SoLineSet polylineLineSet = new SoLineSet();

    public String filePath = "ressource/MushroomButton_L2.123c905c8c0c-b164-45b5-ae21-cd1ca6951a92.zip";

    public MushroomsFamily(SceneGraphIndexedFaceSetShader sg, List<SbVec3f> polylinePoints, SoShaderProgram program2) {
        this.sg = sg;
        int nbPolylinePoints = polylinePoints.size();
        this.polylinePoints = new ArrayList<>(nbPolylinePoints);
        for (int i=0; i<nbPolylinePoints; i++) {
            this.polylinePoints.add(new SbVec3f(polylinePoints.get(i)));
        }
//        node.addChild(program2);

        String mushroomPath = filePath;

        File mushroomFile = new File(mushroomPath);

        if(!mushroomFile.exists()) {
            mushroomPath = "application/"+mushroomPath;
        }

        SoFile file = new SoFile() {
            @Override
            public void GLRender(SoGLRenderAction action) {
                super.GLRender(action);
            }
        };

        node.addChild(file);

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

        for (int i=0; i<nbPolylinePoints-1; i++) {
            SbVec3f p1 = polylinePoints.get(i);
            SbVec3f p2 = polylinePoints.get(i+1);
            float pp1Z = sg.getInternalZ(p1.x(),p1.y(),indices,true) + sg.getzTranslation();
            float pp2Z = sg.getInternalZ(p2.x(),p2.y(),indices,true) + sg.getzTranslation();

            SbVec3f pp1 = new SbVec3f(p1.x(),p1.y(),pp1Z+0.5f);
            SbVec3f pp2 = new SbVec3f(p2.x(),p2.y(),pp2Z+0.5f);

            polylinePointsOnLand.add(pp1);
            if (i == nbPolylinePoints-2) {
                polylinePointsOnLand.add(pp2);
            }
        }

        SoVertexProperty vertexProperty = new SoVertexProperty();

        int polylinePointsSize = polylinePointsOnLand.size();

        for (int i = 0; i < polylinePointsSize; i++) {
            vertexProperty.vertex.set1Value(i, polylinePointsOnLand.get(i));
        }
        vertexProperty.vertex.setNum(polylinePointsSize);

        polylineLineSet.numVertices.set1Value(0, polylinePointsSize);
        polylineLineSet.vertexProperty.setValue(vertexProperty);

//        node.addChild(polylineLineSet);

        for (int i=0; i<nbPolylinePoints; i++) {
            mushroomsCoords.set1Value(i,polylinePointsOnLand.get(i));
            addInstance(i);
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
    public SoNode getNode() {
        return node;
    }

    @Override
    public float getViewDistance() {
        return 99999;
    }
}
