/**
 * 
 */
package jexample.parts;


import jscenegraph.database.inventor.SbName;
import jscenegraph.database.inventor.SoPath;
import jscenegraph.database.inventor.actions.SoGLRenderAction;
import jscenegraph.database.inventor.actions.SoSearchAction;
import jscenegraph.database.inventor.nodes.SoAnnotation;
import jscenegraph.database.inventor.nodes.SoMaterial;
import jscenegraph.database.inventor.nodes.SoNode;
import jscenegraph.database.inventor.nodes.SoSeparator;
import jscenegraph.interaction.inventor.SoInteraction;
import jscenegraph.interaction.inventor.draggers.SoCenterballDragger;
import jscenegraph.port.Destroyable;
import jsceneviewerawt.inventor.qt.SoQt;
import jsceneviewerawt.inventor.qt.SoQtCameraController;
import jsceneviewerawt.inventor.qt.viewers.SoQtExaminerViewer;
import jsceneviewerawt.inventor.qt.viewers.SoQtFullViewer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

/**
 * @author BOYADJIAN
 *
 */
public class SegFault {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		JFrame frame = new JFrame("VRMLViewer");
		frame.getContentPane().setBackground(new Color(0,true));
		frame.getContentPane().setLayout(new BorderLayout());
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setLocationRelativeTo(null);

		SwingUtilities.invokeLater(() -> {
		SoQt.init();
		
		// An Annotation node must be used here because it delays the rendering
		SoSeparator anno = new SoAnnotation();
		

		int style = 0;
		
		final boolean[] first = new boolean[1];
		first[0] = true;
		
		SoQtExaminerViewer eviewer = new SoQtExaminerViewer(SoQtFullViewer.BuildFlag.BUILD_ALL, SoQtCameraController.Type.BROWSER,frame.getContentPane(),style)
				{
				// Use this function to add an Annotation node after the
				// scene has been rendered
				public void setViewing(boolean enable) {
					if( first[0] ) {
						first[0] = false;
						super.setViewing(enable);
						return;
					}
					SoNode root = getSceneHandler().getSceneGraph();

					SoSearchAction searchAction = new SoSearchAction();
					searchAction.setType(SoSeparator.getClassTypeId());
					searchAction.setInterest(SoSearchAction.Interest.FIRST);
					searchAction.setName(new SbName("GroupOnTopPreSel"));
					searchAction.apply(root);
					SoPath selectionPath = searchAction.getPath();

					if (selectionPath != null) {
						SoSeparator sep = (SoSeparator)(selectionPath.getTail());
						sep.addChild(anno);
					}
					Destroyable.delete(searchAction);
				}
				};
		SoInteraction.init();

		SoSeparator root = new SoSeparator();
		root.ref();

		SoMaterial mat = new SoMaterial();
		mat.transparency.setValue(0.5f);
		mat.diffuseColor.setIgnored(true);
		mat.setOverride(true);

		SoSeparator edit = new SoSeparator();
		edit.setName("GroupOnTopPreSel");
		edit.addChild(mat);
		root.addChild(edit);

		SoSeparator view = new SoSeparator();
		view.renderCaching.setValue(SoSeparator.CacheEnabled.AUTO);
		view.addChild(new SoCenterballDragger/*SoTranslate1Dragger*/());

		root.addChild(view);

		anno.ref();
		anno.addChild(view);

//		QWidget* mainwin = SoQt::init(argc, argv, argv[0]);
		//HWND mainwin = SoWin::init(argv[0]);
		//MyViewer* eviewer = new MyViewer(mainwin, anno);

		// Transparency type must be set to SORTED_OBJECT_SORTED_TRIANGLE_BLEND in order
		// to activate the caching mechanism
		SoGLRenderAction glAction = eviewer.getSceneHandler().getGLRenderAction();
		glAction.setTransparencyType(SoGLRenderAction.TransparencyType.SORTED_OBJECT_SORTED_TRIANGLE_BLEND);

	    eviewer.buildWidget(style);
	    
		eviewer.setSceneGraph(root);

		frame.pack();
		frame.setSize(800,600);
		frame.setVisible(true);


//		eviewer.show();
//
//		SoWin::show(mainwin);
//		SoWin::mainLoop();

		//delete eviewer;
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
					anno.unref();
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
		//SoWin::done();
		//return 0;
		});
	}

}
