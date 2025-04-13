package mrieditor.parts;

import java.util.Arrays;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.eclipse.e4.core.services.events.IEventBroker;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.di.Persist;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.widgets.TextFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import retroboss.game.MRIGame;
import retroboss.game.bigfoothunting.BigfootHuntingGame;
import retroboss.game.hikingisland.HikingIslandGame;
import retroboss.game.retroboss.RetroBossGame;

public class SamplePart {
	
	private static final String GAME_SELECTION ="gameSelection";

	private TableViewer tableViewer;

	@Inject
	private MPart part;

	@Inject
	private IEventBroker eventBroker;	
	
	@PostConstruct
	public void createComposite(Composite parent) {
		parent.setLayout(new GridLayout(1, false));

		TextFactory.newText(SWT.BORDER) //
				.message("Enter text to mark part as dirty") //
				.onModify(e -> part.setDirty(true)) //
				.layoutData(new GridData(GridData.FILL_HORIZONTAL))//
				.create(parent);

		tableViewer = new TableViewer(parent);

		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setInput(createInitialDataModel());
		tableViewer.getTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				IStructuredSelection ss = event.getStructuredSelection();
				if (!ss.isEmpty()) {
					eventBroker.send(GAME_SELECTION, ss.getFirstElement());		
				}
			}
			
		});

	}

	@Focus
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}

	@Persist
	public void save() {
		part.setDirty(false);
	}

	private List<MRIGame> createInitialDataModel() {
		return Arrays.asList(new BigfootHuntingGame(), new RetroBossGame(), new HikingIslandGame());
	}
}