/**
 * 
 */
package application;

import java.util.Objects;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import application.objects.Hero;
import application.scenario.FirstApproachQuest;
import application.scenario.GoToTheOracleMessageQuest;
import application.scenario.LeaveKlapatchePointQuest;
import application.scenario.Scenario;
import application.scenario.TargetsKillingQuest;
import application.scenegraph.SceneGraphIndexedFaceSetShader;

/**
 * 
 */
public class BigfootHuntingMain {

    /**
     * @param args
     */
    public static void main(String[] args) {

        if (args.length == 1 && Objects.equals(args[0], "opengl32")) {
            System.loadLibrary("opengl32");
        }

        if (args.length == 1 && Objects.equals(args[0], "god")) {
        	MainGLFW.god = true;
        }
        SwingUtilities.invokeLater(() -> {
            loadGame(MainGLFW.showSplash("Bigfoot Hunting, an Adventure Game", 1f / 20f));
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

                    Hero.setStartingPosition(260f, 294f, 1255.5f);

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
    	MainGLFW.scenario = new Scenario(sg);

        // __________________________ Display "Go to the Oracle" message
    	MainGLFW.scenario.addQuest(new GoToTheOracleMessageQuest());
        // __________________________________________ Leave Klapatche point
    	MainGLFW.scenario.addQuest(new LeaveKlapatchePointQuest());
        // __________________________________________ Oracle encounter
    	MainGLFW.scenario.addQuest(new FirstApproachQuest());
        // __________________________________________ Killing targets
    	MainGLFW.scenario.addQuest(new TargetsKillingQuest());

    }
    
}
