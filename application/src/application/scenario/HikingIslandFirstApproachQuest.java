package application.scenario;

import application.MainGLFW;
import application.scenegraph.SceneGraphIndexedFaceSetShader;
import application.viewer.glfw.SoQtWalkViewer;
import jscenegraph.database.inventor.nodes.SoCamera;

public class HikingIslandFirstApproachQuest implements Quest {

    public final float NEAR_DISTANCE = 8;

    SceneGraphIndexedFaceSetShader sceneGraph;

    @Override
    public void setSceneGraph(SceneGraphIndexedFaceSetShader sceneGraph) {
        this.sceneGraph = sceneGraph;
    }

    @Override
    public boolean isAchieved(SoQtWalkViewer viewer) {
        boolean achieved = getDistanceFromOracle(viewer) <= NEAR_DISTANCE;
        if(achieved) {
            System.out.println("Oracle found");
            sceneGraph.exchangeBananasForBullets();
        }
        sceneGraph.setMessage("");
        return achieved;
    }

    @Override
    public void actionIfNextNotAchieved(SoQtWalkViewer viewer) {
        String[] speech = {
        		"Hello, I am the Oracle. I am hungry.",
        		"Get me a squirrel, a marmot, a seal, a goat,",
        		"a spotted owl, a barred owl, and of course, a big foot.",
        		"I exchange your bananas for bullets for hunting."};
        sceneGraph.talk(speech);
        sceneGraph.stopBody();
        sceneGraph.showOracleObjective(false);
    }

    double getDistanceFromOracle(SoQtWalkViewer viewer) {
        SoCamera camera = viewer.getCameraController().getCamera();
        float x = camera.position.getValue().x();
        float y = camera.position.getValue().y();
        float z = camera.position.getValue().z() + MainGLFW.Z_TRANSLATION;

        float xOracle = SceneGraphIndexedFaceSetShader.ORACLE_X;
        float yOracle = SceneGraphIndexedFaceSetShader.ORACLE_Y;
        float zOracle = SceneGraphIndexedFaceSetShader.ORACLE_Z;

        return Math.sqrt(Math.pow(x-xOracle,2)+Math.pow(y-yOracle,2)+Math.pow(z-zOracle,2));
    }
}
