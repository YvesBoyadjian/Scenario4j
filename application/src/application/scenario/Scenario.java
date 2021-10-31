package application.scenario;

import application.scenegraph.SceneGraphIndexedFaceSetShader;
import application.viewer.glfw.SoQtWalkViewer;

import java.util.ArrayList;
import java.util.List;

public class Scenario {

    int currentQuestIndex = 0;

    SceneGraphIndexedFaceSetShader sceneGraph;

    final List<Quest> quests = new ArrayList<>();

    public Scenario(SceneGraphIndexedFaceSetShader sceneGraph){
        this.sceneGraph = sceneGraph;
        this.sceneGraph.setScenario(this);
    }

    public void addQuest(Quest quest) {
        quest.setSceneGraph(sceneGraph);
        quests.add(quest);
    }

    public void start(int questIndex, SoQtWalkViewer viewer) {
        currentQuestIndex = questIndex;

        if(currentQuestIndex > 0) {
            quests.get(currentQuestIndex-1).actionIfNextNotAchieved(viewer);
        }
    }

    public boolean idle(SoQtWalkViewer viewer) {

        if(isOver()) {
            return false;
        }

        Quest thisQuest = quests.get(currentQuestIndex);

        if(quests.get(currentQuestIndex).isAchieved(viewer) ) {
            if(currentQuestIndex >= 0) {
                currentQuestIndex++;
            }
            if(!idle(viewer)) {
                thisQuest.actionIfNextNotAchieved(viewer);
            }
            return true;
        }
        return false;
    }

    public int getCurrentQuestIndex() {
        return currentQuestIndex;
    }

    public boolean isOver() {
        return currentQuestIndex >= quests.size();
    }
}
