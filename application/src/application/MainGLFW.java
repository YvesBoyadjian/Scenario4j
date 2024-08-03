/**
 * Mount Rainier Island, an adventure game
 */
package application;

import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.Raster;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.zip.GZIPInputStream;

import javax.imageio.ImageIO;
import javax.sound.sampled.*;
import javax.swing.*;
import javax.swing.border.BevelBorder;

import application.audio.AudioRenderer;
import application.audio.ProgressUpdater;
import application.audio.VorbisTrack;
import application.gui.OptionDialog;
import application.objects.Hero;
import application.scenario.FirstApproachQuest;
import application.scenario.LeaveKlapatchePointQuest;
import application.scenario.Scenario;
import application.scenario.TargetsKillingQuest;
import application.viewer.glfw.ForceProvider;
import application.viewer.glfw.PositionProvider;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.physics.bullet.Bullet;
import com.badlogic.gdx.physics.bullet.collision.*;
import com.badlogic.gdx.physics.bullet.dynamics.btConstraintSolver;
import com.badlogic.gdx.physics.bullet.dynamics.btDiscreteDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btDynamicsWorld;
import com.badlogic.gdx.physics.bullet.dynamics.btRigidBody;
import com.badlogic.gdx.physics.bullet.dynamics.btSequentialImpulseConstraintSolver;
import com.badlogic.gdx.physics.bullet.linearmath.btMotionState;
import com.badlogic.gdx.physics.bullet.linearmath.btTransform;
import com.badlogic.gdx.physics.bullet.linearmath.btVector3;
import com.badlogic.gdx.utils.GdxNativesLoader;
import com.badlogic.gdx.utils.SharedLibraryLoader;
import com.jogamp.opengl.GL2;

import application.physics.OpenGLMotionState;

//import org.eclipse.swt.SWT;
//import org.eclipse.swt.graphics.Color;
//import org.eclipse.swt.graphics.Cursor;
//import org.eclipse.swt.graphics.ImageData;
//import org.eclipse.swt.graphics.PaletteData;
//import org.eclipse.swt.graphics.RGB;
//import org.eclipse.swt.layout.FillLayout;
//import org.eclipse.swt.widgets.Display;
//import org.eclipse.swt.widgets.Shell;

import application.scenegraph.SceneGraph;
import application.scenegraph.SceneGraphIndexedFaceSet;
import application.scenegraph.SceneGraphIndexedFaceSetShader;
import application.scenegraph.ShadowTestSceneGraph;
import application.scenegraph.Soleil;
import application.terrain.IslandLoader;
import application.trails.TrailsLoader;
import application.viewer.glfw.SoQtWalkViewer;
import jscenegraph.database.inventor.*;
import jscenegraph.database.inventor.actions.SoGLRenderAction;
import jscenegraph.database.inventor.actions.SoGLRenderAction.TransparencyType;
import jscenegraph.database.inventor.events.SoKeyboardEvent;
import jscenegraph.database.inventor.events.SoMouseButtonEvent;
import jscenegraph.database.inventor.actions.SoAction;
import jscenegraph.database.inventor.actions.SoRayPickAction;
import jscenegraph.database.inventor.nodes.*;
import jscenegraph.interaction.inventor.SoSceneManager;
import jscenegraph.port.KDebug;
import jsceneviewerglfw.inventor.qt.SoQt;
import jsceneviewerglfw.inventor.qt.SoQtCameraController;
import jsceneviewerglfw.inventor.qt.viewers.SoQtFullViewer;
import jsceneviewerglfw.Cursor;
import jsceneviewerglfw.Display;
import jsceneviewerglfw.GLData;
import jsceneviewerglfw.SWT;
import loader.TerrainLoader;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.AL10;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLDebugMessageCallback;
import org.ode4j.math.DQuaternion;
import org.ode4j.math.DVector3;
import org.ode4j.math.DVector3C;
import org.ode4j.ode.*;
import org.ode4j.ode.internal.DxHashSpace;
import org.ode4j.ode.internal.ErrorHandler;
import org.ode4j.ode.internal.ErrorHdl;
import org.ode4j.ode.internal.Rotation;

import static application.objects.Hero.*;
import static com.badlogic.gdx.physics.bullet.collision.CollisionConstants.DISABLE_DEACTIVATION;
import static java.lang.Thread.sleep;
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.opengl.GL11C.glEnable;
import static org.lwjgl.opengl.GL11C.glGetError;
import static org.lwjgl.opengl.GL43C.*;

/**
 * @author Yves Boyadjian
 */
public class MainGLFW {

    public static final boolean DEBUG_MODE = false;

    public static final float CONTACT_SURFACE_MU_DEFAULT = 0.838f; // Original value in game

    public static final float CONTACT_SURFACE_MU_BAREFOOT = 0.5f;

    public static final float CONTACT_SURFACE_MU_BOOTS = 1.0f;

    public static final float MINIMUM_VIEW_DISTANCE = 2.5f;//1.0f;

    public static final float MAXIMUM_VIEW_DISTANCE = SceneGraphIndexedFaceSetShader.WATER_HORIZON;//5e4f;//SceneGraphIndexedFaceSet.SUN_FAKE_DISTANCE * 1.5f;

    public static final float Z_TRANSLATION = 2000;

    public static final double REAL_START_TIME_SEC = 60 * 60 * 4.5; // 4h30 in the morning

    public static final double COMPUTER_START_TIME_SEC = REAL_START_TIME_SEC / TimeConstants./*JMEMBA_TIME_ACCELERATION*/GTA_SA_TIME_ACCELERATION;

    public static final String TIME = "time_sec";

    public static final String TIME_STOP = "time_stop";

    public static final String ALLOW_FLY = "allow_fly";

    public static final String QUEST_INDEX = "quest_index";

    public static final String SHADOW_PRECISION = "shadow_precision";

    public static final String LOD_FACTOR = "lod_factor";

    public static final String LOD_FACTOR_SHADOW = "lod_factor_shadow";

    public static final String TREE_DISTANCE = "tree_distance";

    public static final String TREE_SHADOW_DISTANCE = "tree_shadow_distance";

    public static final String MAX_I = "max_i";

    public static final String OVERALL_CONTRAST = "overall_contrast";

    public static final String VOLUMETRIC_SKY = "volumetric_sky";

    public static final String DISPLAY_FPS = "display_fps";

    public static final String KILLED_ENEMIES = "killed_enemies";

    public static SbVec3f SCENE_POSITION;

    public static final SbColor SKY_BLUE = new SbColor(0.53f, 0.81f, 0.92f);

    public static final SbColor DEEP_SKY_BLUE = new SbColor(0.0f, 0.749f, 1.0f);

    public static final SbColor VERY_DEEP_SKY_BLUE = new SbColor(0.0f, 0.749f / 3.0f, 1.0f);

    public static boolean god = false;

    public static final long TRAILS_VERSION = 0;

    public static final int MAX_PROGRESS = 99999;

    public static boolean newGameAtStart = false;

    /**
     * @param args
     */
    public static void main(String[] args) {

        if (args.length == 1 && Objects.equals(args[0], "opengl32")) {
            System.loadLibrary("opengl32");
        }

        if (args.length == 1 && Objects.equals(args[0], "god")) {
            god = true;
        }
        SwingUtilities.invokeLater(() -> {
            showSplash();
        });
    }

    //System.loadLibrary("opengl32"); //for software rendering using mesa3d for Windows

    //System.setProperty("IV_DEBUG_CACHES", "1");

//		ImageIcon ii = new ImageIcon();
//		
//		try {
//			ii = new ImageIcon(new URL("https://github.com/YvesBoyadjian/Koin3D/blob/master/MountRainierIslandScreenShot.jpg"));
//		} catch (MalformedURLException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
//		
//		Image splashScreen = ii.getImage();

    static JWindow window;

    static boolean hasMainGameBeenCalled;

    static Scenario scenario;


    static double last_call = Double.NaN;

    public static void showSplash() {
        window = new JWindow();

        final Container rootContentPane = window.getContentPane();

        Image bgImage = null;
        try {
            bgImage = ImageIO.read(new File("ressource/BigFoot_1200DPI.jpg"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        final Image bgImagef = bgImage;

        final Container contentPane = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {

                super.paintComponent(g);

                float imageRatio = (float)bgImagef.getHeight(null)/bgImagef.getWidth(null);
                float rootContentPaneRatio = (float)rootContentPane.getHeight()/rootContentPane.getWidth();
                int drawWidth = rootContentPane.getWidth();
                if (rootContentPaneRatio < imageRatio) {
                    drawWidth *= rootContentPaneRatio / imageRatio;
                }

                g.drawImage(bgImagef, (rootContentPane.getWidth() - drawWidth)/2, 0, drawWidth, rootContentPane.getHeight(), null);
            }
        };
        rootContentPane.add(contentPane);
        contentPane.setLayout(new BorderLayout());

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        double width = screenSize.getWidth();
        double height = screenSize.getHeight();

        JLabel intro = new JLabel("Bigfoot Hunting, an Adventure Game", null, SwingConstants.CENTER);
        intro.setForeground(Color.red.darker());
        intro.setFont(intro.getFont().deriveFont((float) height / 20f));
        contentPane.add(intro);

        final JProgressBar progressBar = new JProgressBar(0, MAX_PROGRESS);
        progressBar.setBackground(Color.black);
        progressBar.setForeground(Color.red.darker());
        progressBar.setBorderPainted(false);

        JPanel southPanel = new JPanel();
        //southPanel.setBackground(Color.BLACK);
        southPanel.setLayout(new BoxLayout(southPanel, BoxLayout.PAGE_AXIS));
        southPanel.setOpaque(false);

        JLabel engine = new JLabel("Scenario4j Engine", null, SwingConstants.CENTER);
        engine.setFont(intro.getFont().deriveFont((float) height / 30f));
        engine.setForeground(Color.orange);
        engine.setBackground(Color.black);

        JLabel keys = new JLabel("[WASD] or [ZQSD] to walk, [left mouse button] to shoot" + ((SceneGraphIndexedFaceSetShader.AIM ? ", [right mouse button] to aim" : "") + ", [Esc] for menu"), null, SwingConstants.CENTER);
        keys.setForeground(Color.yellow);
        keys.setFont(intro.getFont().deriveFont((float) height / 40f));
        keys.setBackground(Color.black);

        JPanel keysPanel = new JPanel();
        keysPanel.setBackground(Color.black);
        keysPanel.add(keys);

        southPanel.add(engine);
        southPanel.add(progressBar);
        southPanel.add(keysPanel);

        contentPane.add(southPanel, BorderLayout.SOUTH);


        contentPane.setBackground(Color.GREEN.darker().darker());
        window.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));
        //window.getContentPane().setForeground(Color.white);

        window.setBounds(0, 0, (int) width, (int) height - 40);

        window.setVisible(true);

        SwingWorker sw = new SwingWorker() {
            @Override
            protected Object doInBackground() throws Exception {
                try {
                    mainGame(progressBar);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(window, e.toString(), "Exception in Mount Rainier Island", JOptionPane.ERROR_MESSAGE);
                    e.printStackTrace();
                    System.exit(-1); // Necessary, because of Linux
                } catch (Error e) {
                    JOptionPane.showMessageDialog(window, e.toString(), "Error in Mount Rainier Island", JOptionPane.ERROR_MESSAGE);
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

    static Display display;

    static SceneGraphIndexedFaceSetShader sg;

    static SoQtWalkViewer viewer;


    static boolean physics_error = false;

    static int loadingQuestIndex = 0;

    static double previousTimeSec = 0;

    static boolean timeStop = false;

    static public final Hero hero = new Hero();
    static boolean allowFly = false;

    static DWorld world;
    static DSpace space;
    static DJointGroup contactGroup;

    //static final DxHashSpace.Node[][] tablePtr = new DxHashSpace.Node[1][];

    //static Clip seaClip;

    //static Clip forestClip;

    static boolean shouldClose;

    static AudioRenderer seaRenderer;

    static float seaRendererVolume;

    static Thread seaThread;

    static CountDownLatch seaAudioLatch = new CountDownLatch(1);

    static AudioRenderer forestRenderer;

    static float forestRendererVolume;

    static Thread forestThread;

    static CountDownLatch forestAudioLatch = new CountDownLatch(1);

    static final AtomicLong[] lastAliveMillis = new AtomicLong[1];

    static {
        lastAliveMillis[0] = new AtomicLong();
    }

    public static void mainGame(final JProgressBar progressBar) {
        display = new Display();
        //Shell shell = new Shell(display);
        //shell.setLayout(new FillLayout());

        RasterProvider rw = IslandLoader.loadWest();
        RasterProvider re = IslandLoader.loadEast();

        SoQt.init("demo");

        SoDB.setDelaySensorTimeout(SbTime.zero()); // Necessary to avoid bug in Display

        //SoSeparator.setNumRenderCaches(0);
        //SceneGraph sg = new SceneGraphQuadMesh(r);

        float shadow_precision = (float) OptionDialog.DEFAULT_SHADOW_PRECISION;
        float level_of_detail = (float) OptionDialog.DEFAULT_LOD_FACTOR;
        float level_of_detail_shadow = (float) OptionDialog.DEFAULT_LOD_FACTOR_SHADOW;
        float tree_distance = (float) OptionDialog.DEFAULT_TREE_DISTANCE;
        float tree_shadow_distance = (float) OptionDialog.DEFAULT_TREE_SHADOW_DISTANCE;
        int max_i = OptionDialog.DEFAULT_ISLAND_DEPTH;
        float overall_contrast = (float) OptionDialog.DEFAULT_OVERALL_CONTRAST;
        boolean volumetric_sky = OptionDialog.DEFAULT_VOLUMETRIC_SKY;
        boolean display_fps = false;

        File graphicsFile = new File("../graphics.mri");
        if (graphicsFile.exists()) {
            try {
                InputStream in = new FileInputStream(graphicsFile);

                Properties graphicsProperties = new Properties();

                graphicsProperties.load(in);

                shadow_precision = Float.valueOf(graphicsProperties.getProperty(SHADOW_PRECISION, "0.4"));

                level_of_detail = Float.valueOf(graphicsProperties.getProperty(LOD_FACTOR, "1"));

                level_of_detail_shadow = Float.valueOf(graphicsProperties.getProperty(LOD_FACTOR_SHADOW, "1"));

                tree_distance = Float.valueOf(graphicsProperties.getProperty(TREE_DISTANCE, "7000"));

                tree_shadow_distance = Float.valueOf(graphicsProperties.getProperty(TREE_SHADOW_DISTANCE, "3000"));

                max_i = Integer.valueOf(graphicsProperties.getProperty(MAX_I, "14000"));

                overall_contrast = Float.valueOf(graphicsProperties.getProperty(OVERALL_CONTRAST, "1.6"));

                volumetric_sky = "true".equals(graphicsProperties.getProperty(VOLUMETRIC_SKY, "true"));

                display_fps = "true".equals(graphicsProperties.getProperty(DISPLAY_FPS, "false"));

                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        // _______________________________________________________ trails

        long[] trails = TrailsLoader.loadTrails();

        int overlap = 13;
        //SceneGraph sg = new SceneGraphIndexedFaceSet(rw,re,overlap,Z_TRANSLATION);
        sg = new SceneGraphIndexedFaceSetShader(rw, re, overlap, Z_TRANSLATION, max_i, trails, progressBar, true);
        //SceneGraph sg = new ShadowTestSceneGraph();
        rw = null; // for garbage collection
        re = null; // for garbage collection

        sg.getShadowGroup().precision.setValue(shadow_precision);
        sg.getShadowGroup().epsilon.setValue(1.0e-5f / shadow_precision);
        sg.setSoftShadows(shadow_precision > 0.05f);

        sg.setLevelOfDetail(level_of_detail);

        sg.setLevelOfDetailShadow(level_of_detail_shadow);

        sg.setTreeDistance(tree_distance);

        sg.setTreeShadowDistance(tree_shadow_distance);

        sg.setOverallContrast(overall_contrast);

        sg.getShadowGroup().isVolumetricActive.setValue(volumetric_sky);
        sg.getEnvironment().fogColor.setValue(volumetric_sky ? new SbColor(sg.SKY_COLOR.darker().darker().darker().darker().darker().darker().operator_mul(sg.getOverallContrast())) : new SbColor(sg.SKY_COLOR.darker().operator_mul(sg.getOverallContrast())));

        sg.enableFPS(display_fps);

        // _____________________________________________________ Story
        scenario = new Scenario(sg);

        // __________________________________________ Leave Klapatche point
        scenario.addQuest(new LeaveKlapatchePointQuest());
        // __________________________________________ Oracle encounter
        scenario.addQuest(new FirstApproachQuest());
        // __________________________________________ Killing targets
        scenario.addQuest(new TargetsKillingQuest());

        int style = 0;//SWT.NO_BACKGROUND;

        viewer = new SoQtWalkViewer(SoQtFullViewer.BuildFlag.BUILD_NONE, SoQtCameraController.Type.BROWSER,/*shell*/null, style) {

            public void onClose(boolean resetToDefault) {

                // ____________________________________________________________________________________________ savegame
                File saveGameFile = new File("../savegame.mri");

                Properties saveGameProperties = new Properties();

                try {
                    OutputStream out = new FileOutputStream(saveGameFile);

                    SoCamera camera = getCameraController().getCamera();

                    if (!resetToDefault) {
                        saveGameProperties.setProperty(HERO_X, String.valueOf(camera.position.getValue().getX()));

                        saveGameProperties.setProperty(HERO_Y, String.valueOf(camera.position.getValue().getY()));

                        saveGameProperties.setProperty(HERO_Z, String.valueOf(camera.position.getValue().getZ() + Z_TRANSLATION));

                        saveGameProperties.setProperty(CAT_X, String.valueOf(sg.getCatPosition().getX()));
                        saveGameProperties.setProperty(CAT_Y, String.valueOf(sg.getCatPosition().getY()));
                        saveGameProperties.setProperty(CAT_Z, String.valueOf(sg.getCatPosition().getZ() + Z_TRANSLATION));

                        saveGameProperties.setProperty(TIME, String.valueOf(getNow()));

                        saveGameProperties.setProperty(TIME_STOP, isTimeStop() ? "true" : "false");

                        saveGameProperties.setProperty(FLY, isFlying() ? "true" : "false");

                        saveGameProperties.setProperty(ALLOW_FLY, isAllowingFly() ? "true" : "false");

                        saveGameProperties.setProperty(QUEST_INDEX, String.valueOf(scenario.getCurrentQuestIndex()));

                        saveGameProperties.setProperty(BOOTS, sg.haveBoots() ? "true" : "false");

                        saveGameProperties.setProperty(LIFE, String.valueOf(sg.getHero().life));

                        saveGameProperties.setProperty(KILLED_ENEMIES, sg.getEnemies().getKilledInstances());

                        sg.saveShots(saveGameProperties);
                    }
                    saveGameProperties.store(out, "Mount Rainier Island save game");

                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                // ____________________________________________________________________________________________ graphics
                File graphicsFile = new File("../graphics.mri");

                Properties graphicsProperties = new Properties();

                try {
                    OutputStream out = new FileOutputStream(graphicsFile);

                    graphicsProperties.setProperty(SHADOW_PRECISION, String.valueOf(sg.getShadowGroup().precision.getValue()));

                    graphicsProperties.setProperty(LOD_FACTOR, String.valueOf(sg.getLevelOfDetail()));

                    graphicsProperties.setProperty(LOD_FACTOR_SHADOW, String.valueOf(sg.getLevelOfDetailShadow()));

                    graphicsProperties.setProperty(TREE_DISTANCE, String.valueOf(sg.getTreeDistance()));

                    graphicsProperties.setProperty(TREE_SHADOW_DISTANCE, String.valueOf(sg.getTreeShadowDistance()));

                    graphicsProperties.setProperty(MAX_I, String.valueOf(sg.getMaxI()));

                    graphicsProperties.setProperty(OVERALL_CONTRAST, String.valueOf(sg.getOverallContrast()));

                    graphicsProperties.setProperty(VOLUMETRIC_SKY, sg.getShadowGroup().isVolumetricActive.getValue() ? "true" : "false");

                    graphicsProperties.setProperty(DISPLAY_FPS, sg.isFPSEnabled() ? "true" : "false");

                    graphicsProperties.store(out, "Mount Rainier Island graphics");

                    out.close();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (god) {

                    // __________________________________________________________________________________________ planks
                    File planksFile = new File("planks.mri");

                    Properties planksProperties = new Properties();

                    sg.storePlanks(planksProperties);

                    try {
                        OutputStream out = new FileOutputStream(planksFile);

                        planksProperties.store(out, "Mount Rainier Island planks");

                        out.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    // __________________________________________________________________________________________ trails
                    File trailsFile = new File("trails.mri");

                    try {
                        DataOutputStream ecrivain = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(trailsFile)));
                        ecrivain.writeLong(TRAILS_VERSION);
                        ecrivain.writeLong(sg.getTrailsSize());
                        sg.getTrails().forEach((l) -> {
                            try {
                                ecrivain.writeLong(l);
                            } catch (IOException e) {
                            }
                        });
                        ecrivain.close();
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            byte[] gunSound = loadSound("GUN_FIRE-GoodSoundForYou-820112263_10db.wav");

            protected void onFire(SoMouseButtonEvent event) {
                //playSound("shortened_40_smith_wesson_single-mike-koenig.wav"/*clipf*/);
                if (gunSound != null) {
                    playSoundDelayed(/*"GUN_FIRE-GoodSoundForYou-820112263_10db.wav"*//*clipf*/gunSound, false, 1f);
                }

                SbViewportRegion vr = this.getSceneHandler().getViewportRegion();
                SoNode sg_ = this.getSceneHandler().getSceneGraph();

//				TargetSearchRunnable tsr = new TargetSearchRunnable(this, vr, sg);
//				tsr.run();
                SwingUtilities.invokeLater(new TargetSearchRunnable(this, vr, sg_, sg));
                //new Thread(new TargetSearchRunnable(this, vr, sg)).start();
            }

            protected void onAim(SoMouseButtonEvent event, boolean aim) {
                sg.aim(aim);
            }

            public void initializeGL(GL2 gl2) {
                super.initializeGL(gl2);

                if (DEBUG_MODE) {
                    int error = glGetError();
                    glEnable(GL_DEBUG_OUTPUT);
                    error = glGetError();
                    glDebugMessageCallback(new GLDebugMessageCallback() {
                        @Override
                        public void invoke(int source, int type, int id, int severity, int length, long message, long userParam) {
                            if (severity == GL_DEBUG_SEVERITY_HIGH) {
                                String messageStr = getMessage(length, message);
                                System.err.println("OpenGL Error : " + messageStr);
                            }
                        }
                    }, 0);
                    error = glGetError();
                    glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
                    error = glGetError();
                }

                final int[] vao = new int[1];
                gl2.glGenVertexArrays(1, vao);
                gl2.glBindVertexArray(vao[0]);

                System.out.println("init");
            }
        };

        final byte[] screamSound = loadSound("VOXEfrt_Cri de douleur (ID 2361)_LS_16bit.wav");
        final Clip[] screamClip = new Clip[1];

        viewer.addIdleListener((viewer0) -> {
            if (screamClip[0] != null) {
                Clip clip = screamClip[0];
                if (clip.isRunning()) {
                    return;
                }
            }
            if (sg.getHero() != null && sg.getHero().hurting) {
                screamClip[0] = playSound(new ByteArrayInputStream(screamSound), false, 1.0f);
            }
        });

        viewer.setHeadlight(false);


        viewer.setSceneGraph(sg.getSceneGraph());

        viewer.setHeightProvider(sg);

        SCENE_POSITION = new SbVec3f(/*sg.getCenterX()/2*/0, sg.getCenterY(), Z_TRANSLATION);

        viewer.setUpDirection(new SbVec3f(0, 0, 1));

        viewer.getCameraController().setAutoClipping(false);

        // In order not to invalidate shaders
        SoSceneManager.enableRealTimeUpdate(false);
        //SoDB.enableRealTimeSensor(false); impossible to disable: Will always be reenabled by other objects like SoBlinker

        SoCamera camera = viewer.getCameraController().getCamera();

        sg.setCamera(() -> viewer.getCameraController().getCamera());

        camera.nearDistance.setValue(MINIMUM_VIEW_DISTANCE);
        camera.farDistance.setValue(MAXIMUM_VIEW_DISTANCE);

        camera.position.setValue(0, 0, 0);
        camera.orientation.setValue(new SbVec3f(0, 1, 0), -(float) Math.PI / 2.0f);

        if (camera instanceof SoPerspectiveCamera) {
            SoPerspectiveCamera perspCamera = (SoPerspectiveCamera) camera;
            perspCamera.heightAngle.setValue(50.0f * (float) Math.PI / 180.0f);
        }

        // _____________________________________________________ Physics with bullet physics

//	    GdxNativesLoader.load();
//	    new SharedLibraryLoader().load("gdx-bullet");
//
//		Bullet.init();
//
//	    btBroadphaseInterface m_pBroadphase;
//	    btCollisionConfiguration m_pCollisionConfiguration;
//	    btCollisionDispatcher m_pDispatcher;
//	    btConstraintSolver m_pSolver;
//	    btDynamicsWorld m_pWorld;
//
//	    //Meanwhile, the code to initialize Bullet can be found in BasicDemo and looks as shown in the following code snippet:
//
//		btDefaultCollisionConstructionInfo info = new btDefaultCollisionConstructionInfo();
//		//info.setUseEpaPenetrationAlgorithm(99);
//
//	    m_pCollisionConfiguration = new btDefaultCollisionConfiguration(info);
//	    m_pDispatcher = new btCollisionDispatcher(m_pCollisionConfiguration);
//
//	    m_pBroadphase = new btDbvtBroadphase();
//	    m_pSolver = new btSequentialImpulseConstraintSolver();
//	    m_pWorld = new btDiscreteDynamicsWorld(m_pDispatcher, m_pBroadphase, m_pSolver, m_pCollisionConfiguration);
//
//		m_pWorld.setGravity(new Vector3(0,0,-9.81f));
//
//	    // create a box shape of size (1,1,1)
//		btSphereShape /*btBoxShape*/ pBoxShape = new btSphereShape/*btBoxShape*/(0.4f);//,1.75f-2*0.4f/*new Vector3(0.5f/2.0f, 0.5f/2.0f, 1.75f/2.0f)*/);
//
//		SbVec3f cameraPositionValue = camera.position.getValue();
//
//	    // give our box an initial position of (0,0,0)
//	    btTransform transform = new btTransform();
//	    transform.setIdentity();
//	    transform.setOrigin(new Vector3(cameraPositionValue.getX(), cameraPositionValue.getY(), cameraPositionValue.getZ() + 0.4f - 1.75f + 0.13f));
//
//	    // create a motion state
//	    OpenGLMotionState m_pMotionState = new OpenGLMotionState(transform);
//
//	    // create the rigid body construction info object, giving it a
//	    // mass of 1, the motion state, and the shape
//	    btRigidBody.btRigidBodyConstructionInfo rbInfo = new btRigidBody.btRigidBodyConstructionInfo(82.0f, m_pMotionState, pBoxShape);
//	    btRigidBody pRigidBody = new btRigidBody(rbInfo);
//
//		pRigidBody.setActivationState(DISABLE_DEACTIVATION);
//
//		//pRigidBody.setCollisionFlags(pRigidBody.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_CHARACTER_OBJECT);
//
//	    // inform our world that we just created a new rigid body for
//	    // it to manage
//	    m_pWorld.addRigidBody(pRigidBody);
//
//	btBoxShape groundShape = new btBoxShape(new Vector3(99999,99999,1));
//
//		// give our box an initial position of (0,0,0)
//		btTransform groundTransform = new btTransform();
//		groundTransform.setIdentity();
//		groundTransform.setOrigin(new Vector3(cameraPositionValue.getX(), cameraPositionValue.getY(), cameraPositionValue.getZ()-1 - 1.75f + 0.13f/*-0.4f*/-0.01f));
//
//	    btRigidBody.btRigidBodyConstructionInfo groundInfo = new btRigidBody.btRigidBodyConstructionInfo(0, new OpenGLMotionState(groundTransform), groundShape);
//	    btRigidBody groundBody = new btRigidBody(groundInfo);
//
//		//groundBody.setCollisionFlags(pRigidBody.getCollisionFlags() | btCollisionObject.CollisionFlags.CF_STATIC_OBJECT);
//
//	    m_pWorld.addRigidBody(groundBody);
//
//	    //m_pWorld.getSolverInfo().setSplitImpulse(1);
//		//m_pWorld.getSolverInfo().setSplitImpulsePenetrationThreshold(0);
//
//		//btHeightfieldTerrainShape terrainShape = new btHeightfieldTerrainShape();
//
//		viewer.addIdleListener((viewer1)->{
//			m_pWorld.stepSimulation((float)viewer1.dt());//,10,(float)viewer1.dt()/10.0f);
//		});
//
//		viewer.setPositionProvider(new PositionProvider() {
//			@Override
//			public SbVec3f getPosition() {
//				Matrix4 worldTrans = new Matrix4();
//				m_pMotionState.getWorldTransform(worldTrans);
//				Vector3 position = new Vector3();
//				worldTrans.getTranslation(position);
//				return new SbVec3f(position.x,position.y,position.z - 0.4f + 1.75f - 0.13f );
//			}
//		});
//
//		viewer.setForceProvider(new ForceProvider() {
//
//			@Override
//			public void apply(SbVec3f force) {
//				//pRigidBody.clearForces();
//				//pRigidBody.clearForces();
//				pRigidBody.applyCentralImpulse(new Vector3(force.getX(),force.getY(),force.getZ()));
//			}
//		});

        //Dickinson, Chris. Learning Game Physics with Bullet Physics and OpenGL (Kindle Locations 801-807). Packt Publishing. Kindle Edition.

        // _____________________________________________________ Physics with bullet physics (End)

        // _____________________________________________________ Physics with ODE4j

        OdeHelper.initODE2(0);
        world = OdeHelper.createWorld();
        world.setAutoDisableFlag(false);
        world.setERP(0.2);
        world.setCFM(1e-5);
        world.setContactMaxCorrectingVel(0.5);
        world.setContactSurfaceLayer(0.01);
        space = OdeHelper.createHashSpace();
        contactGroup = OdeHelper.createJointGroup();
        SbVec3f cameraPositionValue = camera.position.getValue();
        DGeom water = OdeHelper.createPlane(space, 0, 0, 1, -Z_TRANSLATION + 1000 - 150);

        sg.setSpace(space);

        DHeightfieldData heightFieldData = OdeHelper.createHeightfieldData();

        int nbi = sg.getNbI();
        int nbj = (int) sg.getNbJ();
        float[] pHeightData = new float[nbi * nbj];
        int index = 0;
        for (int i = 0; i < nbi; i++) {
            for (int j = 0; j < nbj; j++) {
                index = i + nbi * j;
                pHeightData[index] = sg.getZ(i/*106*/,/*4927*/j) - Z_TRANSLATION;
                //pHeightData[index] = (float) (cameraPositionValue.getZ() - 1.75f + 0.13f - 0.01f);
            }
        }
        sg.clearRasters();

        double heightFieldWidth = sg.getWidth();
        double heightFieldDepth = sg.getHeight();
        int widthSamples = nbi;
        int depthSamples = nbj;
        double scale = 1;
        double offset = 0;
        double thickness = 10;
        heightFieldData.build(pHeightData, false, heightFieldWidth, heightFieldDepth, widthSamples, depthSamples, scale, offset, thickness, false);
        final DHeightfield heightField = OdeHelper.createHeightfield(space, heightFieldData, true);
        DQuaternion q = new DQuaternion();
        Rotation.dQFromAxisAndAngle(q, 1, 0, 0, Math.PI / 2);
        heightField.setQuaternion(q);
        heightField.setPosition(-SCENE_POSITION.getX() + heightFieldWidth / 2, -SCENE_POSITION.getY() + sg.getExtraDY() + heightFieldDepth / 2, 0);

        File saveGameFile = new File("../savegame.mri");
        if (saveGameFile.exists()) {
            try {
                InputStream in = new FileInputStream(saveGameFile);

                Properties saveGameProperties = new Properties();

                saveGameProperties.load(in);

                float x = Float.valueOf(saveGameProperties.getProperty(HERO_X, String.valueOf(Hero.STARTING_X)));

                float y = Float.valueOf(saveGameProperties.getProperty(HERO_Y, String.valueOf(Hero.STARTING_Y)));

                float z = Float.valueOf(saveGameProperties.getProperty(HERO_Z, String.valueOf(Hero.STARTING_Z/* - SCENE_POSITION.getZ()*/)));

                float cat_x = Float.valueOf(saveGameProperties.getProperty(CAT_X, String.valueOf(Hero.STARTING_X)));

                float cat_y = Float.valueOf(saveGameProperties.getProperty(CAT_Y, String.valueOf(Hero.STARTING_Y)));

                float cat_z = Float.valueOf(saveGameProperties.getProperty(CAT_Z, String.valueOf(Hero.STARTING_Z)));

                hero.life = Float.valueOf(saveGameProperties.getProperty(LIFE, String.valueOf(1.0f)));

                previousTimeSec = Double.valueOf(saveGameProperties.getProperty(TIME, "0"));

                camera.position.setValue(x, y, z - SCENE_POSITION.getZ());

                timeStop = "true".equals(saveGameProperties.getProperty(TIME_STOP, "false"));

                hero.fly = "true".equals(saveGameProperties.getProperty(FLY, "false")) ? true : false;

                allowFly = "true".equals(saveGameProperties.getProperty(ALLOW_FLY, "false"));

                sg.loadShots(saveGameProperties);

                loadingQuestIndex = Integer.valueOf(saveGameProperties.getProperty(QUEST_INDEX, "0"));

                sg.setBoots("true".equals(saveGameProperties.getProperty(BOOTS, "false")));

                sg.loadEnemiesKills(saveGameProperties);

                sg.setCatPosition(new SbVec3f(cat_x, cat_y, cat_z - SCENE_POSITION.getZ()));

                in.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else {
            camera.position.setValue(Hero.STARTING_X, Hero.STARTING_Y, Hero.STARTING_Z - SCENE_POSITION.getZ());
            newGameAtStart = true;
        }
        viewer.getCameraController().changeCameraValues(camera);

        viewer.getSceneHandler().setClearBeforeRender(false/*true*/);
        viewer.getSceneHandler().setBackgroundColor(/*new SbColor(0,0,1)*/SceneGraphIndexedFaceSetShader.SKY_COLOR.darker());

        viewer.getSceneHandler().setTransparencyType(TransparencyType.BLEND/*SORTED_LAYERS_BLEND*/);

        sg.setPosition(SCENE_POSITION.getX(), SCENE_POSITION.getY()/*,SCENE_POSITION.getZ()*/);

        SwingUtilities.invokeLater(() -> {
            runVisu();
        });
    }

    public static void runVisu() {
        int style = 0;//SWT.NO_BACKGROUND;

        // ____________________________________________________________________________________ Building OpenGL widget
        GLData glf = new GLData(/*GLProfile.getDefault()*/);
        glf.name = "Mount Rainier Island";
        glf.redSize = 10;
        glf.greenSize = 10;
        glf.blueSize = 10;
        glf.alphaSize = 0;
        glf.depthSize = 32;
        glf.doubleBuffer = true;
        glf.majorVersion = 4;//2;//3;
        glf.minorVersion = 0;//1;
        glf.api = GLData.API.GL;
        glf.profile = GLData.Profile.CORE;
        glf.debug = DEBUG_MODE;//true;
        glf.grabCursor = !DEBUG_MODE;
        glf.waitForRefresh = true;
        viewer.setFormat(glf, style);

        viewer.buildWidget(style);
        viewer.setVisible(true);
        viewer.setVisible(false);
        viewer.setVisible(true);

        GL2 gl2 = new GL2() {
        };
        gl2.glClearColor(0, 0, 0, 1);
        gl2.glClear(GL2.GL_COLOR_BUFFER_BIT);
        viewer.swapBuffers();

        window.setVisible(false);

        final double computerStartTimeCorrected = COMPUTER_START_TIME_SEC - (double) System.nanoTime() / 1e9;//60*60*4.5 / TimeConstants./*JMEMBA_TIME_ACCELERATION*/GTA_SA_TIME_ACCELERATION;

        if (previousTimeSec == 0) {
            viewer.setStartDate(computerStartTimeCorrected);
        } else {
            viewer.setStartDate(previousTimeSec - (double) System.nanoTime() / 1e9);
        }

        if (timeStop) {
            viewer.toggleTimeStop();
        }

        viewer.setAllowToggleFly(allowFly);

        if (hero.fly) {
            viewer.toggleFly();
        }

        viewer.addIdleListener((viewer1) -> {
            double nowSec = viewer.getNow();
            double nowHour = nowSec / 60 / 60;
            double nowDay = 100;//nowHour / 24; // always summer
            double nowGame = nowHour * TimeConstants./*JMEMBA_TIME_ACCELERATION*/GTA_SA_TIME_ACCELERATION;
            double Phi = 47;
            SbVec3f sunPosition = Soleil.soleil_xyz((float) nowDay, (float) nowGame, (float) Phi);
            sg.setSunPosition(new SbVec3f(-sunPosition.y(), -sunPosition.x(), sunPosition.z()));
        });
        viewer.addIdleListener((viewer1) -> {
            sg.idle();
        });

        //shell.open();

        // create a cursor with a transparent image
//	    Color white = display.getSystemColor(SWT.COLOR_WHITE);
//	    Color black = display.getSystemColor(SWT.COLOR_BLACK);
//	    PaletteData palette = new PaletteData(new RGB[] { white.getRGB(), black.getRGB() });
//	    ImageData sourceData = new ImageData(16, 16, 1, palette);
//	    sourceData.transparentPixel = 0;
        Cursor cursor = new Cursor();//display, /*sourceData*/null, 0, 0);

//	    shell.getDisplay().asyncExec(new Runnable() {
//	        public void run() {
//	    		shell.setFullScreen(true);
//	            shell.forceActive();
//	        }
//	    });
//	    shell.forceActive();
//	    shell.forceFocus();

        viewer.setCursor(cursor);

        int pixelPerInch = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
        System.out.println("PixelPerInch: " + pixelPerInch);

        viewer.getSceneHandler().getSceneManager().getGLRenderAction().getViewportRegion().setPixelsPerInch(pixelPerInch);

        viewer.start();
        viewer.updateLocation(new SbVec3f(0.0f, 0.0f, 0.0f), ForceProvider.Direction.STILL);


        //viewer.getGLWidget().maximize();

        // run the event loop as long as the window is open
//		while (!shell.isDisposed()) {
//		    // read the next OS event queue and transfer it to a SWT event
//		    if (!display.readAndDispatch())
//		     {
//		    // if there are currently no other OS event to process
//		    // sleep until the next OS event is available
//			  //viewer.getSceneHandler().getSceneGraph().touch();
//		        display.sleep();
//		    	//viewer.idle();
//		     }
//		}
        System.gc();

        int[] depthBits = new int[1];
        gl2.glGetIntegerv(GL2.GL_DEPTH_BITS, depthBits);

        System.out.println("Depth Buffer : " + depthBits[0]);

        String glVersion = (String) GL11.glGetString(GL2.GL_VERSION);

        System.out.println("GL Version : " + glVersion);

        String glVendor = (String) GL11.glGetString(GL2.GL_VENDOR);

        System.out.println("GL Vendor : " + glVendor);

        String glRenderer = (String) GL11.glGetString(GL2.GL_RENDERER);

        System.out.println("GL Renderer : " + glRenderer);

        // ______________________________________________________________________________________________________ planks
        File planksFile = new File("planks.mri");

        if (!planksFile.exists()) {
            planksFile = new File("../planks.mri");
        }

        if (planksFile.exists()) {
            try {
                InputStream in = new FileInputStream(planksFile);

                Properties planksProperties = new Properties();

                planksProperties.load(in);

                in.close();

                SbViewportRegion vpRegion = viewer.getSceneHandler().getViewportRegion();

                sg.loadPlanks(vpRegion, planksProperties);

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }

        world.setGravity(0, 0, -9.81);

        final float above_ground = //4.5f; // Necessary for planks
                0.2f; // Necessary when respawning on water

        SoCamera camera = viewer.getCameraController().getCamera();

        SbVec3f cameraPositionValue = camera.position.getValue();

        final DBody body = OdeHelper.createBody(world);
        body.setPosition(cameraPositionValue.getX(), cameraPositionValue.getY(), cameraPositionValue.getZ() - /*1.75f / 2*/0.4f + 0.13f + above_ground);
        DMass m = OdeHelper.createMass();
        m.setBox(1000.0f, 0.25, 0.25, 1.312);
        body.setMass(m);
        body.setMaxAngularSpeed(0);
        hero.setBody(body);

        sg.setHero(hero);

        //DGeom box = OdeHelper.createCapsule(space, 0.4, 1.75 - 2 * 0.4);
        DGeom box = OdeHelper.createSphere(space, 0.4);
        box.setBody(body);

//		DRay ray = OdeHelper.createRay(space, 10000);
//		DVector3C direction = ray.getDirection();
//		//DBody rayBody = OdeHelper.createBody(world);
//		ray.setBody(body);
//		//ray.set(cameraPositionValue.getX(), cameraPositionValue.getY(), cameraPositionValue.getZ() - 1.75f/2 + 0.13f,0,0,-1);

        DGeom ball = OdeHelper.createSphere(space, 0.4);
        final DBody ballBody = OdeHelper.createBody(world);
        ballBody.setPosition(
                body.getPosition().get0()/*cameraPositionValue.getX()*/,
                body.getPosition().get1()/*cameraPositionValue.getY()*/,
                body.getPosition().get2() /*cameraPositionValue.getZ() - 0.4f + 0.13f + above_ground*/ - 1.75f + 2 * 0.4f);
        DMass ballm = OdeHelper.createMass();
        ballm.setSphere(1000.0f, 0.25);
        ballBody.setMass(ballm);

        hero.ballBody = ballBody;

        //sg.setBallBody(ballBody);

        ball.setBody(ballBody);

//		final DAMotorJoint joint = OdeHelper.createAMotorJoint(world,null);
//		joint.attach(body,ballBody);
//		joint.setNumAxes(3);
//		joint.setAxis(0,1,1,0,0);
//		joint.setAxis(1,1,0,1,0);
//		joint.setAxis(2,1,0,0,1);
//		joint.setParamVel(0.0f);
//		joint.setParamFMax(9999);
//		joint.setParamVel2(0.0f);
//		joint.setParamFMax2(9999);
//		joint.setParamVel3(0.0f);
//		joint.setParamFMax3(9999);

        final DHingeJoint hinge2Joint = OdeHelper.createHingeJoint(world, null);
        hinge2Joint.attach(body, ballBody);
        hinge2Joint.setAnchor(ballBody.getPosition());
        //hinge2Joint.setAxis1 (0,0,1);
        hinge2Joint.setAxis(0, 1, 0);
        hinge2Joint.setParamVel(0);
        //hinge2Joint.setParamVel2(0);
        hinge2Joint.setParamFMax(1000);
        //hinge2Joint.setParamFMax2(100);
//		hinge2Joint.setParamFudgeFactor(0.1);
//		hinge2Joint.setParamSuspensionERP (0.4);
//		hinge2Joint.setParamSuspensionCFM (0.8);

        DGeom.DNearCallback callback = new DGeom.DNearCallback() {
            @Override
            public void call(Object data, DGeom geom1, DGeom geom2) {
                // Get the rigid bodies associated with the geometries
                DBody body1 = geom1.getBody();// dGeomGetBody(geom1);
                DBody body2 = geom2.getBody();// dGeomGetBody(geom2);

                // Maximum number of contacts to create between bodies (see ODE documentation)
                int MAX_NUM_CONTACTS = 8;
                //dContact contacts[MAX_NUM_CONTACTS];
                DContactBuffer contacts = new DContactBuffer(MAX_NUM_CONTACTS);

                // Add collision joints
                int numc = OdeHelper.collide(geom1, geom2, MAX_NUM_CONTACTS, contacts.getGeomBuffer());

                if ((geom1 instanceof DRay || geom2 instanceof DRay)) {

                    double force = 0;
                    if (numc != 0) {
                        DContact contact = contacts.get(0);
                        DContactGeom contactGeom = contact.getContactGeom();
                        double depth = contactGeom.depth;

                        force = 50 * depth;
                        if (geom1 instanceof DRay) {
                            double mass = body1.getMass().getMass();
                            body1.addForce(0, 0, force * mass - 10 * body1.getLinearVel().get2() * mass);
                            //if (depth < 0.2) {
                            DVector3 lvDir = body1.getLinearVel().clone();
                            if (lvDir.lengthSquared() != 0) {
                                lvDir.normalize();
                                lvDir.scale(-mass * 3);
                                body1.addForce(lvDir);
                            }
                            DVector3 lv = body1.getLinearVel().clone();
                            lv.scale(-mass * 0.5);
                            body1.addForce(lv);
                            //}
                        } else {
                            double mass = body2.getMass().getMass();
                            body2.addForce(0, 0, force * mass - 10 * body2.getLinearVel().get2() * mass);
                            //if (depth < 0.2) {
                            DVector3 lvDir = body2.getLinearVel().clone();
                            if (lvDir.lengthSquared() != 0) {
                                lvDir.normalize();
                                lvDir.scale(-mass * 3);
                                body2.addForce(lvDir);
                            }
                            DVector3 lv = body2.getLinearVel().clone();
                            lv.scale(-mass * 0.5);
                            body2.addForce(lv);
                            //}
                        }
                    }
                    return;
                }

                for (int i = 0; i < numc; ++i) {
                    DContact contact = contacts.get(i);
                    contact.surface.mode = OdeConstants.dContactSoftERP | OdeConstants.dContactSoftCFM | OdeConstants.dContactApprox1 |
                            OdeConstants.dContactSlip1 | OdeConstants.dContactSlip2;

                    //contact.surface.bounce = 0.1;
                    contact.surface.mu = ((double[]) data)[0];//0.8;//50.0;
                    contact.surface.soft_erp = 0.96;
                    contact.surface.soft_cfm = 1e-5;
                    contact.surface.rho = 0;
                    contact.surface.rho2 = 0;

                    // struct dSurfaceParameters {
                    //      int mode;
                    //      dReal mu;
                    //      dReal mu2;
                    //      dReal rho;
                    //      dReal rho2;
                    //      dReal rhoN;
                    //      dReal bounce;
                    //      dReal bounce_vel;
                    //      dReal soft_erp;
                    //      dReal soft_cfm;
                    //      dReal motion1, motion2, motionN;
                    //      dReal slip1, slip2;
                    // };

//					DContactJoint contactJoint = OdeHelper.createContactJoint(/*collision_data->world*/world,
//							/*collision_data->contact_group*/contactGroup, contacts.get(i));
//
//					contactJoint.attach(body1, body2);
                }
            }
        };


        // Maximum number of contacts to create between bodies (see ODE documentation)
        final int MAX_NUM_CONTACTS = 3;//8;
        //dContact contacts[MAX_NUM_CONTACTS];
        //Don't reuse contacts between callbacks, it causes errors when on planks
        //final DContactBuffer contacts = new DContactBuffer(MAX_NUM_CONTACTS);

        DGeom.DNearCallback callback2 = new DGeom.DNearCallback() {

            @Override
            public void call(Object data, DGeom geom1, DGeom geom2) {

                boolean withCapsule = false;
                if (geom1 instanceof DCapsule || geom2 instanceof DCapsule) {
                    withCapsule = true;
                }

                // Get the rigid bodies associated with the geometries
                DBody body1 = geom1.getBody();// dGeomGetBody(geom1);
                DBody body2 = geom2.getBody();// dGeomGetBody(geom2);

                // Can happen outside island
                if (body1 != null && Double.isNaN(body1.getPosition().get0())) {
                    return;
                }

                if (body2 != null && Double.isNaN(body2.getPosition().get0())) {
                    return;
                }

                if (body1 == null && body2 == null) {
                    return; // no joint between two still objects
                }

//				if (body1 == body && body2 == ballBody) {
//					return;
//				}
//
//				if (body2 == body && body1 == ballBody) {
//					return;
//				}
                final DContactBuffer contacts = new DContactBuffer(MAX_NUM_CONTACTS);

                // Add collision joints
                int numc = OdeHelper.collide(geom1, geom2, MAX_NUM_CONTACTS, contacts.getGeomBuffer());

                for (int i = 0; i < numc; ++i) {
                    DContact contact = contacts.get(i);
                    contact.surface.mode = OdeConstants.dContactSoftERP | OdeConstants.dContactSoftCFM | OdeConstants.dContactApprox1 |
                            OdeConstants.dContactSlip1 | OdeConstants.dContactSlip2;

                    //contact.surface.bounce = 0.1;
                    contact.surface.mu = sg.getMU();//0.838;//((double[]) data)[0];//0.8;//50.0;
                    contact.surface.slip1 = 0;//.1;
                    contact.surface.slip2 = 0;//.1;
                    contact.surface.soft_erp = 0.96;
                    contact.surface.soft_cfm = 1e-5;
                    contact.surface.rho = 0;
                    contact.surface.rho2 = 0;

                    // struct dSurfaceParameters {
                    //      int mode;
                    //      dReal mu;
                    //      dReal mu2;
                    //      dReal rho;
                    //      dReal rho2;
                    //      dReal rhoN;
                    //      dReal bounce;
                    //      dReal bounce_vel;
                    //      dReal soft_erp;
                    //      dReal soft_cfm;
                    //      dReal motion1, motion2, motionN;
                    //      dReal slip1, slip2;
                    // };

                    DContactJoint contactJoint = OdeHelper.createContactJoint(/*collision_data->world*/world,
                            /*collision_data->contact_group*/contactGroup, contacts.get(i));

                    contactJoint.attach(body1, body2);
                }

            }
        };

        final double[] data = new double[1];
        data[0] = 100.0;//0.8;

        final int nb_step = 25;
        final double max_physics_frequency = 250;

        final boolean firstDT[] = new boolean[1];
        firstDT[0] = true;

        DVector3 saved_pos = new DVector3();

        viewer.addIdleListener((viewer1) -> {

            // TODO : getGroundZ() is not accurate
            float camz = sg.getGroundZ() + 1.75f - 0.13f;

            float zref = camz - 0.4f + 0.13f;

            if (viewer1.isFlying()) {
                saved_pos.set0(camera.position.getValue().getX());
                saved_pos.set1(camera.position.getValue().getY());
                saved_pos.set2(camera.position.getValue().getZ()/*zref + 1.0f*/ - /*1.75f / 2*/0.4f + 0.13f + above_ground);
                hero.setPosition(saved_pos);
                //body.setLinearVel(0,0,0);
                //ballBody.setPosition(camera.position.getValue().getX(), camera.position.getValue().getY(), camera.position.getValue().getZ() - /*1.75f / 2*/0.4f + 0.13f - 1.75f+ 2*0.4f + above_ground);
                ballBody.setPosition(
                        hero.getPosition().getX()/*cameraPositionValue.getX()*/,
                        hero.getPosition().getY()/*cameraPositionValue.getY()*/,
                        hero.getPosition().getZ() /*cameraPositionValue.getZ() - 0.4f + 0.13f + above_ground*/ - 1.75f + 2 * 0.4f);
                return;
            }

            double nbSteps = firstDT[0] ? 999 : nb_step;

            final double dt = Math.min(0.25, viewer1.dt()); // 4 FPS min
            if (!firstDT[0] && max_physics_frequency < nbSteps / dt) {
                nbSteps = Math.ceil(max_physics_frequency * dt);
            }
            firstDT[0] = false;

            for (int i = 0; i < nbSteps; i++) {
                physics_error = false;
                saved_pos.set(body.getPosition());
                ((DxHashSpace) space).collide(data, /*callback*/callback2);
                world.step(dt / nbSteps);
                contactGroup.empty();
                if (physics_error) {
                    saved_pos.add2(0.1);
                    hero.setPosition(saved_pos);
                    ballBody.setPosition(
                            hero.getPosition().getX()/*cameraPositionValue.getX()*/,
                            hero.getPosition().getY()/*cameraPositionValue.getY()*/,
                            hero.getPosition().getZ() /*cameraPositionValue.getZ() - 0.4f + 0.13f + above_ground*/ - 1.75f + 2 * 0.4f);
                }
            }
            if (hero.getPosition().getZ() < zref - 1.9f) {
                System.err.println("Error in placement, too low");
                saved_pos.set2(zref + 1.0f);
                hero.setPosition(saved_pos);
                ballBody.setPosition(
                        hero.getPosition().getX()/*cameraPositionValue.getX()*/,
                        hero.getPosition().getY()/*cameraPositionValue.getY()*/,
                        hero.getPosition().getZ() /*cameraPositionValue.getZ() - 0.4f + 0.13f + above_ground*/ - 1.75f + 2 * 0.4f);
            }
            sg.updateTargetPositions(dt);
        });

        ErrorHandler.dMessageFunction function = new ErrorHandler.dMessageFunction() {
            @Override
            public void call(int errnum, String msg, Object... ap) {
                physics_error = true;
                System.err.println(msg);
            }
        };

        ErrorHdl.dSetMessageHandler(function);

        viewer.setPositionProvider(new PositionProvider() {
            @Override
            public SbVec3f getPosition() {
                SbVec3f position = hero.getPosition();

                if (Double.isNaN(position.getX())) {
                    return null;
                }

                return new SbVec3f((float) position.getX(), (float) position.getY(), (float) position.getZ() + /*1.75f / 2*/0.4f - 0.13f);
            }
        });

        viewer.setForceProvider(new ForceProvider() {

            @Override
            public void apply(SbVec3f force, Direction direction) {
                if (force.length() == 0) {
                    data[0] = 1.0;
                } else {
                    //force.setZ(82*200/2000);
                    data[0] = 0;
                }
                //body.addForce(force.getX() * 2000, force.getY() * 2000, force.getZ() * 2000);
                SbVec3f xvec = new SbVec3f(0, 0, 1);
                SbVec3f vwVec = camera.orientation.getValue().multVec(new SbVec3f(0, 0, -1));
                //SbVec3f dir = camera.orientation.getValue().multVec(xvec);
                switch (direction) {

                    case STILL:
                        hinge2Joint.setAxis(-vwVec.y(), vwVec.x(), 0);
                        hinge2Joint.setParamVel(0);
                        break;
                    case FRONT:
                        hinge2Joint.setAxis(-vwVec.y(), vwVec.x(), 0);
                        hinge2Joint.setParamVel(-10 / 0.4);
                        break;
                    case BACK:
                        hinge2Joint.setAxis(-vwVec.y(), vwVec.x(), 0);
                        hinge2Joint.setParamVel(10);
                        break;
                    case LEFT:
                        hinge2Joint.setAxis(vwVec.x(), vwVec.y(), 0);
                        hinge2Joint.setParamVel(10);
                        break;
                    case RIGHT:
                        hinge2Joint.setAxis(vwVec.x(), vwVec.y(), 0);
                        hinge2Joint.setParamVel(-10);
                        break;
                }
            }
        });

        viewer.setEscapeCallback((viewer2) -> {

            if (!viewer2.isVisible()) {
                viewer2.getGLWidget().setVisible(false);
                return;
            }

            if (!viewer2.isTimeStop()) {
                viewer2.toggleTimeStop();
            }
            viewer.addOneShotIdleListener((viewer3) -> {
                glfwPollEvents();
                viewer2.setVisible(false);
                glfwPollEvents();

                viewer.addOneShotIdleListener((viewer4) -> {
                    glfwPollEvents();
                    OptionDialog dialog = new OptionDialog(viewer, sg);
                    dialog.setUndecorated(true);
                    dialog.setVisible(true);
                    glfwPollEvents();
                    dialog.pack();
                    glfwPollEvents();
                    dialog.setLocationRelativeTo(null);
                    glfwPollEvents();
                    dialog.setBounds(GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds());
                    glfwPollEvents();
                    dialog.setAlwaysOnTop(true);
                    viewer.addOneShotIdleListener((viewer5) -> {
                        glfwPollEvents();
                    });
                });
            });
        });
/*
		viewer.addIdleListener((viewer2)->{

			if(!viewer2.isVisible() && viewer2.getGLWidget().isVisible()) {
				viewer2.getGLWidget().setVisible(false);
				return;
			}
		});
*/
        // _____________________________________________________ Physics with ODE4j (End)

        viewer.addIdleListener((viewer1) -> {
                    sg.setFPS(viewer1.getFPS());
                }
        );

        viewer.addIdleListener((viewer1) -> {
            sg.displayObjectives(viewer1);
        });

        final float delta_volume = 0.02f;

        viewer.addIdleListener((viewer1) -> {
            if (Double.isNaN(last_call)) {
                last_call = System.nanoTime();
            } else {
                float distanceFromBeach = sg.getDistanceFromBeach();
                float atmosphericAbsorption = (float) Math.pow(0.01, distanceFromBeach / 1000.0f);
                float seaClipVolume = 1.0f / (15.0f + distanceFromBeach / 40.0f) * atmosphericAbsorption;
//                if (seaClip != null) {
//                    setVolume(seaClip, seaClipVolume);
//                }
//                if (forestClip != null) {
//                    setVolume(forestClip, 1 - 15.0f * seaClipVolume);
//                }

                if (System.nanoTime() - last_call > 0.5e9) {
                    last_call = System.nanoTime();
                    if (seaRenderer != null) {
                        if (seaRendererVolume != 0 && seaClipVolume != 0) {
                            float ratio = seaRendererVolume / seaClipVolume;
                            if (ratio > 1 + delta_volume || ratio < 1 - delta_volume) {
                                seaRendererVolume = seaClipVolume;
                            }
                        }
                        else {
                            seaRendererVolume = seaClipVolume;
                        }
                    }
                    if (forestRenderer != null) {
                        float forestClipVolume = Math.max(1 - 15.0f * seaClipVolume, 0);
                        if (forestRendererVolume != 0 && forestClipVolume != 0) {
                            float ratio = forestRendererVolume / forestClipVolume;
                            if (ratio > 1 + delta_volume || ratio < 1 - delta_volume) {
                                forestRendererVolume = forestClipVolume;
                            }
                        }
                        else {
                            forestRendererVolume = forestClipVolume;
                        }
                    }
                }
            }
        });

        final int[] id = new int[1];

        final int[] idTrail = new int[1];

        if (god) {
            viewer.addKeyDownListener(SoKeyboardEvent.Key.P, () -> {


                SbViewportRegion vr = viewer.getSceneHandler().getViewportRegion();
                SoNode sg_ = viewer.getSceneHandler().getSceneGraph();

                SoRayPickAction fireAction = new SoRayPickAction(vr);

                fireAction.setPoint(vr.getViewportSizePixels().operator_div(2));
                fireAction.setRadius(2.0f);
                fireAction.apply(sg_);
                SoPickedPoint pp = fireAction.getPickedPoint();

                if (pp != null) {
                    SoPath p = pp.getPath();
                    if (p != null) {
                        SoNode n = p.getTail();
                        if (n instanceof SoCube && Objects.equals(n.getName().getString(), "plank")) {
                            int len = p.getLength();
                            if (len >= 2) {
                                SoNode sep = p.getNode(len - 2);
                                sg.removePlank(sep);
                                fireAction.destructor();
                                return;
                            }
                        }
                    }
                }

                fireAction.destructor();

                SbVec3f translation = new SbVec3f();

                SoCamera vcamera = viewer.getCameraController().getCamera();

                translation.setValue(vcamera.position.getValue());
                SbVec3f axis = new SbVec3f();
                axis.setValue(0, 0, 1);
                SbRotation rotation = vcamera.orientation.getValue();
                SbRotation rot2 = new SbRotation();
                rot2.setValue(new SbVec3f(1, 0, 0), (float) -Math.PI / 2);
                sg.addPlank(translation, rot2.operator_mul(rotation));
                id[0] = 1;
                System.out.println("plank");
            });

            viewer.addIdleListener((viewer1) -> {
                if (id[0] == 0) {
                    return;
                }
                SbVec3f translation = new SbVec3f();

                SoCamera vcamera = viewer.getCameraController().getCamera();

                translation.setValue(vcamera.position.getValue());
                SbVec3f axis = new SbVec3f();
                axis.setValue(0, 0, 1);
                SbRotation rotation = vcamera.orientation.getValue();
                SbRotation rot2 = new SbRotation();
                rot2.setValue(new SbVec3f(1, 0, 0), (float) -Math.PI / 2);

                SbViewportRegion vpRegion = viewer1.getSceneHandler().getViewportRegion();

                sg.movePlank(vpRegion, translation, rot2.operator_mul(rotation));
            });

            viewer.addKeyUpListener(SoKeyboardEvent.Key.P, () -> {
                id[0] = 0;
            });

            viewer.addKeyDownListener(SoKeyboardEvent.Key.O, () -> {
                idTrail[0] = 1;
                System.out.println("trail");
            });

            viewer.addIdleListener((viewer1) -> {
                if (idTrail[0] == 0) {
                    return;
                }
                float ifloat = sg.getIFloat(sg.getPosition().x());
                float jfloat = sg.getJFloat(sg.getPosition().y());

                int i0 = (int) Math.floor(ifloat);
                int j0 = (int) Math.floor(jfloat);

                for (int di = -1; di < 3; di++) {
                    for (int dj = -1; dj < 3; dj++) {
                        sg.addTrail(i0 + di, j0 + dj);
                    }
                }
            });

            viewer.addKeyUpListener(SoKeyboardEvent.Key.O, () -> {
                idTrail[0] = 0;
            });
        } // end GOD

//		final long[] nanotime = new long[1];
//		nanotime[0] = -1;

//		viewer.getSceneHandler().getGLRenderAction().setAbortCallback(new SoGLRenderAction.SoGLRenderAbortCB() {
//			@Override
//			public SoGLRenderAction.AbortCode abort(Object userData) {
//				long newTime = System.nanoTime();
//				if (newTime -
//						/*nanotime[0]*/viewer.getStartPaintTime() > 1e9/80 /*&& nanotime[0] != -1*/) {
//					//nanotime[0] = newTime;
//					return SoGLRenderAction.AbortCode.PRUNE;
//				}
//				//nanotime[0] = newTime;
//				return SoGLRenderAction.AbortCode.CONTINUE;
//			}
//		}, null);

        scenario.start(loadingQuestIndex, viewer);

        viewer.addIdleListener((viewer1) -> {
            scenario.idle(viewer1);
        });

        viewer.addIdleListener((v) -> {
            if (hero.life <= 0) {
                hero.life = 1;
                SwingUtilities.invokeLater(() -> {
                    sg.newGame(v);
                });
            }
        });

        SwingUtilities.invokeLater(() -> {
            try {
                loop();
            } catch (Exception e) {
                viewer.setVisible(false);
                JOptionPane.showMessageDialog(window, e.toString(), "Exception in Mount Rainier Island", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(-1); // Necessary, because of Linux
            } catch (Error e) {
                viewer.setVisible(false);
                JOptionPane.showMessageDialog(window, e.toString(), "Error in Mount Rainier Island", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
                System.exit(-1); // Necessary, because of Linux
            }

//// Can call "alc" functions at any time
//			long device = alcOpenDevice((ByteBuffer)null);
//			ALCCapabilities deviceCaps = ALC.createCapabilities(device);
//
//			long context = alcCreateContext(device, (IntBuffer)null);
//			alcMakeContextCurrent(context);
//			AL.createCapabilities(deviceCaps);
//// Can now call "al" functions
//
//			IntBuffer buffer = BufferUtils.createIntBuffer(1);
//			AL10.alGenBuffers(buffer);
//
//			String args = "ressource/" + "AMBSea_Falaise 2 (ID 2572)_LS_16bit.wav";
//			File file = new File(args);
//			if (!file.exists()) {
//				file = new File("application/" + args);
//			}
//			URL url = null;
//			try {
//				url = file.toURL();
//			} catch (MalformedURLException e) {
//				e.printStackTrace();
//			}
//
//			long time = 0;
//			try {
//				time = createBufferData(buffer.get(0),url);
//			} catch (UnsupportedAudioFileException e) {
//				e.printStackTrace();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}

            //InputStream seaSound = loadMP3Sound("AMBSea_Falaise 2 (ID 2572)_LS_16bit.mp3");

            //seaClip = playSound(seaSound,true, 0.0001f);

            AtomicInteger seaAtomicInteger = new AtomicInteger();

            VorbisTrack seaTrack = new VorbisTrack("ressource/AMBSea_Falaise 2 (ID 2572)_LS_Audacity_Quality_6.ogg", seaAtomicInteger);

            final float[] currentSeaVolume = new float[1];

            seaThread = new Thread(() -> {

                seaRenderer = new AudioRenderer(seaTrack);

                seaRenderer.play();

                seaRenderer.setVolume(0.0f);

                seaAudioLatch.countDown();

                while (!shouldClose) {
                    if (!seaRenderer.update(true)) {
                        System.err.println("Playback failed.");
                    }
                    try {
                        sleep(1000 / 30);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    if (currentSeaVolume[0] != seaRendererVolume) {
                        currentSeaVolume[0] = seaRendererVolume;
                        seaRenderer.setVolume(seaRendererVolume);
                    }
                }
                seaRenderer.close();
                seaTrack.close();
            }) {

            };
            seaThread.start();

            try {
                seaAudioLatch.await();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }

            //seaRenderer.getProgressUpdater().makeCurrent(true);

            //InputStream forestSound = loadMP3Sound("STORM_Orage et pluie 4 (ID 2719)_LS_audacity.mp3");

            //forestClip = playSound(forestSound, true, 1.0f);

            VorbisTrack forestTrack = new VorbisTrack("ressource/STORM_Orage et pluie 4 (ID 2719)_LS_Audacity_Quality_6.ogg", seaAtomicInteger);

            final float[] currentForestVolume = new float[1];

            forestThread = new Thread(() -> {

                forestRenderer = new AudioRenderer(forestTrack);

                forestRenderer.play();

                forestRenderer.setVolume(0.0f);

                forestAudioLatch.countDown();

                while (!shouldClose) {
                    if (!forestRenderer.update(true)) {
                        System.err.println("Playback failed.");
                    }
                    try {
                        sleep(1000 / 30);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    if (currentForestVolume[0] != forestRendererVolume) {
                        currentForestVolume[0] = forestRendererVolume;
                        forestRenderer.setVolume(forestRendererVolume);
                    }
                }
                forestRenderer.close();
                forestTrack.close();
            }) {

            };
            forestThread.start();

            try {
                forestAudioLatch.await();
            } catch (InterruptedException e) {
                throw new IllegalStateException(e);
            }

            boolean success = viewer.setFocus();

            Robot bot = null;
            try {
                bot = new Robot();
                bot.mousePress(InputEvent.BUTTON2_DOWN_MASK);
                bot.mouseRelease(InputEvent.BUTTON2_DOWN_MASK);
            } catch (AWTException e) {
                e.printStackTrace();
            }

            lastAliveMillis[0].set(System.currentTimeMillis() + 10000);

            final Thread aliveThread = new Thread(() -> {
                while (true) {
                    try {
                        sleep(100);
                        if (System.currentTimeMillis() - lastAliveMillis[0].get() > 5000) {
                            File graphicsFile = new File("graphics.mri");
                            if (graphicsFile.exists()) {
                                Toolkit.getDefaultToolkit().beep();
//								graphicsFile.delete();
                            }
//							System.exit(-2);
                        }
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                }
            });
            //aliveThread.start();
        });
    }

    public static void loop() {
        display.readAndDispatch();

        if (display.shouldClose()) {
            shouldClose = true;
            try {
                seaThread.join();
                forestThread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            SwingUtilities.invokeLater(() -> {

                        sg.preDestroy();

                        viewer.destructor();

                        viewer = null;

                        // disposes all associated windows and their components
                        display.dispose();

                        display = null;

                        sg = null;

                        window.dispose(); // must be done at the end, for linux

                        window = null;

                        KDebug.dump();

                        System.exit(0); // Necessary, because of Linux
                    }
            );
        } else {
            SwingUtilities.invokeLater(() -> {
                try {
                    loop();
                    lastAliveMillis[0].set(System.currentTimeMillis());

                    if (newGameAtStart) {
                        newGameAtStart = false;
                        viewer.addOneShotIdleListener(v -> {

                            sg.newGame(v);

//				String[] message = new String[2];
//				message[0] = "Go to the oracle."; message[1] = "He is on the right on the path";
//				sg.displayTemporaryMessage(message,30);
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    viewer.setVisible(false);
                    JOptionPane.showMessageDialog(window, e.toString(), "Exception in Mount Rainier Island", JOptionPane.ERROR_MESSAGE);
                    System.exit(-1); // Necessary, because of Linux
                } catch (Error e) {
                    e.printStackTrace();
                    viewer.setVisible(false);
                    JOptionPane.showMessageDialog(window, e.toString(), "Error in Mount Rainier Island", JOptionPane.ERROR_MESSAGE);
                    System.exit(-1); // Necessary, because of Linux
                }
            });
        }
    }

    public static byte[] loadSound(final String url) {
        String args = "ressource/" + url;
        File file = new File(args);
        if (!file.exists()) {
            file = new File("application/" + args);
        }
        byte[] fileContent = null;
        try {
            fileContent = Files.readAllBytes(file.toPath());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return fileContent;
    }

    public static InputStream loadGZipSound(final String url) {
        String args = "ressource/" + url;
        File file = new File(args);
        if (!file.exists()) {
            file = new File("application/" + args);
        }
        InputStream fileContent = null;
        try {
            fileContent = new FileInputStream(file);//Files.readAllBytes(file.toPath());
            return new BufferedInputStream(new GZIPInputStream(fileContent));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return null;
    }

    public static AudioInputStream loadMP3Sound(final String url) {
        String args = "ressource/" + url;
        File file = new File(args);
        if (!file.exists()) {
            file = new File("application/" + args);
        }
        try {
            AudioInputStream in = AudioSystem.getAudioInputStream(file);
            AudioFormat baseFormat = in.getFormat();
            AudioFormat decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                    baseFormat.getSampleRate(),
                    16,
                    baseFormat.getChannels(),
                    baseFormat.getChannels() * 2,
                    baseFormat.getSampleRate(),
                    false);
            AudioInputStream din = AudioSystem.getAudioInputStream(decodedFormat, in);
            return din;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedAudioFileException e) {
            throw new RuntimeException(e);
        }

        return null;
    }

    public static synchronized void playSoundDelayed(final /*String url*/byte[] sound, boolean loop, float volume) {
        new Thread(new Runnable() {
            // The wrapper thread is unnecessary, unless it blocks on the
            // Clip finishing; see comments.
            public void run() {
                playSound(new ByteArrayInputStream(sound), loop, volume);
            }
        }).start();
    }

    public static synchronized Clip playSound(final /*String url*/InputStream sound, boolean loop, float volume) {
//		new Thread(new Runnable() {
        // The wrapper thread is unnecessary, unless it blocks on the
        // Clip finishing; see comments.
//			public void run() {
        try {
            Clip clip = AudioSystem.getClip();
            clip.addLineListener(new LineListener() {

                @Override
                public void update(LineEvent event) {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                }

            });
            AudioInputStream inputStream = sound instanceof AudioInputStream ? (AudioInputStream) sound : AudioSystem.getAudioInputStream(
                    sound);
            clip.open(inputStream);
            setVolume(clip, volume);
            if (loop) {
                clip.loop(Integer.MAX_VALUE);
            } else {
                clip.start();
            }
            return clip;
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return null;
//			}
//		}).start();
    }

    public static synchronized void playSound(Clip clip) {
        new Thread(new Runnable() {
            // The wrapper thread is unnecessary, unless it blocks on the
            // Clip finishing; see comments.
            public void run() {
                try {
                    clip.start();

                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }
        }).start();
    }

    private static long createBufferData(int p, URL fileName) throws UnsupportedAudioFileException, IOException {
        //shortcut finals:
        final int MONO = 1, STEREO = 2;

        AudioInputStream stream = null;
        stream = AudioSystem.getAudioInputStream(fileName);

        AudioFormat format = stream.getFormat();
        if (format.isBigEndian()) throw new UnsupportedAudioFileException("Can't handle Big Endian formats yet");

        //load stream into byte buffer
        int openALFormat = -1;
        switch (format.getChannels()) {
            case MONO:
                switch (format.getSampleSizeInBits()) {
                    case 8:
                        openALFormat = AL10.AL_FORMAT_MONO8;
                        break;
                    case 16:
                        openALFormat = AL10.AL_FORMAT_MONO16;
                        break;
                }
                break;
            case STEREO:
                switch (format.getSampleSizeInBits()) {
                    case 8:
                        openALFormat = AL10.AL_FORMAT_STEREO8;
                        break;
                    case 16:
                        openALFormat = AL10.AL_FORMAT_STEREO16;
                        break;
                }
                break;
        }

        //load data into a byte buffer
        //I've elected to use IOUtils from Apache Commons here, but the core
        //notion is to load the entire stream into the byte array--you can
        //do this however you would like.

        //org.apache.commons.io.IOUtils.toByteArray(stream);

        // https://stackoverflow.com/questions/1264709/convert-inputstream-to-byte-array-in-java

        InputStream is = stream;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] datab = new byte[16384];

        while ((nRead = is.read(datab, 0, datab.length)) != -1) {
            buffer.write(datab, 0, nRead);
        }

        byte[] b = buffer.toByteArray();


        ByteBuffer data = BufferUtils.createByteBuffer(b.length).put(b);
        data.flip();

        //load audio data into appropriate system space....
        AL10.alBufferData(p, openALFormat, data, (int) format.getSampleRate());

        //and return the rough notion of length for the audio stream!
        return (long) (1000f * stream.getFrameLength() / format.getFrameRate());
    }

    public static void setVolume(Clip clip, float volume) {
        if (volume < 0f || volume > 1f)
            throw new IllegalArgumentException("Volume not valid: " + volume);
        FloatControl gainControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
        float volumeDB = 20f * (float) Math.log10(volume);
        if (volumeDB < gainControl.getMinimum() + 1.0f) {
            volumeDB = gainControl.getMinimum() + 1.0f;
        }
        gainControl.setValue(volumeDB);
    }

    public static void setVolume(AudioRenderer clip, float volume) {
        if (volume < 0f || volume > 1f)
            throw new IllegalArgumentException("Volume not valid: " + volume);
        float volumeDB = 20f * (float) Math.log10(volume);
        clip.setVolume(volume);
    }
}