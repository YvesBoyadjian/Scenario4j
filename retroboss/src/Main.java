import application.MainGLFW;

import javax.swing.*;
import java.util.Objects;

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
                    MainGLFW.loadSceneGraph(progressBar);
                    MainGLFW.buildScenario();
                    MainGLFW.buildViewer();
                    MainGLFW.fillViewer();
                    MainGLFW.buildPhysics();
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
}