package application.scenario;

import application.MainGLFW;
import application.scenegraph.*;
import application.viewer.glfw.SoQtWalkViewer;
import jscenegraph.database.inventor.nodes.SoCamera;

public class TargetsKillingQuest implements Quest {

    public final float NEAR_DISTANCE = 8;

    SceneGraphIndexedFaceSetShader sceneGraph;

    @Override
    public void setSceneGraph(SceneGraphIndexedFaceSetShader sceneGraph) {
        this.sceneGraph = sceneGraph;
    }

    @Override
    public boolean isAchieved(SoQtWalkViewer viewer) {
        boolean achieved = getDistanceFromOracle(viewer) <= NEAR_DISTANCE;
        boolean allKilled = true;
        allKilled &= sceneGraph.haveShot(GroundSquirrels.GROUND_SQUIRREL_NAME);
        allKilled &= sceneGraph.haveShot(HoaryMarmots.HOARY_MARMOT_NAME);
        allKilled &= sceneGraph.haveShot(Seals.SEAL_NAME);
        allKilled &= sceneGraph.haveShot(MountainGoats.MOUNTAIN_GOAT_NAME);
        allKilled &= sceneGraph.haveShot(Owls.SPOTTED_OWL_NAME);
        allKilled &= sceneGraph.haveShot(Owls.BARRED_OWL_NAME);
        allKilled &= sceneGraph.haveShot(BigFoots.BIGFOOT_NAME);

        achieved &= allKilled;

        sceneGraph.setSearchForSea(false);

        if(!achieved) {
            boolean onlyMissingSeal = !sceneGraph.haveShot(Seals.SEAL_NAME);
            onlyMissingSeal &= sceneGraph.haveShot(GroundSquirrels.GROUND_SQUIRREL_NAME);
            onlyMissingSeal &= sceneGraph.haveShot(HoaryMarmots.HOARY_MARMOT_NAME);
            onlyMissingSeal &= sceneGraph.haveShot(MountainGoats.MOUNTAIN_GOAT_NAME);
            onlyMissingSeal &= sceneGraph.haveShot(Owls.SPOTTED_OWL_NAME);
            onlyMissingSeal &= sceneGraph.haveShot(Owls.BARRED_OWL_NAME);
            onlyMissingSeal &= sceneGraph.haveShot(BigFoots.BIGFOOT_NAME);

            if(onlyMissingSeal) {
                sceneGraph.setMessage("You will find seals by the sea, on the beach");
            }
            else {
                if(allKilled) {
                    sceneGraph.setMessage("Go back to the oracle by taking the trail, he has a present for you");
                }
                else {
                    sceneGraph.setMessage("");
                }
            }
            sceneGraph.setSearchForSea(onlyMissingSeal);
        }
        return achieved;
    }

    @Override
    public void actionIfNextNotAchieved(SoQtWalkViewer viewer) {
        viewer.setAllowToggleFly(true);
        String[] speech = {"Hooray, I now have enough to eat.","To show my gratitude, I'm allowing you to fly", "by pressing the 'F' key."};
        sceneGraph.talk(speech);
        sceneGraph.stopBody();
        sceneGraph.setMessage("'F' key to toggle fly mode On or Off");
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
