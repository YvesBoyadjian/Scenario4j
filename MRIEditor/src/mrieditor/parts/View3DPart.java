/**
 * 
 */
package mrieditor.parts;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.JProgressBar;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.widgets.TextFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import application.MainGLFW;
import application.RasterProvider;
import application.gui.OptionDialog;
import application.objects.Hero;
import application.scenegraph.SceneGraphIndexedFaceSetShader;
import application.swt.SoQtWalkViewer;
import application.terrain.IslandLoader;
import application.trails.TrailsLoader;
import jscenegraph.coin3d.inventor.SbVec2i32;
import jscenegraph.database.inventor.SbTime;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.SoDB;
import jscenegraph.database.inventor.actions.SoGLRenderAction.TransparencyType;
import jscenegraph.database.inventor.nodes.SoCamera;
import jsceneviewer.inventor.qt.SoQt;
import jsceneviewer.inventor.qt.SoQtCameraController.Type;
import jsceneviewer.inventor.qt.SoQtGLWidget;
import jsceneviewer.inventor.qt.SoQtGLWidget.EventType;
import jsceneviewer.inventor.qt.SoQtGLWidget.eventCBType;
import jsceneviewer.inventor.qt.viewers.SoQtFullViewer.BuildFlag;

/**
 * 
 */
public class View3DPart {

	private TableViewer tableViewer;
	
	private SoQtWalkViewer walkViewer;
	
	private boolean mouseDown;

	@Inject
	private MPart part;

	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		Button button = new Button(parent, SWT.PUSH);
		button.setText("Load 3D Model");
		
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				load3DModel();				
			}
		});
		
		Composite intermediate = new Composite(parent,SWT.NONE);
		intermediate.setLayout(new FillLayout());
		intermediate.setLayoutData(new GridData(GridData.FILL_BOTH));

//		TextFactory.newText(SWT.BORDER) //
//				.message("Enter text to mark part as dirty") //
//				.onModify(e -> part.setDirty(true)) //
//				.layoutData(new GridData(GridData.FILL_HORIZONTAL))//
//				.create(parent);

//		tableViewer = new TableViewer(parent);
//
//		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
//		tableViewer.setInput(createInitialDataModel());
//		tableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));

        SoQt.init("MRIEditor");

		SoDB.setDelaySensorTimeout(SbTime.zero()); // Necessary to avoid bug in Display
		
		int style = SWT.NO_BACKGROUND;
		walkViewer = new SoQtWalkViewer(BuildFlag.BUILD_ALL,Type.BROWSER,intermediate,style) {

		    protected void processMouseMoveEvent(/*MouseEvent e*/) {
		    	if (mouseDown) {
		    		super.processMouseMoveEvent();
		    	}
		    	else {
		            /* Zjisteni zmeny pozice kurzoru. */
		            final SbVec2i32 position = getCursorPosition();
		            old_position.copyFrom( position );

		    	}
		    }
		};
        walkViewer.buildWidget(style);
	}

	@Focus
	public void setFocus() {
//		tableViewer.getTable().setFocus();
		walkViewer.setFocus();
	}

	@Persist
	public void save() {
		part.setDirty(false);
	}

	private List<String> createInitialDataModel() {
		return Arrays.asList("Sample item 1", "Sample item 2", "Sample item 3", "Sample item 4", "Sample item 5");
	}

	private void load3DModel() {
		RasterProvider rw = IslandLoader.loadWest();
		RasterProvider re = IslandLoader.loadEast();

		// _______________________________________________________ trails

		long[] trails = TrailsLoader.loadTrails();

		final int overlap = 13;
		final int max_i = OptionDialog.DEFAULT_ISLAND_DEPTH;
		
		SceneGraphIndexedFaceSetShader sg = new SceneGraphIndexedFaceSetShader(
				rw, 
				re, 
				overlap, 
				MainGLFW.Z_TRANSLATION, 
				max_i, 
				trails, 
				null);

		walkViewer.setHeadlight(false);


		walkViewer.setSceneGraph(sg.getSceneGraph());

		walkViewer.setHeightProvider(sg);

		walkViewer.setUpDirection(new SbVec3f(0, 0, 1));

		walkViewer.getCameraController().setAutoClipping(false);


		SoCamera camera = walkViewer.getCameraController().getCamera();

		camera.nearDistance.setValue(MainGLFW.MINIMUM_VIEW_DISTANCE);
		camera.farDistance.setValue(MainGLFW.MAXIMUM_VIEW_DISTANCE);
		
		sg.setCamera(camera);

		walkViewer.getSceneHandler().setTransparencyType(TransparencyType.BLEND/*SORTED_LAYERS_BLEND*/);

		MainGLFW.SCENE_POSITION = new SbVec3f(/*sg.getCenterX()/2*/0, sg.getCenterY(), MainGLFW.Z_TRANSLATION);

		camera.position.setValue(Hero.STARTING_X, Hero.STARTING_Y, Hero.STARTING_Z - MainGLFW.SCENE_POSITION.getZ());
		
		walkViewer.getCameraController().changeCameraValues(camera);
		
		sg.setPosition(MainGLFW.SCENE_POSITION.getX(), MainGLFW.SCENE_POSITION.getY()/*,SCENE_POSITION.getZ()*/);

		sg.setHero(MainGLFW.hero);
		
		sg.setLevelOfDetail((float)OptionDialog.DEFAULT_LOD_FACTOR);
		sg.setLevelOfDetailShadow((float) OptionDialog.DEFAULT_LOD_FACTOR_SHADOW);
		sg.setTreeDistance((float)OptionDialog.DEFAULT_TREE_DISTANCE);

		walkViewer.addIdleListener((viewer1) -> {
			sg.getZ(
					camera.position.getValue().getX(),
					camera.position.getValue().getY(),
					camera.position.getValue().getZ() - walkViewer.EYES_HEIGHT);
			sg.idle();
		});

        walkViewer.start();
        
        walkViewer.setEventCallback(new SoQtGLWidget.eventCBType() {

			@Override
			public boolean run(Object userData, TypedEvent anyevent, EventType type) {
				if( anyevent instanceof MouseEvent) {
					MouseEvent me = (MouseEvent)anyevent;
					if (me.button == 1) {
						
						if( type == EventType.MOUSE_EVENT_MOUSE_DOWN) {
							mouseDown = true;
						}
						else if (type == EventType.MOUSE_EVENT_MOUSE_UP) {
							mouseDown = false;
						}
					}
					else if (type == EventType.MOUSE_EVENT_MOUSE_MOVE) {
						return !mouseDown;
					}
				}
				return false;
			}
        	
        }, null);
		
		System.out.println("Load 3D Model");		
		
	}
}
