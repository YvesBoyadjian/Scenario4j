/**
 * 
 */
package mrieditor.parts;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.function.Consumer;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import javax.swing.JProgressBar;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.widgets.TextFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;

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
import jscenegraph.database.inventor.SbViewportRegion;
import jscenegraph.database.inventor.SoDB;
import jscenegraph.database.inventor.SoPickedPoint;
import jscenegraph.database.inventor.actions.SoRayPickAction;
import jscenegraph.database.inventor.actions.SoGLRenderAction.TransparencyType;
import jscenegraph.database.inventor.events.SoKeyboardEvent;
import jscenegraph.database.inventor.events.SoMouseButtonEvent;
import jscenegraph.database.inventor.misc.SoNotRec;
import jscenegraph.database.inventor.nodes.SoCamera;
import jscenegraph.database.inventor.nodes.SoLineSet;
import jscenegraph.database.inventor.nodes.SoNode;
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
import mrieditor.utils.Utils;
import retroboss.game.MRIGame;

/**
 * 
 */
public class View3DPart {
	
	private static final String EDITED_POLYLINE_NAME = "editedPolyline";
	
	private static final String GAME_SELECTION ="gameSelection";

	private TableViewer tableViewer;
	
	private SoQtWalkViewer walkViewer;
	
	private boolean mouseDown;
	
	private SceneGraphIndexedFaceSetShader sg;
	
	private boolean ruler;
	
	private SbVec3f previousRulerPosition = new SbVec3f();
	
	private boolean polylineDraw;
	
	private MouseListener polylineDrawMouseListener;
	
	private Consumer<SbVec3f> positionConsumer;

	private SoFieldSensor auditor;
	
	@Inject
	private MPart part;

	@Inject
	private IEventBroker eventBroker;	
	
	private MRIGame currentGame;
	
	@Inject @Optional
	public void  getEvent(@UIEventTopic(GAME_SELECTION) Object message) {
	    currentGame = (MRIGame) message;
	    
	    if (walkViewer != null) {
			SoCamera camera = walkViewer.getCameraController().getCamera();
			
			float[] startingPosition = currentGame.getStartingPosition();
			camera.position.setValue(startingPosition[0],startingPosition[1],startingPosition[2] - MainGLFW.SCENE_POSITION.getZ());
	    }
	}	

	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout(1, false));
		
		Composite upperToolBar = new Composite(parent,SWT.NONE);
		upperToolBar.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		upperToolBar.setLayout(new RowLayout());
		
		Label label = new Label(upperToolBar, SWT.NONE);
		
		positionConsumer = new Consumer<SbVec3f>() {

			@Override
			public void accept(SbVec3f position) {
				label.setText("Position: "+ Utils.formatCentimeter(position.getX())+", "+Utils.formatCentimeter(position.getY())+", "+Utils.formatCentimeter(position.getZ()));
				upperToolBar.layout();
			}
			
		};
		
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
		
		Button button3 = new Button(upperToolBar, SWT.TOGGLE);
		button3.setText("Ruler");
		
		button3.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				toggleRuler();
			}
			
		});
		
		Button button7 = new Button(upperToolBar, SWT.PUSH);
		button7.setText("Erase Polyline");
		
		button7.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				erasePolyline();
			}
			
		});
		
		Button button4 = new Button(upperToolBar, SWT.TOGGLE);
		button4.setText("Polyline Draw");
		
		button4.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				togglePolylineDraw();
			}
		});
		
		Button button45 = new Button(upperToolBar, SWT.PUSH);
		button45.setText("Erase Last Polyline Segment");
		
		button45.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				eraseLastPolylineSegment();
			}
		});
		
		Button button5 = new Button(upperToolBar, SWT.PUSH);
		button5.setText("Save Polyline");
		
		button5.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				savePolyline(parent);
			}			
		});
		
		Button button6 = new Button(upperToolBar, SWT.PUSH);
		button6.setText("Load Polyline");
		
		button6.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				loadPolyline(parent);
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
		    
			protected void onFire(SoMouseButtonEvent event) {
				if (polylineDraw) {

					SbViewportRegion vr = this.getSceneHandler().getViewportRegion();
					SoNode sg_ = this.getSceneHandler().getSceneGraph();

					SoRayPickAction fireAction = new SoRayPickAction(vr);
					
					fireAction.setPoint(event.getPosition(vr));

					fireAction.apply(sg_);
					
					SoPickedPoint pp = fireAction.getPickedPoint();
					if( pp == null) {
						fireAction.destructor();
						return;
					}
					else {
						SbVec3f i = new SbVec3f(pp.getPoint().operator_minus(sg.getTranslation()));
						i.setZ(i.getZ()+20f);
						sg.addPolylinePoint(EDITED_POLYLINE_NAME,i);
						System.out.println("x = "+i.getX()+", y = "+i.getY()+", z = "+i.getZ());
					}
					fireAction.destructor();					
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

		            float rotation_x = getDiff().getValue()[1];
		            rotation_x = invert ? -rotation_x : rotation_x;
		            float rotation_z = getDiff().getValue()[0];

		    		if (!ruler){
		                updateLocation(new SbVec3f(0.0f, -rotation_x * cameraHeight/screenHeight, 0.0f));
		                updateLocation(new SbVec3f(rotation_z*cameraHeight/screenHeight, 0.0f, 0.0f));
		                
			    		getDiff().setValue((int)0, (int)0);
		    		}
		    		else {
		    			previousRulerPosition = updateRulerLocation(new SbVec3f(0.0f, rotation_x * cameraHeight/screenHeight, 0.0f), previousRulerPosition);
		    			previousRulerPosition = updateRulerLocation(new SbVec3f(- rotation_z*cameraHeight/screenHeight, 0.0f, 0.0f), previousRulerPosition);
		    			sg.setRuler(previousRulerPosition);
		                
			    		getDiff().setValue((int)0, (int)0);
		    		}
		    		
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
        
        polylineDrawMouseListener = new MouseAdapter() {

			@Override
			public void mouseDown(MouseEvent e) {
				int mouseX = e.x;
				int mouseY = e.y;
				
				System.out.println("x = "+ mouseX +", y = " + mouseY);
			}
        	
        };
        
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
				null, false);

		walkViewer.setHeadlight(false);


		walkViewer.setSceneGraph(sg.getSceneGraph());

		walkViewer.setHeightProvider(sg);

		walkViewer.setUpDirection(new SbVec3f(0, 0, 1));

		walkViewer.getCameraController().setAutoClipping(false);


		SoCamera camera = walkViewer.getCameraController().getCamera();
		
		auditor = new SoFieldSensor(new SoSensorCB() {

			@Override
			public void run(Object data, SoSensor sensor) {
				//System.out.println("position changed");
				Display.getCurrent().asyncExec(()->{
					SbVec3f cameraPosition = new SbVec3f(camera.position.getValue());
					cameraPosition.setZ(sg.getGroundZ() + MainGLFW.Z_TRANSLATION + walkViewer.EYES_HEIGHT);
					
					positionConsumer.accept(cameraPosition);
				});
			}
			
		}, null);
		
		auditor.attach(camera.position);
		
		camera.nearDistance.setValue(MainGLFW.MINIMUM_VIEW_DISTANCE);
		camera.farDistance.setValue(MainGLFW.MAXIMUM_VIEW_DISTANCE);
		
		sg.setCamera(()->walkViewer.getCameraController().getCamera());

		walkViewer.getSceneHandler().setTransparencyType(TransparencyType.BLEND/*SORTED_LAYERS_BLEND*/);

		MainGLFW.SCENE_POSITION = new SbVec3f(/*sg.getCenterX()/2*/0, sg.getCenterY(), MainGLFW.Z_TRANSLATION);

		if (currentGame == null) {
			camera.position.setValue(Hero.STARTING_X, Hero.STARTING_Y, Hero.STARTING_Z - MainGLFW.SCENE_POSITION.getZ());
		}
		else {
			float[] startingPosition = currentGame.getStartingPosition();
			camera.position.setValue(startingPosition[0],startingPosition[1],startingPosition[2] - MainGLFW.SCENE_POSITION.getZ());
		}
		
		walkViewer.getCameraController().changeCameraValues(camera);
		
		sg.setPosition(MainGLFW.SCENE_POSITION.getX(), MainGLFW.SCENE_POSITION.getY()/*,SCENE_POSITION.getZ()*/);

		sg.setHero(MainGLFW.hero);


		// ______________________________________________________________________________________________________ planks
		File planksFile = new File("planks.mri");
		if (planksFile.exists()) {
			try {
				InputStream in = new FileInputStream(planksFile);

				Properties planksProperties = new Properties();

				planksProperties.load(in);

				in.close();
				
				SbViewportRegion vpRegion = walkViewer.getSceneHandler().getViewportRegion();

				sg.loadPlanks(vpRegion,planksProperties);

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
		
//		sg.enableFPS(true);
		
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
		

//		walkViewer.addIdleListener((viewer1) -> {
//					sg.setFPS(viewer1.getFPS());
//				}
//		);
		

        walkViewer.start();
        
        setEventCallback(true);
		
		System.out.println("Load 3D Model");		
		
	}
	
	private void setEventCallback(boolean set) {

		if (set) {
        walkViewer.setEventCallback(new SoQtGLWidget.eventCBType() {

			@Override
			public boolean run(Object userData, TypedEvent anyevent, EventType type) {
				if( anyevent instanceof MouseEvent) {
					MouseEvent me = (MouseEvent)anyevent;
					if (me.button == 1 || me.button == 2) {
						
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
		}
		else {
			walkViewer.setEventCallback(null, null);
		}
	}
	
	private void upperView() {
				
		auditor.detach();
		
		walkViewer.getCameraController().toggleCameraType();

		SoCamera camera = walkViewer.getCameraController().getCamera();
		
		auditor = new SoFieldSensor(new SoSensorCB() {

			@Override
			public void run(Object data, SoSensor sensor) {
				//System.out.println("position changed");
				Display.getCurrent().asyncExec(()->{
					SbVec3f cameraPosition = new SbVec3f(camera.position.getValue());
					cameraPosition.setZ(sg.getGroundZ() + MainGLFW.Z_TRANSLATION + walkViewer.EYES_HEIGHT);
					
					positionConsumer.accept(cameraPosition);
				});
			}
			
		}, null);
		
		auditor.attach(camera.position);

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
			sg.center(false);
		}
		else {
			camera.orientation.setValue(new SbVec3f(0,0,1), 0);
    		
    		SoOrthographicCamera orthoCamera = (SoOrthographicCamera) camera;
    		orthoCamera.height.setValue(100);
    		
    		SbVec3f position = camera.position.getValue();
    		camera.position.setValue(position.getX(), position.getY(), 2000);
    		sg.center(true);
		}
		
		System.out.println("Upper View");
	}
	
	private void toggleRuler() {
		ruler = !ruler;
		
		if (!ruler) {
			walkViewer.getDiff().setValue((int)0, (int)0);
			sg.setRuler(null);
		} else {
			previousRulerPosition.copyFrom(walkViewer.getCameraController().getCamera().position.getValue());
		}
	}
	
	private void erasePolyline() {		
		sg.removeAllPolylinePoints(EDITED_POLYLINE_NAME);		
	}
	
	private void eraseLastPolylineSegment() {
		sg.removeLastPolylinePoint(EDITED_POLYLINE_NAME);
	}
	
	private void togglePolylineDraw() {
		polylineDraw = !polylineDraw;
		
		if (polylineDraw) {
			walkViewer.getGLWidget().addMouseListener(polylineDrawMouseListener);
			walkViewer.setViewing(false);
			//setEventCallback(false);
			sg.showPolylines(true);
		}
		else {
			walkViewer.getGLWidget().removeMouseListener(polylineDrawMouseListener);
			walkViewer.setViewing(true);
			//setEventCallback(true);
			sg.showPolylines(false);
		}
	}
	
	private void savePolyline(Composite parent) {
		List<SbVec3f> points = sg.getPolylinePoints(EDITED_POLYLINE_NAME);
		FileDialog fd = new FileDialog(parent.getShell(), SWT.SAVE);
		String[] extensions = new String[1];
		extensions[0] = "*.poly";
		fd.setFilterExtensions(extensions);
		String path = fd.open();
		if (path != null) {
			Properties props = new Properties();
			
			int index = 0;
			for (SbVec3f point : points) {
				props.put("X"+index, Float.toString(point.getX()));
				props.put("Y"+index, Float.toString(point.getY()));
				props.put("Z"+index, Float.toString(point.getZ()));
				index++;				
			}
			props.put("numPoints", Integer.toString(index));
			
			File savePolyFile = new File(path);
			
			try {
				OutputStream out = new FileOutputStream(savePolyFile);
				
				props.store(out, "Polyline");

				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.err.println(props.toString());
		}
	}
	
	private void loadPolyline(Composite parent) {
		FileDialog fd = new FileDialog(parent.getShell(), SWT.OPEN);
		String[] extensions = new String[1];
		extensions[0] = "*.poly";
		fd.setFilterExtensions(extensions);
		String path = fd.open();
		if (path != null) {
			
			sg.loadPolyline(path);
		}		
	}
}
