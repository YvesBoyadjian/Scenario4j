/**
 * 
 */
package jexample.parts;


import jscenegraph.database.inventor.SoDB;
import jscenegraph.database.inventor.SoInput;
import jscenegraph.database.inventor.nodes.SoSeparator;
import jsceneviewerawt.inventor.qt.SoQt;
import jsceneviewerawt.inventor.qt.SoQtCameraController;
import jsceneviewerawt.inventor.qt.viewers.SoQtExaminerViewer;
import jsceneviewerawt.inventor.qt.viewers.SoQtFullViewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * @author Yves Boyadjian
 *
 */
public class Input {


public static SoSeparator
readFile( String filename)
{
   // Open the input file
   final SoInput mySceneInput = new SoInput();
   if (!mySceneInput.openFile(filename)) {
      System.err.printf("Cannot open file "+filename+"\n");
      System.exit(1);
   }

   // Read the whole file into the database
   SoSeparator myGraph = SoDB.readAll(mySceneInput);
   if (myGraph == null) {
      System.err.printf("Problem reading file\n");
      System.exit(1);
   }
   mySceneInput.closeFile();
   mySceneInput.destructor();
   return myGraph;
}

public static void main(String[] argv)
{

    JFrame frame = new JFrame("VRMLViewer");
    frame.getContentPane().setBackground(new Color(0,true));
    frame.getContentPane().setLayout(new BorderLayout());
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setLocationRelativeTo(null);

  // Initialize the Qt system:
  //QApplication app(argc, argv);

  // Make a main window:
//  QWidget mainwin;
//  mainwin.resize(400,400);

    SwingUtilities.invokeLater(() -> {

  // Initialize SoQt
  SoQt.init("");

  // The root of a scene graph
  SoSeparator root = new SoSeparator();
  root.ref();

  root.addChild(readFile("examples_iv/data/test.iv"));
  //root.addChild(readFile("examples_iv/data/sphere1.iv"));

  // Initialize an examiner viewer:
  SoQtExaminerViewer eviewer = new SoQtExaminerViewer(SoQtFullViewer.BuildFlag.BUILD_ALL, SoQtCameraController.Type.BROWSER,frame.getContentPane());
  
  //eviewer.buildWidget(SWT.NO_BACKGROUND);
  
  eviewer.setSceneGraph(root);
  eviewer.show();

  // Pop up the main window.
  //SoQt.show(/*mainwin*/);
    frame.pack();
    frame.setSize(800,600);
    frame.setVisible(true);

  // Loop until exit.
  //SoQt.mainLoop();

  // Clean up resources.
  //eviewer.destructor();
    frame.addWindowListener(new WindowListener() {
        @Override
        public void windowOpened(WindowEvent e) {

        }

        @Override
        public void windowClosing(WindowEvent e) {

        }

        @Override
        public void windowClosed(WindowEvent e) {
            root.unref();
        }

        @Override
        public void windowIconified(WindowEvent e) {

        }

        @Override
        public void windowDeiconified(WindowEvent e) {

        }

        @Override
        public void windowActivated(WindowEvent e) {

        }

        @Override
        public void windowDeactivated(WindowEvent e) {

        }
    });
    });
}
}
