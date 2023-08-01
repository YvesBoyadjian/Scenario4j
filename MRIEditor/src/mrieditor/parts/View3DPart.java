/**
 * 
 */
package mrieditor.parts;

import java.util.Arrays;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.widgets.TextFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import application.RasterProvider;
import application.swt.SoQtWalkViewer;
import application.terrain.IslandLoader;
import jscenegraph.database.inventor.SbTime;
import jscenegraph.database.inventor.SoDB;
import jsceneviewer.inventor.qt.SoQt;
import jsceneviewer.inventor.qt.SoQtCameraController.Type;
import jsceneviewer.inventor.qt.viewers.SoQtFullViewer.BuildFlag;

/**
 * 
 */
public class View3DPart {

	private TableViewer tableViewer;
	
	private SoQtWalkViewer walkViewer;

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
		walkViewer = new SoQtWalkViewer(BuildFlag.BUILD_ALL,Type.BROWSER,intermediate,style);
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
		
		System.out.println("Load 3D Model");		
		
	}
}
