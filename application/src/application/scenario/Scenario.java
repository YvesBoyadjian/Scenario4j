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
    }

    public void addQuest(Quest quest) {
        quest.setSceneGraph(sceneGraph);
        quests.add(quest);
    }

    public void start(int questIndex) {
        currentQuestIndex = questIndex;
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

    public void setCurrentQuestIndex(int questIndex) {
        currentQuestIndex = questIndex;
    }

    public boolean isOver() {
        return currentQuestIndex >= quests.size();
    }
}
