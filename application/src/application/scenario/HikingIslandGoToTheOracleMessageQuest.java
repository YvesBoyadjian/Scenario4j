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
        message[0] = "Find the oracle."; 
        message[1] = "He is at the end of the path";
        message[2] = "Collect the bananas on the way";
        sceneGraph.displayTemporaryMessage(message,30);
    }
}
