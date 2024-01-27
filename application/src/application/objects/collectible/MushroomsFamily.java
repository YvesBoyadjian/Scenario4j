package application.objects.collectible;

import application.scenegraph.SceneGraphIndexedFaceSetShader;
import jscenegraph.coin3d.inventor.nodes.SoVertexProperty;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.fields.SoMFVec3f;
import jscenegraph.database.inventor.nodes.SoLineSet;
import jscenegraph.database.inventor.nodes.SoNode;
import jscenegraph.database.inventor.nodes.SoSeparator;

import java.util.ArrayList;
import java.util.List;

public class MushroomsFamily extends ThreeDObjectFamilyBase implements ThreeDObjectFamily {

    SceneGraphIndexedFaceSetShader sg;

    List<SbVec3f> polylinePoints;

    private int nbMushrooms;

    SoMFVec3f mushroomsCoords = new SoMFVec3f();

    boolean computed;

    SoSeparator node = new SoSeparator();
    private final SoLineSet polylineLineSet = new SoLineSet();

    public MushroomsFamily(SceneGraphIndexedFaceSetShader sg, List<SbVec3f> polylinePoints) {
        this.sg = sg;
        int nbPolylinePoints = polylinePoints.size();
        this.polylinePoints = new ArrayList<>(nbPolylinePoints);
        for (int i=0; i<nbPolylinePoints; i++) {
            this.polylinePoints.add(new SbVec3f(polylinePoints.get(i)));
        }
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
        for (int i=0; i<nbPolylinePoints-1; i++) {
            SbVec3f p1 = polylinePoints.get(i);
            SbVec3f p2 = polylinePoints.get(i+1);
            float pp1Z = sg.getZ(p1.x(),p1.y(),p1.z());
            float pp2Z = sg.getZ(p2.x(),p2.y(),p2.z());

            SbVec3f pp1 = new SbVec3f(p1.x(),p1.y(),pp1Z);
            SbVec3f pp2 = new SbVec3f(p2.x(),p2.y(),pp2Z);
        }

        SoVertexProperty vertexProperty = new SoVertexProperty();

        int polylinePointsSize = polylinePoints.size();

        for (int i = 0; i < polylinePointsSize; i++) {
            vertexProperty.vertex.set1Value(i, polylinePoints.get(i));
        }
        vertexProperty.vertex.setNum(polylinePointsSize);

        polylineLineSet.numVertices.set1Value(0, polylinePointsSize);
        polylineLineSet.vertexProperty.setValue(vertexProperty);

        node.addChild(polylineLineSet);
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
        return 150.0f;
    }
}
