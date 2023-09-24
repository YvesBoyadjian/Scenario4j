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
import org.eclipse.swt.layout.RowLayout;
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
import jscenegraph.database.inventor.SbBasic;
import jscenegraph.database.inventor.SbTime;
import jscenegraph.database.inventor.SbVec3f;
import jscenegraph.database.inventor.SoDB;
import jscenegraph.database.inventor.actions.SoGLRenderAction.TransparencyType;
import jscenegraph.database.inventor.events.SoKeyboardEvent;
import jscenegraph.database.inventor.misc.SoNotRec;
import jscenegraph.database.inventor.nodes.SoCamera;
import jscenegraph.database.inventor.nodes.SoOrthographicCamera;
import jscenegraph.database.inventor.nodes.SoPerspectiveCamera;
import jscenegraph.database.inventor.sensors.SoDataSensor;
import jscenegraph.database.inventor.sensors.SoFieldSensor;
import jscenegraph.database.inventor.sensors.SoSensor;
import jscenegraph.database.inventor.sensors.SoSensorCB;
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
	
	private SceneGraphIndexedFaceSetShader sg;

	@Inject
	private MPart part;

	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		Composite upperToolBar = new Composite(parent,SWT.NONE);
		upperToolBar.setLayout(new RowLayout());
		
		Button button = new Button(upperToolBar, SWT.PUSH);
		button.setText("Load 3D Model");
		
		button.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				load3DModel();				
			}
		});
		
		Button button2 = new Button(upperToolBar, SWT.PUSH);
		button2.setText("Upper View");
		
		button2.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				upperView();
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

		    protected void processMouseScrollEvent(int count) {
		    	SoCamera camera = getCameraController().getCamera();
		    	
		    	if (camera instanceof SoPerspectiveCamera) {
		    		// do nothing
		    	}
		    	else {
		    		
		    		SoOrthographicCamera orthoCamera = (SoOrthographicCamera) camera;
		    		orthoCamera.height.setValue(orthoCamera.height.getValue()*(100.0f - 5.0f * count)/100.0f);
		    		
		    	}
			}
		    
		    public void idle() {
		    	SoCamera camera = getCameraController().getCamera();
		    	
		    	if (camera instanceof SoPerspectiveCamera) {
		    		super.idle();
		    	}
		    	else if (camera != null) {

		    		SoOrthographicCamera orthoCamera = (SoOrthographicCamera)camera;
		    		
		    		float cameraHeight = orthoCamera.height.getValue();
		    		
		    		float screenHeight =getGLWidget().getSize().y;

		            float rotation_x = diff.getValue()[1];
		            rotation_x = invert ? -rotation_x : rotation_x;
		            float rotation_z = diff.getValue()[0];

	                updateLocation(new SbVec3f(0.0f, -rotation_x * cameraHeight/screenHeight, 0.0f));
	                updateLocation(new SbVec3f(rotation_z*cameraHeight/screenHeight, 0.0f, 0.0f));
		            
		            diff.setValue((int)0, (int)0);
		    		
		            double currentTimeSec = System.nanoTime()/1.0e9;

		            float deltaT = (float)(currentTimeSec - lastTimeSec);
		            if(deltaT > 1.0f) {
		                deltaT = 1.0f;
		            }

		            if (
		                    keysDown.contains(SoKeyboardEvent.Key.Z)) {

		                //lastTimeSec = System.nanoTime()/1.0e9;


		                updateLocation(new SbVec3f(0.0f, -SPEED* deltaT, 0.0f));
		            }
		            if (

		                    keysDown.contains(SoKeyboardEvent.Key.S)) {

		                //  lastTimeSec = System.nanoTime()/1.0e9;

		                updateLocation(new SbVec3f(0.0f, SPEED* deltaT, 0.0f));

		            }
		            if (  keysDown.contains(SoKeyboardEvent.Key.Q)) {

		                //lastTimeSec = System.nanoTime()/1.0e9;

		                updateLocation(new SbVec3f(- SPEED* deltaT, 0.0f, 0.0f));

		            }
		            if (  keysDown.contains(SoKeyboardEvent.Key.D)) {

		                //lastTimeSec = System.nanoTime()/1.0e9;

		                updateLocation( new SbVec3f(SPEED* deltaT, 0.0f, 0.0f));

		            }

		            idleListeners.forEach((item)->item.accept(this));
		    		
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
		
		sg = new SceneGraphIndexedFaceSetShader(
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
		
		SoFieldSensor auditor = new SoFieldSensor(new SoSensorCB() {

			@Override
			public void run(Object data, SoSensor sensor) {
				System.out.println("position changed");
			}
			
		}, null);
		
//		auditor.attach(camera.position);
		
		camera.nearDistance.setValue(MainGLFW.MINIMUM_VIEW_DISTANCE);
		camera.farDistance.setValue(MainGLFW.MAXIMUM_VIEW_DISTANCE);
		
		sg.setCamera(()->walkViewer.getCameraController().getCamera());

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
			SoCamera sceneGraphCamera = sg.getCamera();
			sg.getZ(
					sceneGraphCamera.position.getValue().getX(),
					sceneGraphCamera.position.getValue().getY(),
					sceneGraphCamera.position.getValue().getZ() - walkViewer.EYES_HEIGHT);
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
	
	private void upperView() {
		
		walkViewer.getCameraController().toggleCameraType();

		SoCamera camera = walkViewer.getCameraController().getCamera();

		camera.nearDistance.setValue(MainGLFW.MINIMUM_VIEW_DISTANCE);
		camera.farDistance.setValue(MainGLFW.MAXIMUM_VIEW_DISTANCE);
		
		if (camera instanceof SoPerspectiveCamera) {
			camera.orientation.setValue(new SbVec3f(1,0,0), (float)SbBasic.M_PI_2);
			SoPerspectiveCamera perspectiveCamera = (SoPerspectiveCamera)camera;
			perspectiveCamera.heightAngle.setValue((float)SbBasic.M_PI_4);
			if (sg != null) {
				float z = sg.getGroundZ();
				SbVec3f cameraPosition = camera.position.getValue();
				camera.position.setValue(cameraPosition.getX(), cameraPosition.getY(), z+1.65f);
			}
		}
		else {
			camera.orientation.setValue(new SbVec3f(0,0,1), 0);
    		
    		SoOrthographicCamera orthoCamera = (SoOrthographicCamera) camera;
    		orthoCamera.height.setValue(100);
    		
    		SbVec3f position = camera.position.getValue();
    		camera.position.setValue(position.getX(), position.getY(), 2000);
		}
		
		System.out.println("Upper View");
	}
}
