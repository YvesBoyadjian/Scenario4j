package application.gui;

import application.scenegraph.SceneGraphIndexedFaceSetShader;
import application.viewer.glfw.SoQtWalkViewer;
import jscenegraph.database.inventor.SbColor;

import javax.swing.*;
import java.awt.event.*;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;

public class OptionDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JSpinner spinnerShadowgroup;
    private JSpinner spinnerLODFactor;
    //private JSpinner spinnerLODFactorShadow;
    private JSpinner spinnerTreeDistance;
    private JSpinner spinnerTreeShadowDistance;
    private JCheckBox volumetricSkyCheckBox;
    private JCheckBox displayFPSCheckBox;
    private JSpinner spinnerMaxI;
    private JButton buttonNew;
    private JButton lowButton;
    private JButton mediumButton;
    private JButton highButton;
    private JButton ultraButton;
    private JButton lowestButton;
    private JButton extremeButton;
    private JSpinner spinnerExposure;
    SoQtWalkViewer viewer;
    SceneGraphIndexedFaceSetShader sg;

    /**
     * low by default
     */
    public static final double DEFAULT_SHADOW_PRECISION = 0.05;//0.075;
    public static final double DEFAULT_LOD_FACTOR = 0.5;
    public static final double DEFAULT_LOD_FACTOR_SHADOW = 0.5;
    public static final double DEFAULT_TREE_DISTANCE = 2500;//3000;
    public static final double DEFAULT_TREE_SHADOW_DISTANCE = 500;//1500;
    public static final int DEFAULT_ISLAND_DEPTH = 5612;
    public static final boolean DEFAULT_VOLUMETRIC_SKY = false;
    public static final double DEFAULT_OVERALL_CONTRAST = 4;//1.6;

    public OptionDialog(SoQtWalkViewer viewer, SceneGraphIndexedFaceSetShader sg) {
        setTitle("Game options");
        setContentPane(contentPane);
        //setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        this.viewer = viewer;
        this.sg = sg;

        buttonNew.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onLeaveToNew();
            }
        });

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

        lowestButton.addActionListener((e)->onLowest());
        lowButton.addActionListener((e)->onLow());
        mediumButton.addActionListener((e)->onMedium());
        highButton.addActionListener((e)->onHigh());
        ultraButton.addActionListener((e)->onUltra());
        extremeButton.addActionListener((e)->onExtreme());

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onOK();
            }
        });

        // call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    }

    private void onOK() {
        // add your code here
        //dispose();
        setAlwaysOnTop(false);
        setVisible(false);
        apply();
        if(viewer.isTimeStop()) {
            viewer.toggleTimeStop();
        }
        SwingUtilities.invokeLater(()->{
            viewer.addOneShotIdleListener((viewer2)->{
                SwingUtilities.invokeLater(()->{
                viewer.setVisible(true);
                viewer.setFocus();
                });
            });
        });
    }

    private void onCancel() {
        // add your code here if necessary
        setVisible(false);//dispose(); //dispose() triggers a crash on linux, at least on Ubuntu
        apply();
        if(viewer.isTimeStop()) {
            viewer.toggleTimeStop();
        }
        viewer.onClose(false);
        long window = viewer.getGLWidget().getWindow();
        if (window != 0)
            glfwSetWindowShouldClose(window, true);
    }

    private void onLeaveToNew() {
        // add your code here
        //dispose();
        setVisible(false);
        apply();

        SwingUtilities.invokeLater(()->{
            sg.newGame(viewer);

//        viewer.onClose(true);
//        glfwSetWindowShouldClose(viewer.getGLWidget().getWindow(), true);
            viewer.setVisible(true);
            viewer.setFocus();
        });
    }

    private void onLowest() {
        setShadowPrecision(0.01);
        setLODFactor(0.1);
        setLODFactorShadow(0.1);
        setTreeDistance(500);
        setTreeShadowDistance(500);
        setIslandDepth(5612);
        setVolumetricSky(false);
    }

    private void onLow() {
        setShadowPrecision(0.05);
        setLODFactor(0.5);
        setLODFactorShadow(0.5);
        setTreeDistance(2500);
        setTreeShadowDistance(500);
        setIslandDepth(5612);
        setVolumetricSky(false);
    }

    private void onMedium() {
        setShadowPrecision(0.075/*DEFAULT_SHADOW_PRECISION*/);
        setLODFactor(1.0/*DEFAULT_LOD_FACTOR*/);
        setLODFactorShadow(1.0/*DEFAULT_LOD_FACTOR_SHADOW*/);
        setTreeDistance(6000/*DEFAULT_TREE_DISTANCE*/);
        setTreeShadowDistance(1500/*DEFAULT_TREE_SHADOW_DISTANCE*/);
        setIslandDepth(5612/*DEFAULT_ISLAND_DEPTH*/);
        setVolumetricSky(false/*DEFAULT_VOLUMETRIC_SKY*/);
    }

    private void onHigh() {
        setShadowPrecision(0.2);
        setLODFactor(2.0);
        setLODFactorShadow(2.0);
        setTreeDistance(6000);
        setTreeShadowDistance(3000);
        setIslandDepth(14000);
        setVolumetricSky(true);
    }

    private void onUltra() {
        setShadowPrecision(0.2);
        setLODFactor(2.0);
        setLODFactorShadow(2.0);
        setTreeDistance(9000);
        setTreeShadowDistance(3000);
        setIslandDepth(14000);
        setVolumetricSky(true);
    }

    private void onExtreme() {
        setShadowPrecision(0.2);
        setLODFactor(3.0);
        setLODFactorShadow(3.0);
        setTreeDistance(15000);
        setTreeShadowDistance(6000);
        setIslandDepth(14000);
        setVolumetricSky(true);
    }

    private void apply() {
        //sg.enableNotifySun();
        float shadowPrecision = (float)((double)((Double)((SpinnerNumberModel) spinnerShadowgroup.getModel()).getNumber()));
        sg.getShadowGroup().precision.setValue(shadowPrecision);
        sg.getShadowGroup().epsilon.setValue(1.0e-5f / shadowPrecision);
        sg.setSoftShadows(shadowPrecision > 0.05f);
        sg.setLevelOfDetail((float)((double)((Double)((SpinnerNumberModel)spinnerLODFactor.getModel()).getNumber())));
        //sg.setLevelOfDetailShadow((float)((double)((Double)((SpinnerNumberModel)spinnerLODFactorShadow.getModel()).getNumber())));
        sg.setTreeDistance((float)((double)((Double)((SpinnerNumberModel)spinnerTreeDistance.getModel()).getNumber())));
        sg.setTreeShadowDistance((float)((double)((Double)((SpinnerNumberModel)spinnerTreeShadowDistance.getModel()).getNumber())));
        sg.setMaxI(((int)((SpinnerNumberModel)spinnerMaxI.getModel()).getNumber()));
        sg.setOverallContrast((float)((double)Math.pow(2.0,(Double)((SpinnerNumberModel) spinnerExposure.getModel()).getNumber())));
        boolean volumetric = volumetricSkyCheckBox.getModel().isSelected();
        sg.getShadowGroup().isVolumetricActive.setValue(volumetric);
        sg.getEnvironment().fogColor.setValue(volumetric ? new SbColor(sg.SKY_COLOR.darker().darker().darker().darker().darker().darker().operator_mul(sg.getOverallContrast())) : new SbColor(sg.SKY_COLOR.darker().operator_mul(sg.getOverallContrast())));
        sg.enableFPS(displayFPSCheckBox.getModel().isSelected());
        //sg.disableNotifySun();
    }

    public void setVisible(boolean b) {
        if(b) {
            final double delta = 1e-5;
            // SHADOW_PRECISION
            spinnerShadowgroup.setModel(new SpinnerNumberModel((double)sg.getShadowGroup().precision.getValue(),0.01 - delta,0.4 + delta,0.01));
            // LOD_FACTOR
            spinnerLODFactor.setModel(new SpinnerNumberModel((double)sg.getLevelOfDetail(),0.05 - delta,3.0 + delta,0.05));
            // LOD_FACTOR_SHADOW
            //spinnerLODFactorShadow.setModel(new SpinnerNumberModel((double)sg.getLevelOfDetailShadow(),0.05 - delta,2.0 + delta,0.05));
            // TREE_DISTANCE
            double treeDistance = (double)sg.getTreeDistance();
            treeDistance = Math.max(500,treeDistance);
            treeDistance = Math.min(30000,treeDistance);
            spinnerTreeDistance.setModel(new SpinnerNumberModel(treeDistance,500 - delta,30000 + delta,500));
            // TREE_SHADOW_DISTANCE
            double treeShadowDistance = (double)sg.getTreeShadowDistance();
            treeShadowDistance = Math.max(500,treeShadowDistance);
            treeShadowDistance = Math.min(30000,treeShadowDistance);
            spinnerTreeShadowDistance.setModel(new SpinnerNumberModel(treeShadowDistance,500 - delta,30000 + delta,500));
            // MAX_I
            spinnerMaxI.setModel(new SpinnerNumberModel(sg.getMaxI(),5612,14000,500));
            spinnerExposure.setModel(new SpinnerNumberModel((double) Math.round(10.0*Math.log10(sg.getOverallContrast())/Math.log10(2))/10.0,-5.01,5.01,0.2));
            // VOLUMETRIC_SKY
            volumetricSkyCheckBox.getModel().setSelected(sg.getShadowGroup().isVolumetricActive.getValue());
            // DISPLAY_FPS
            displayFPSCheckBox.getModel().setSelected(sg.isFPSEnabled());
        }
        super.setVisible(b);
    }

    public void setShadowPrecision(double precision) {
        spinnerShadowgroup.getModel().setValue(precision);
    }

    public void setLODFactor(double lodFactor) {
        spinnerLODFactor.getModel().setValue(lodFactor);
    }

    public void setLODFactorShadow(double lodFactorShadow) {
        //spinnerLODFactorShadow.getModel().setValue(lodFactorShadow);
    }

    public void setTreeDistance(double treeDistance) {
        spinnerTreeDistance.getModel().setValue(treeDistance);
    }

    public void setTreeShadowDistance(double treeShadowDistance) {
        spinnerTreeShadowDistance.getModel().setValue(treeShadowDistance);
    }

    public void setIslandDepth(int islandDepth) {
        spinnerMaxI.getModel().setValue(islandDepth);
    }

    public void setOveralContrast(double contrast) {
        spinnerExposure.getModel().setValue(Math.log10(contrast)/Math.log10(2.0));
    }
    public void setVolumetricSky(boolean volumetricSky) {
        volumetricSkyCheckBox.getModel().setSelected(volumetricSky);
    }

    public static void main(String[] args) {
        OptionDialog dialog = new OptionDialog(null,null);
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
