/**
 * 
 */
package retroboss;

import static application.MainGLFW.SCENE_POSITION;

import java.util.Objects;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import actor.boss.SoBoss;
import actor.house.SoHouse;
import application.MainGLFW;
import application.actor.Actor;
import application.actor.ThreeDObject;
import application.objects.Hero;
import application.scenario.FirstApproachQuest;
import application.scenario.GoToTheOracleMessageQuest;
import application.scenario.HikingIslandFirstApproachQuest;
import application.scenario.HikingIslandGoToTheOracleMessageQuest;
import application.scenario.HikingIslandLeaveHouseQuest;
import application.scenario.LeaveKlapatchePointQuest;
import application.scenario.Scenario;
import application.scenario.TargetsKillingQuest;
import application.scenegraph.SceneGraphIndexedFaceSetShader;
import application.viewer.glfw.SoQtWalkViewer;
import jscenegraph.database.inventor.SbVec3f;

/**
 * 
 */
public class HikingIslandMain {

    /**
     * @param args
     */
    public static void main(String[] args) {
        System.out.println("Bigfoot Hunting");

        if (args.length == 1 && Objects.equals(args[0], "opengl32")) {
            System.loadLibrary("opengl32");
        }

        if (args.length == 1 && Objects.equals(args[0], "god")) {
        	MainGLFW.god = true;
        }
        SwingUtilities.invokeLater(() -> {
            loadGame(MainGLFW.showSplash("Bigfoot Hunting", 1f / 5f));
        });
    }


    public static void loadGame(final JProgressBar progressBar) {

        SwingWorker sw = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                try {
                	SceneGraphIndexedFaceSetShader sg = MainGLFW.loadSceneGraph(progressBar);
                	buildScenario(sg);
                	MainGLFW.buildViewer();
                	MainGLFW.fillViewer();
                	MainGLFW.buildPhysics();

                    Hero.setStartingPosition(2947.3f, -5516.95f, 1056.32f);

                    MainGLFW.loadSavedGame();
                    SwingUtilities.invokeLater(() -> {
                        try {
                        	MainGLFW.startOpenGL();
                        	MainGLFW.startViewer();
                        	MainGLFW.loadPlanks();
                        	MainGLFW.loadBananas();
                        	MainGLFW.buildHeroPhysics();
                        	MainGLFW.setEscapeCallback();
                        	MainGLFW.addIdleListeners();
                        	MainGLFW.runVisu();
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(MainGLFW.window, e.toString(), "Exception in Bigfoot Hunting", JOptionPane.ERROR_MESSAGE);
                            e.printStackTrace();
                            System.exit(-1); // Necessary, because of Linux
                        } catch (Error e) {
                            JOptionPane.showMessageDialog(MainGLFW.window, e.toString(), "Error in Bigfoot Hunting", JOptionPane.ERROR_MESSAGE);
                            e.printStackTrace();
                            System.exit(-1); // Necessary, because of Linux
                        }
                    });
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MainGLFW.window, e.toString(), "Exception in Bigfoot Hunting", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    System.exit(-1); // Necessary, because of Linux
                } catch (Error e) {
                    JOptionPane.showMessageDialog(MainGLFW.window, e.toString(), "Error in Bigfoot Hunting", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    System.exit(-1); // Necessary, because of Linux
                }
                return null;
            }
        };

        SwingUtilities.invokeLater(() -> {
            sw.execute();
        });
    }

    public static void buildScenario(SceneGraphIndexedFaceSetShader sg) {

        // _____________________________________________________ Story
    	MainGLFW.scenario = new Scenario(sg) {
            @Override
            public void start(int questIndex, SoQtWalkViewer viewer) {
                super.start(questIndex, viewer);
                Actor boss;
                if (!sg.hasActor("boss")) {
                    sg.addActor("boss", boss = new SoBoss());
                }
                else {
                    boss = sg.getActor("boss");
                }
                if (questIndex == 0 || true) {
                    SbVec3f bossPosition = new SbVec3f(2571,-69.5f,937 - SCENE_POSITION.getZ());
                    final int[] catPositionIndices = new int[4];
                    bossPosition.setZ(sg.getInternalZ(bossPosition.getX(), bossPosition.getY(),catPositionIndices,false));
                    boss.setPosition(bossPosition);
                }
                
                Actor house;
                if(!sg.hasActor("house")) {
                	sg.addActor("house", house = new SoHouse());
                }
                else {
                	house = sg.getActor("house");
                }
                SbVec3f housePosition = new SbVec3f(2947.3f, -5516.95f, 1056.32f - SCENE_POSITION.getZ() - 1.7f);
                house.setPosition(housePosition);
            }
        };

        // __________________________ Display "Go to the Oracle" message
    	MainGLFW.scenario.addQuest(new HikingIslandGoToTheOracleMessageQuest());
        // __________________________________________ Leave Klapatche point
    	MainGLFW.scenario.addQuest(new HikingIslandLeaveHouseQuest());
        // __________________________________________ Oracle encounter
    	MainGLFW.scenario.addQuest(new HikingIslandFirstApproachQuest());
        // __________________________________________ Killing targets
    	MainGLFW.scenario.addQuest(new TargetsKillingQuest());

    }
    
}
