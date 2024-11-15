package application.scenario;

import application.scenegraph.SceneGraphIndexedFaceSetShader;
import application.viewer.glfw.SoQtWalkViewer;

public class GoToTheOracleMessageQuest implements Quest {

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

        String[] message = new String[2];
        message[0] = "Go to the oracle."; message[1] = "He is on the right on the path";
        sceneGraph.displayTemporaryMessage(message,30);
    }
}
