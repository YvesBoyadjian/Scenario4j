package vrmlviewer;

import com.jogamp.opengl.GL2;
import jscenegraph.coin3d.fxviz.nodes.SoShadowGroup;
import jscenegraph.coin3d.inventor.VRMLnodes.SoVRMLImageTexture;
import jscenegraph.coin3d.inventor.nodes.SoFragmentShader;
import jscenegraph.coin3d.inventor.nodes.SoShaderObject;
import jscenegraph.coin3d.inventor.nodes.SoVertexShader;
import jscenegraph.coin3d.shaders.inventor.nodes.SoShaderProgram;
import jscenegraph.coin3d.shaders.inventor.nodes.SoShaderStateMatrixParameter;
import jscenegraph.database.inventor.SbColor;
import jscenegraph.database.inventor.SbViewportRegion;
import jscenegraph.database.inventor.SoInput;
import jscenegraph.database.inventor.SoInputFile;
import jscenegraph.database.inventor.actions.SoGLRenderAction;
import jscenegraph.database.inventor.nodes.*;
import jscenegraph.port.Ctx;
import jsceneviewerawt.inventor.qt.SoQt;
import jsceneviewerawt.inventor.qt.SoQtCameraController;
import jsceneviewerawt.inventor.qt.viewers.SoQtExaminerViewer;
import jsceneviewerawt.inventor.qt.viewers.SoQtFullViewer;
import org.lwjgl.opengl.GLDebugMessageCallback;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.io.File;
import java.util.List;
import static org.lwjgl.opengl.GL43C.*;

public class VRMLViewer {

public static void main(String[] args) {

    SoQt.init("VRMLViewer");

    //JPanel panel = new JPanel();
    JFrame frame = new JFrame("VRMLViewer");
    frame.getContentPane().setBackground(new Color(0,true));
    //frame.getContentPane().add(panel);
    frame.getContentPane().setLayout(new BorderLayout());
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//    frame.pack();
    frame.setLocationRelativeTo(null);
//    frame.setVisible(true);

    //SoCone cube = new SoCone();

    //String path = "C:/Users/Yves Boyadjian/Downloads/83_honda_atc.wrl";
    String path = "C:/Users/Yves Boyadjian/Downloads/doom-combat-scene_wrl/doom combat scene.wrl";

    SbViewportRegion.setDefaultPixelsPerInch(7.20f);

    SoVRMLImageTexture.setDelayFetchURL(false); // Don't wait to load textures

    SoSeparator upperCache = new SoSeparator();

    upperCache.ref();


    SoShaderProgram program = new SoShaderProgram();

    SoVertexShader vs = new SoVertexShader();

    vs.sourceType.setValue(SoShaderObject.SourceType.GLSL_PROGRAM);
    vs.sourceProgram.setValue(
            "#version 330 core\n"+
                    "layout (location = 0) in vec3 aPos;\n"+
                    "uniform mat4 s4j_ModelViewMatrix;\n"+
                    "uniform mat4 s4j_ProjectionMatrix;\n"+
                    "void main()\n"+
                    "{\n"+
                    "gl_Position = s4j_ProjectionMatrix * s4j_ModelViewMatrix * vec4(aPos.x, aPos.y, aPos.z, 1.0);\n"+
                    "}\n");

    final SoShaderStateMatrixParameter mvs = new SoShaderStateMatrixParameter();
    mvs.name.setValue("s4j_ModelViewMatrix");
    mvs.matrixType.setValue(SoShaderStateMatrixParameter.MatrixType.MODELVIEW);

    final SoShaderStateMatrixParameter ps = new SoShaderStateMatrixParameter();
    ps.name.setValue("s4j_ProjectionMatrix");
    ps.matrixType.setValue(SoShaderStateMatrixParameter.MatrixType.PROJECTION);

    vs.parameter.set1Value(0, mvs);
    vs.parameter.set1Value(1, ps);

    program.shaderObject.set1Value(0, vs);

    SoFragmentShader fs = new SoFragmentShader();

    fs.sourceType.setValue(SoShaderObject.SourceType.GLSL_PROGRAM);
    fs.sourceProgram.setValue(
            "#version 330 core\n"+
                    "out vec4 FragColor;\n"+
                    "void main()\n"+
                    "{\n"+
                    "FragColor = vec4(1.0f, 0.5f, 0.2f, 1.0f);\n"+
                    "}\n"
    );

    program.shaderObject.set1Value(1, fs);

    SoShadowGroup shadowGroup = new SoShadowGroup();
    shadowGroup.quality.setValue(1.0f);

    //upperCache.addChild(program);
    upperCache.addChild(shadowGroup);

    SoSeparator cache = new SoSeparator();

    /*upperCache*/shadowGroup.addChild(cache);


    SoText3 text = new SoText3();
    text.string.setValue("Drag an drop your iv/wrl/zip file here");

    cache.addChild(text);
    //cache.addChild(new SoCube());

    SwingUtilities.invokeLater(() -> {
        SoQtExaminerViewer examinerViewer = new SoQtExaminerViewer(
                SoQtFullViewer.BuildFlag.BUILD_ALL,
                SoQtCameraController.Type.BROWSER,
                /*panel*/frame.getContentPane()
        ) {
            public void initializeGL(GL2 gl2) {
                super.initializeGL(gl2);

                int error = glGetError();
                glEnable(GL_DEBUG_OUTPUT);
                error = glGetError();
                glDebugMessageCallback(new GLDebugMessageCallback() {
                    @Override
                    public void invoke(int i, int i1, int i2, int i3, int length, long message, long l1) {
                        String messageStr = getMessage(length,message);
                        System.err.println("OpenGL Error : "+ messageStr);
                    }
                }, 0);
                error = glGetError();
                glEnable(GL_DEBUG_OUTPUT_SYNCHRONOUS);
                error = glGetError();

                final int[] vao = new int[1];
                gl2.glGenVertexArrays(1,vao);
                gl2.glBindVertexArray(vao[0]);

                System.out.println("init");
            }
        };


        //examinerViewer.setAntialiasing(true, 15);
        //examinerViewer.getSceneHandler().setTransparencyType(SoGLRenderAction.TransparencyType.DELAYED_BLEND);
        //examinerViewer.getSceneHandler().setTransparencyType(SoGLRenderAction.TransparencyType.SORTED_LAYERS_BLEND); still bugs with villa Savoye

        examinerViewer.getSceneHandler().setBackgroundColor(new SbColor(0.6f,0.535f,0.28f));

        examinerViewer.buildWidget(0);

        frame.pack();
        frame.setSize(800,600);
        frame.setVisible(true);

        examinerViewer.setSceneGraph(upperCache);

        examinerViewer.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent evt) {
                try {
                    evt.acceptDrop(DnDConstants.ACTION_COPY);
                    List<File> droppedFiles = (List<File>)
                            evt.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    cache.removeAllChildren();

//                    SoCallback callback = new SoCallback();
//
//                    callback.setCallback(action -> {
//                        if(action instanceof SoGLRenderAction) {
//                            SoGLRenderAction glRenderAction = (SoGLRenderAction)action;
//                            GL2 gl2 = Ctx.get(glRenderAction.getCacheContext());
//                            gl2.glEnable(GL2.GL_FRAMEBUFFER_SRGB);
//                            gl2.glLightModeli(GL2.GL_LIGHT_MODEL_LOCAL_VIEWER, GL2.GL_TRUE);
//                        }
//                    });
//                    cache.addChild(callback);

                    String title = "";

                    for (File file : droppedFiles) {
                        SoFile input = new SoFile();
                        input.name.setValue(file.toString());

                        if(file.toString().endsWith(".iv")) {
                            cache.renderCaching.setValue(SoSeparator.CacheEnabled.AUTO);
                        }
                        else {
                            cache.renderCaching.setValue(SoSeparator.CacheEnabled.ON);
                        }

                        cache.addChild(input);
                        examinerViewer.viewAll();

                        title += file.getName()+" ";
                    }
                    frame.setTitle(title);

                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

        });
    });
}
}
