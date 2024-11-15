import application.MainGLFW;
import application.actor.Actor;
import application.objects.Hero;
import application.scenario.FirstApproachQuest;
import application.scenario.Scenario;
import application.scenario.TargetsKillingQuest;
import application.scenegraph.SceneGraphIndexedFaceSetShader;
import application.viewer.glfw.SoQtWalkViewer;
import actor.boss.SoBoss;
import jscenegraph.database.inventor.SbVec3f;

import javax.swing.*;
import java.util.Objects;

import static application.MainGLFW.SCENE_POSITION;

public class Main {
    public static void main(String[] args) {
        System.out.println("Retro Boss!");

        if (args.length == 1 && Objects.equals(args[0], "opengl32")) {
            System.loadLibrary("opengl32");
        }

        if (args.length == 1 && Objects.equals(args[0], "god")) {
            MainGLFW.god = true;
        }
        SwingUtilities.invokeLater(() -> {
            loadGame(MainGLFW.showSplash("Retro Boss", 1f / 4f));
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

                    Hero.setStartingPosition(2532f, -69.5f, 933f);

                    MainGLFW.loadSavedGame();
                    SwingUtilities.invokeLater(() -> {
                        try {
                        MainGLFW.startOpenGL();
                        MainGLFW.startViewer();
                        MainGLFW.loadPlanks();
                        MainGLFW.buildHeroPhysics();
                        MainGLFW.setEscapeCallback();
                        MainGLFW.addIdleListeners();
                        MainGLFW.runVisu();
                        } catch (Exception e) {
                            JOptionPane.showMessageDialog(MainGLFW.window, e.toString(), "Exception in Retro Boss", JOptionPane.ERROR_MESSAGE);
                            e.printStackTrace();
                            System.exit(-1); // Necessary, because of Linux
                        } catch (Error e) {
                            JOptionPane.showMessageDialog(MainGLFW.window, e.toString(), "Error in Retro Boss", JOptionPane.ERROR_MESSAGE);
                            e.printStackTrace();
                            System.exit(-1); // Necessary, because of Linux
                        }
                    });
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(MainGLFW.window, e.toString(), "Exception in Retro Boss", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    System.exit(-1); // Necessary, because of Linux
                } catch (Error e) {
                    JOptionPane.showMessageDialog(MainGLFW.window, e.toString(), "Error in Retro Boss", JOptionPane.ERROR_MESSAGE);
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
                if (questIndex == 0) {
                    SbVec3f bossPosition = new SbVec3f(2571,-69.5f,937 - SCENE_POSITION.getZ());
                    final int[] catPositionIndices = new int[4];
                    bossPosition.setZ(sg.getInternalZ(bossPosition.getX(), bossPosition.getY(),catPositionIndices,false));
                    boss.setPosition(bossPosition);
                }
            }
        };

        // __________________________________________ Leave Klapatche point
        //MainGLFW.scenario.addQuest(new LeaveKlapatchePointQuest());
        // __________________________________________ Oracle encounter
        MainGLFW.scenario.addQuest(new FirstApproachQuest());
        // __________________________________________ Killing targets
        MainGLFW.scenario.addQuest(new TargetsKillingQuest());

    }
}