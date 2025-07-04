package application.scenario;

import application.objects.Hero;
import application.scenegraph.SceneGraphIndexedFaceSetShader;
import application.viewer.glfw.SoQtWalkViewer;
import jscenegraph.database.inventor.nodes.SoCamera;

public class HikingIslandLeaveHouseQuest implements Quest {

    SceneGraphIndexedFaceSetShader sceneGraph;

    @Override
    public void setSceneGraph(SceneGraphIndexedFaceSetShader sceneGraph) {
        this.sceneGraph = sceneGraph;
    }

    @Override
    public boolean isAchieved(SoQtWalkViewer viewer) {
        boolean achieved = getDistanceFromKlapatche(viewer) > 20;
        if(achieved) {
            sceneGraph.setMessage("");
        }
//        else {
//            sceneGraph.setMessage("You are at Klapatche Point. Your fate is on the trail");
//            sceneGraph.showOracleObjective(true);
//        }
        return achieved;
    }

    @Override
    public void actionIfNextNotAchieved(SoQtWalkViewer viewer) {
        String[] speech ={""};
        sceneGraph.talk(speech);
        sceneGraph.showOracleObjective(true);
    }

    double getDistanceFromKlapatche(SoQtWalkViewer viewer) {
        SoCamera camera = viewer.getCameraController().getCamera();
        float x = camera.position.getValue().x();
        float y = camera.position.getValue().y();

        float xStart = Hero.STARTING_X;
        float yStart = Hero.STARTING_Y;

        return Math.sqrt(Math.pow(x-xStart,2)+Math.pow(y-yStart,2));
    }
}
