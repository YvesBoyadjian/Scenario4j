package application.scenario;

import application.scenegraph.SceneGraphIndexedFaceSetShader;
import application.viewer.glfw.SoQtWalkViewer;

public class HikingIslandGoToTheOracleMessageQuest implements Quest {

    SceneGraphIndexedFaceSetShader sceneGraph;

    @Override
    public void setSceneGraph(SceneGraphIndexedFaceSetShader sceneGraph) {
        this.sceneGraph = sceneGraph;
    }

    @Override
    public boolean isAchieved(SoQtWalkViewer viewer) {
        return true;
    }

    @Override
    public void actionIfNextNotAchieved(SoQtWalkViewer viewer) {

        String[] message = new String[3];
        message[0] = "Cross the bridge and find the oracle."; 
        message[1] = "He is on the path";
        message[2] = "Collect the bananas on the way";
        sceneGraph.displayTemporaryMessage(message,30);
    }
}
