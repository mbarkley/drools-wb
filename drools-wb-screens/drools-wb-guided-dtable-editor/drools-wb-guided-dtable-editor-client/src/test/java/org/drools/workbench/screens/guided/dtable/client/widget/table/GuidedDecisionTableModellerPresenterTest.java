/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.workbench.screens.guided.dtable.client.widget.table;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.event.Event;

import com.ait.lienzo.client.core.event.NodeMouseMoveHandler;
import com.ait.lienzo.client.core.event.NodeMouseOutHandler;
import com.ait.lienzo.client.core.types.Point2D;
import com.ait.lienzo.test.LienzoMockitoTestRunner;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.MouseDownHandler;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import org.drools.workbench.models.datamodel.workitems.PortableWorkDefinition;
import org.drools.workbench.models.guided.dtable.shared.model.ActionCol52;
import org.drools.workbench.models.guided.dtable.shared.model.AttributeCol52;
import org.drools.workbench.models.guided.dtable.shared.model.BaseColumn;
import org.drools.workbench.models.guided.dtable.shared.model.CompositeColumn;
import org.drools.workbench.models.guided.dtable.shared.model.GuidedDecisionTable52;
import org.drools.workbench.models.guided.dtable.shared.model.MetadataCol52;
import org.drools.workbench.screens.guided.dtable.client.editor.menu.RadarMenuBuilder;
import org.drools.workbench.screens.guided.dtable.client.widget.table.events.cdi.DecisionTableColumnSelectedEvent;
import org.drools.workbench.screens.guided.dtable.client.widget.table.events.cdi.DecisionTablePinnedEvent;
import org.drools.workbench.screens.guided.dtable.client.widget.table.events.cdi.DecisionTableSelectedEvent;
import org.drools.workbench.screens.guided.dtable.client.widget.table.events.cdi.RefreshActionsPanelEvent;
import org.drools.workbench.screens.guided.dtable.client.widget.table.events.cdi.RefreshAttributesPanelEvent;
import org.drools.workbench.screens.guided.dtable.client.widget.table.events.cdi.RefreshConditionsPanelEvent;
import org.drools.workbench.screens.guided.dtable.client.widget.table.events.cdi.RefreshMetaDataPanelEvent;
import org.drools.workbench.screens.guided.dtable.client.widget.table.popovers.ColumnHeaderPopOver;
import org.drools.workbench.screens.guided.dtable.client.wizard.column.NewGuidedDecisionTableColumnWizard;
import org.drools.workbench.screens.guided.dtable.model.GuidedDecisionTableEditorContent;
import org.guvnor.common.services.shared.metadata.model.Overview;
import org.jboss.errai.ioc.client.api.ManagedInstance;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kie.soup.project.datamodel.model.PackageDataModelOracleBaselinePayload;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.uberfire.backend.vfs.ObservablePath;
import org.uberfire.ext.wires.core.grids.client.model.Bounds;
import org.uberfire.ext.wires.core.grids.client.model.GridColumn;
import org.uberfire.ext.wires.core.grids.client.model.GridData;
import org.uberfire.ext.wires.core.grids.client.model.impl.BaseBounds;
import org.uberfire.ext.wires.core.grids.client.model.impl.BaseGridColumn;
import org.uberfire.ext.wires.core.grids.client.model.impl.BaseGridData;
import org.uberfire.ext.wires.core.grids.client.widget.grid.renderers.columns.GridColumnRenderer;
import org.uberfire.ext.wires.core.grids.client.widget.layer.GridLayer;
import org.uberfire.mocks.EventSourceMock;
import org.uberfire.mvp.ParameterizedCommand;
import org.uberfire.mvp.PlaceRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(LienzoMockitoTestRunner.class)
public class GuidedDecisionTableModellerPresenterTest {

    @Mock
    private GuidedDecisionTableModellerView view;

    @Mock
    private ManagedInstance<NewGuidedDecisionTableColumnWizard> wizardManagedInstance;

    @Mock
    private GridLayer gridLayer;

    private Event<RadarMenuBuilder.UpdateRadarEvent> updateRadarEvent = spy(new EventSourceMock<RadarMenuBuilder.UpdateRadarEvent>() {
        @Override
        public void fire(final RadarMenuBuilder.UpdateRadarEvent event) {
            //Do nothing. Default implementation throws an UnsupportedOperationException
        }
    });

    private Event<DecisionTablePinnedEvent> pinnedEvent = spy(new EventSourceMock<DecisionTablePinnedEvent>() {
        @Override
        public void fire(final DecisionTablePinnedEvent event) {
            //Do nothing. Default implementation throws an UnsupportedOperationException
        }
    });

    private Bounds bounds = new BaseBounds(-1000,
                                           -1000,
                                           2000,
                                           2000);

    @Mock
    private ManagedInstance<GuidedDecisionTableView.Presenter> dtablePresenterProvider;

    @Mock
    private GuidedDecisionTableView.Presenter dtablePresenter;

    @Mock
    private GuidedDecisionTable52 model;

    @Mock
    private GuidedDecisionTableView dtableView;

    @Mock
    private GuidedDecisionTableModellerContextMenuSupport contextMenuSupport;

    @Mock
    private ColumnHeaderPopOver columnHeaderPopOver;

    private GuidedDecisionTableModellerPresenter presenter;

    @Before
    public void setup() {
        when(gridLayer.addNodeMouseMoveHandler(any(NodeMouseMoveHandler.class))).thenReturn(mock(HandlerRegistration.class));
        when(gridLayer.addNodeMouseOutHandler(any(NodeMouseOutHandler.class))).thenReturn(mock(HandlerRegistration.class));
        when(view.addKeyDownHandler(any(KeyDownHandler.class))).thenReturn(mock(HandlerRegistration.class));
        when(view.addContextMenuHandler(any(ContextMenuHandler.class))).thenReturn(mock(HandlerRegistration.class));
        when(view.addMouseDownHandler(any(MouseDownHandler.class))).thenReturn(mock(HandlerRegistration.class));
        when(view.getGridLayerView()).thenReturn(gridLayer);
        when(view.getBounds()).thenReturn(bounds);

        final GuidedDecisionTableModellerPresenter wrapped = new GuidedDecisionTableModellerPresenter(view,
                                                                                                      dtablePresenterProvider,
                                                                                                      contextMenuSupport,
                                                                                                      updateRadarEvent,
                                                                                                      pinnedEvent,
                                                                                                      columnHeaderPopOver,
                                                                                                      wizardManagedInstance);
        presenter = spy(wrapped);

        when(dtablePresenterProvider.get()).thenReturn(dtablePresenter);
        when(dtablePresenter.getView()).thenReturn(dtableView);
        when(dtableView.getModel()).thenReturn(new BaseGridData());
    }

    private GuidedDecisionTableView.Presenter makeDecisionTable() {
        final GuidedDecisionTableView.Presenter dtPresenter = mock(GuidedDecisionTableView.Presenter.class);
        final GuidedDecisionTableView dtView = mock(GuidedDecisionTableView.class);
        final GridData dtData = new BaseGridData();

        when(dtPresenter.getView()).thenReturn(dtView);
        when(dtPresenter.getAccess()).thenReturn(mock(GuidedDecisionTablePresenter.Access.class));
        when(dtPresenter.getModel()).thenReturn(mock(GuidedDecisionTable52.class));
        when(dtView.getModel()).thenReturn(dtData);

        return dtPresenter;
    }

    private GuidedDecisionTableEditorContent makeDecisionTableContent() {
        final GuidedDecisionTable52 model = mock(GuidedDecisionTable52.class);
        final PackageDataModelOracleBaselinePayload dmoBaseline = mock(PackageDataModelOracleBaselinePayload.class);
        final Set<PortableWorkDefinition> workItemDefinitions = Collections.emptySet();
        final Overview overview = mock(Overview.class);

        final GuidedDecisionTableEditorContent dtContent = new GuidedDecisionTableEditorContent(model,
                                                                                                workItemDefinitions,
                                                                                                overview,
                                                                                                dmoBaseline);
        return dtContent;
    }

    @SuppressWarnings("unchecked")
    private GridColumn makeUiColumn() {
        return spy(new BaseGridColumn(mock(GridColumn.HeaderMetaData.class),
                                      mock(GridColumnRenderer.class),
                                      100.0));
    }

    @Test
    public void onClose() {
        presenter.onClose();

        verify(view,
               times(1)).clear();
        verify(presenter,
               times(1)).releaseDecisionTables();
        verify(presenter,
               times(1)).releaseHandlerRegistrations();
    }

    @Test
    public void addDecisionTable() {
        final ObservablePath path = mock(ObservablePath.class);
        final PlaceRequest placeRequest = mock(PlaceRequest.class);
        final GuidedDecisionTableEditorContent dtContent = makeDecisionTableContent();

        presenter.addDecisionTable(path,
                                   placeRequest,
                                   dtContent,
                                   false,
                                   null,
                                   null);

        verify(presenter,
               times(1)).updateLinks();
        verify(gridLayer,
               times(1)).refreshGridWidgetConnectors();
        verify(view,
               times(1)).addDecisionTable(eq(dtableView));
    }

    @Test
    public void refreshDecisionTable() {
        final GuidedDecisionTableView.Presenter dtPresenter = makeDecisionTable();
        final GuidedDecisionTableEditorContent dtContent = makeDecisionTableContent();
        final GuidedDecisionTableView dtView = dtPresenter.getView();
        final ObservablePath path = mock(ObservablePath.class);
        final PlaceRequest placeRequest = mock(PlaceRequest.class);

        final ArgumentCaptor<Command> afterRemovalCommandCaptor = ArgumentCaptor.forClass(Command.class);

        presenter.refreshDecisionTable(dtPresenter,
                                       path,
                                       placeRequest,
                                       dtContent,
                                       false);

        verify(view,
               times(1)).removeDecisionTable(eq(dtView),
                                             afterRemovalCommandCaptor.capture());
        final Command afterRemovalCommand = afterRemovalCommandCaptor.getValue();
        assertNotNull(afterRemovalCommand);
        afterRemovalCommand.execute();

        verify(view,
               times(1)).addDecisionTable(eq(dtView));
    }

    @Test
    public void refreshingDecisionTableRetainsExistingLocation() {
        final GuidedDecisionTableView.Presenter dtPresenter = makeDecisionTable();
        final GuidedDecisionTableEditorContent dtContent = makeDecisionTableContent();
        final GuidedDecisionTableView dtView = dtPresenter.getView();
        final ObservablePath path = mock(ObservablePath.class);
        final PlaceRequest placeRequest = mock(PlaceRequest.class);

        final Point2D dtLocation = new Point2D(100,
                                               100);

        when(dtView.getLocation()).thenReturn(dtLocation);

        final ArgumentCaptor<Command> afterRemovalCommandCaptor = ArgumentCaptor.forClass(Command.class);

        presenter.refreshDecisionTable(dtPresenter,
                                       path,
                                       placeRequest,
                                       dtContent,
                                       false);

        verify(dtView,
               times(1)).getLocation();
        verify(view,
               times(1)).removeDecisionTable(eq(dtView),
                                             afterRemovalCommandCaptor.capture());
        final Command afterRemovalCommand = afterRemovalCommandCaptor.getValue();
        assertNotNull(afterRemovalCommand);
        afterRemovalCommand.execute();

        verify(dtView,
               times(1)).setLocation(eq(dtLocation));
    }

    @Test
    public void removeDecisionTable() {
        final GuidedDecisionTableView.Presenter dtPresenter = makeDecisionTable();

        final ArgumentCaptor<Command> afterRemovalCommandCaptor = ArgumentCaptor.forClass(Command.class);

        presenter.removeDecisionTable(dtPresenter);

        verify(view,
               times(1)).removeDecisionTable(eq(dtPresenter.getView()),
                                             afterRemovalCommandCaptor.capture());
        final Command afterRemovalCommand = afterRemovalCommandCaptor.getValue();
        assertNotNull(afterRemovalCommand);
        afterRemovalCommand.execute();

        verify(view,
               times(1)).setEnableColumnCreation(eq(false));
        verify(view,
               times(1)).refreshAttributeWidget(eq(Collections.emptyList()));
        verify(view,
               times(1)).refreshMetaDataWidget(eq(Collections.emptyList()));
        verify(view,
               times(1)).refreshConditionsWidget(eq(Collections.emptyList()));
        verify(view,
               times(1)).refreshActionsWidget(eq(Collections.emptyList()));
        verify(view,
               times(1)).refreshColumnsNote(eq(false));
        verify(dtPresenter,
               times(1)).onClose();
        verify(presenter,
               times(1)).removeLinksForDecisionTable(eq(dtPresenter));
    }

    @Test
    public void onLockStatusUpdatedWithNullDecisionTable() {
        final GuidedDecisionTableView.Presenter dtPresenter = makeDecisionTable();

        when(presenter.getActiveDecisionTable()).thenReturn(dtPresenter);

        presenter.onLockStatusUpdated(null);

        verify(presenter,
               never()).refreshDefinitionsPanel(any(GuidedDecisionTableView.Presenter.class));
    }

    @Test
    public void onLockStatusUpdatedWithActiveDecisionTable() {
        final GuidedDecisionTableView.Presenter dtPresenter = makeDecisionTable();

        when(presenter.getActiveDecisionTable()).thenReturn(dtPresenter);

        presenter.onLockStatusUpdated(dtPresenter);

        verify(presenter,
               times(1)).refreshDefinitionsPanel(eq(dtPresenter));
    }

    @Test
    public void onLockStatusUpdatedWithAnotherActiveDecisionTable() {
        final GuidedDecisionTableView.Presenter dtPresenter = makeDecisionTable();

        when(presenter.getActiveDecisionTable()).thenReturn(mock(GuidedDecisionTableView.Presenter.class));

        presenter.onLockStatusUpdated(dtPresenter);

        verify(presenter,
               never()).refreshDefinitionsPanel(eq(dtPresenter));
    }

    @Test
    public void isActiveDecisionTableEditableIsReadOnly() {
        final GuidedDecisionTableView.Presenter dtPresenter = makeDecisionTable();
        final GuidedDecisionTablePresenter.Access access = new GuidedDecisionTablePresenter.Access();
        access.setReadOnly(true);

        when(presenter.getActiveDecisionTable()).thenReturn(dtPresenter);
        when(dtPresenter.getAccess()).thenReturn(access);

        assertFalse(presenter.isActiveDecisionTableEditable());
    }

    @Test
    public void isActiveDecisionTableEditableNotReadOnly() {
        final GuidedDecisionTableView.Presenter dtPresenter = makeDecisionTable();
        final GuidedDecisionTablePresenter.Access access = new GuidedDecisionTablePresenter.Access();
        access.setReadOnly(false);

        when(presenter.getActiveDecisionTable()).thenReturn(dtPresenter);
        when(dtPresenter.getAccess()).thenReturn(access);

        assertTrue(presenter.isActiveDecisionTableEditable());
    }

    @Test
    public void isActiveDecisionTableEditableLockedByCurrentUser() {
        final GuidedDecisionTableView.Presenter dtPresenter = makeDecisionTable();
        final GuidedDecisionTablePresenter.Access access = new GuidedDecisionTablePresenter.Access();
        access.setLock(GuidedDecisionTablePresenter.Access.LockedBy.CURRENT_USER);

        when(presenter.getActiveDecisionTable()).thenReturn(dtPresenter);
        when(dtPresenter.getAccess()).thenReturn(access);

        assertTrue(presenter.isActiveDecisionTableEditable());
    }

    @Test
    public void isActiveDecisionTableEditableLockedByOtherUser() {
        final GuidedDecisionTableView.Presenter dtPresenter = makeDecisionTable();
        final GuidedDecisionTablePresenter.Access access = new GuidedDecisionTablePresenter.Access();
        access.setLock(GuidedDecisionTablePresenter.Access.LockedBy.OTHER_USER);

        when(presenter.getActiveDecisionTable()).thenReturn(dtPresenter);
        when(dtPresenter.getAccess()).thenReturn(access);

        assertFalse(presenter.isActiveDecisionTableEditable());
    }

    @Test
    public void onInsertColumnWithNullActiveDecisionTable() {
        presenter.onInsertColumn();

        verify(view,
               never()).onInsertColumn();
    }

    @Test
    public void onInsertColumnWithNonNullActiveDecisionTable() {
        final GuidedDecisionTableView.Presenter dtPresenter = makeDecisionTable();

        when(presenter.getActiveDecisionTable()).thenReturn(dtPresenter);

        presenter.onInsertColumn();

        verify(view,
               times(1)).onInsertColumn();
    }

    @Test
    public void setZoom() {
        presenter.setZoom(100);

        verify(view,
               times(1)).setZoom(eq(100));
    }

    @Test
    public void enterPinnedMode() {
        final GuidedDecisionTableView.Presenter dtPresenter = makeDecisionTable();
        final Command command = mock(Command.class);
        presenter.enterPinnedMode(dtPresenter.getView(),
                                  command);

        verify(gridLayer,
               times(1)).enterPinnedMode(eq(dtPresenter.getView()),
                                         eq(command));
        verify(columnHeaderPopOver,
               times(1)).hide();
    }

    @Test
    public void exitPinnedMode() {
        final Command command = mock(Command.class);
        presenter.exitPinnedMode(command);

        verify(gridLayer,
               times(1)).exitPinnedMode(eq(command));
        verify(columnHeaderPopOver,
               times(1)).hide();
    }

    @Test
    public void updatePinnedContext() {
        final GuidedDecisionTableView.Presenter dtPresenter = makeDecisionTable();

        presenter.updatePinnedContext(dtPresenter.getView());

        verify(gridLayer,
               times(1)).updatePinnedContext(eq(dtPresenter.getView()));
        verify(columnHeaderPopOver,
               times(1)).hide();
    }

    @Test
    public void getPinnedContext() {
        presenter.getPinnedContext();

        verify(gridLayer,
               times(1)).getPinnedContext();
    }

    @Test
    public void isGridPinned() {
        presenter.isGridPinned();

        verify(gridLayer,
               times(1)).isGridPinned();
    }

    @Test
    public void getDefaultTransformMediator() {
        presenter.getDefaultTransformMediator();

        verify(gridLayer,
               times(1)).getDefaultTransformMediator();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onDecisionTableSelectedWhenInPinnedMode() {
        final GuidedDecisionTableView.Presenter dtPresenter1 = makeDecisionTable();
        final GuidedDecisionTableView.Presenter dtPresenter2 = makeDecisionTable();
        final DecisionTableSelectedEvent event = new DecisionTableSelectedEvent(dtPresenter1);
        final List<String> parentRuleNames = Collections.emptyList();

        when(gridLayer.isGridPinned()).thenReturn(true);

        when(presenter.isDecisionTableAvailable(eq(dtPresenter1))).thenReturn(true);
        when(presenter.getAvailableDecisionTables()).thenReturn(new HashSet<GuidedDecisionTableView.Presenter>() {{
            add(dtPresenter1);
            add(dtPresenter2);
        }});

        final ArgumentCaptor<ParameterizedCommand> parentRuleNamesCommandCaptor = ArgumentCaptor.forClass(ParameterizedCommand.class);

        presenter.onDecisionTableSelected(event);

        verify(dtPresenter1,
               times(1)).initialiseAnalysis();
        verify(presenter,
               times(1)).refreshDefinitionsPanel(eq(dtPresenter1));
        verify(view,
               times(1)).select(dtPresenter1.getView());

        verify(dtPresenter1,
               times(1)).getPackageParentRuleNames(parentRuleNamesCommandCaptor.capture());
        final ParameterizedCommand parentRuleNamesCommand = parentRuleNamesCommandCaptor.getValue();
        assertNotNull(parentRuleNamesCommand);
        parentRuleNamesCommand.execute(parentRuleNames);

        verify(view,
               times(1)).refreshRuleInheritance(any(String.class),
                                                eq(parentRuleNames));
        verify(gridLayer,
               times(1)).flipToGridWidget(eq(dtPresenter1.getView()));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onDecisionTableSelectedWhenNotInPinnedMode() {
        final GuidedDecisionTableView.Presenter dtPresenter1 = makeDecisionTable();
        final GuidedDecisionTableView.Presenter dtPresenter2 = makeDecisionTable();
        final DecisionTableSelectedEvent event = new DecisionTableSelectedEvent(dtPresenter1);
        final List<String> parentRuleNames = Collections.emptyList();

        when(presenter.isDecisionTableAvailable(eq(dtPresenter1))).thenReturn(true);
        when(presenter.getAvailableDecisionTables()).thenReturn(new HashSet<GuidedDecisionTableView.Presenter>() {{
            add(dtPresenter1);
            add(dtPresenter2);
        }});

        final ArgumentCaptor<ParameterizedCommand> parentRuleNamesCommandCaptor = ArgumentCaptor.forClass(ParameterizedCommand.class);

        presenter.onDecisionTableSelected(event);

        verify(dtPresenter1,
               times(1)).initialiseAnalysis();
        verify(presenter,
               times(1)).refreshDefinitionsPanel(eq(dtPresenter1));
        verify(view,
               times(1)).select(dtPresenter1.getView());

        verify(dtPresenter1,
               times(1)).getPackageParentRuleNames(parentRuleNamesCommandCaptor.capture());
        final ParameterizedCommand parentRuleNamesCommand = parentRuleNamesCommandCaptor.getValue();
        assertNotNull(parentRuleNamesCommand);
        parentRuleNamesCommand.execute(parentRuleNames);

        verify(view,
               times(1)).refreshRuleInheritance(any(String.class),
                                                eq(parentRuleNames));
        verify(gridLayer,
               never()).flipToGridWidget(any(GuidedDecisionTableView.class));
    }

    @Test
    public void onDecisionTableSelectedNoneSelected() {
        final DecisionTableSelectedEvent event = DecisionTableSelectedEvent.NONE;

        presenter.onDecisionTableSelected(event);

        assertNull(presenter.getActiveDecisionTable());

        verify(presenter,
               never()).doDecisionTableSelected(any(GuidedDecisionTableView.Presenter.class));
    }

    @Test
    public void onDecisionTableSelectedNotAvailable() {
        final GuidedDecisionTableView.Presenter dtPresenter1 = makeDecisionTable();
        final GuidedDecisionTableView.Presenter dtPresenter2 = makeDecisionTable();
        final DecisionTableSelectedEvent event = new DecisionTableSelectedEvent(dtPresenter1);

        when(presenter.getAvailableDecisionTables()).thenReturn(Collections.singleton(dtPresenter2));

        presenter.onDecisionTableSelected(event);

        assertNull(presenter.getActiveDecisionTable());

        verify(presenter,
               never()).doDecisionTableSelected(any(GuidedDecisionTableView.Presenter.class));
    }

    @Test
    public void onDecisionTableLinkedColumnSelected() {
        final GridColumn gridColumn = mock(GridColumn.class);
        final DecisionTableColumnSelectedEvent event = new DecisionTableColumnSelectedEvent(gridColumn);

        presenter.onDecisionTableLinkedColumnSelected(event);

        verify(view,
               times(1)).selectLinkedColumn(eq(gridColumn));
    }

    @Test
    public void onRefreshAttributesPanelEventWithDecisionTableAvailable() {
        final GuidedDecisionTableView.Presenter dtPresenter = makeDecisionTable();
        final List<AttributeCol52> columns = Collections.emptyList();
        final RefreshAttributesPanelEvent event = new RefreshAttributesPanelEvent(dtPresenter,
                                                                                  columns);

        when(presenter.isDecisionTableAvailable(dtPresenter)).thenReturn(true);

        presenter.onRefreshAttributesPanelEvent(event);

        verify(view,
               times(1)).refreshAttributeWidget(eq(columns));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onRefreshAttributesPanelEventWithDecisionTableUnavailable() {
        final GuidedDecisionTableView.Presenter dtPresenter = makeDecisionTable();
        final List<AttributeCol52> columns = Collections.emptyList();
        final RefreshAttributesPanelEvent event = new RefreshAttributesPanelEvent(dtPresenter,
                                                                                  columns);

        when(presenter.isDecisionTableAvailable(dtPresenter)).thenReturn(false);

        presenter.onRefreshAttributesPanelEvent(event);

        verify(view,
               never()).refreshAttributeWidget(any(List.class));
    }

    @Test
    public void onRefreshMetaDataPanelEventWithDecisionTableAvailable() {
        final GuidedDecisionTableView.Presenter dtPresenter = makeDecisionTable();
        final List<MetadataCol52> columns = Collections.emptyList();
        final RefreshMetaDataPanelEvent event = new RefreshMetaDataPanelEvent(dtPresenter,
                                                                              columns);

        when(presenter.isDecisionTableAvailable(dtPresenter)).thenReturn(true);

        presenter.onRefreshMetaDataPanelEvent(event);

        verify(view,
               times(1)).refreshMetaDataWidget(eq(columns));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onRefreshMetaDataPanelEventWithDecisionTableUnavailable() {
        final GuidedDecisionTableView.Presenter dtPresenter = makeDecisionTable();
        final List<MetadataCol52> columns = Collections.emptyList();
        final RefreshMetaDataPanelEvent event = new RefreshMetaDataPanelEvent(dtPresenter,
                                                                              columns);

        when(presenter.isDecisionTableAvailable(dtPresenter)).thenReturn(false);

        presenter.onRefreshMetaDataPanelEvent(event);

        verify(view,
               never()).refreshMetaDataWidget(any(List.class));
    }

    @Test
    public void onRefreshConditionsPanelEventWithDecisionTableAvailable() {
        final GuidedDecisionTableView.Presenter dtPresenter = makeDecisionTable();
        final List<CompositeColumn<? extends BaseColumn>> columns = Collections.emptyList();
        final RefreshConditionsPanelEvent event = new RefreshConditionsPanelEvent(dtPresenter,
                                                                                  columns);

        when(presenter.isDecisionTableAvailable(dtPresenter)).thenReturn(true);

        presenter.onRefreshConditionsPanelEvent(event);

        verify(view,
               times(1)).refreshConditionsWidget(eq(columns));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onRefreshConditionsPanelEventWithDecisionTableUnavailable() {
        final GuidedDecisionTableView.Presenter dtPresenter = makeDecisionTable();
        final List<CompositeColumn<? extends BaseColumn>> columns = Collections.emptyList();
        final RefreshConditionsPanelEvent event = new RefreshConditionsPanelEvent(dtPresenter,
                                                                                  columns);

        when(presenter.isDecisionTableAvailable(dtPresenter)).thenReturn(false);

        presenter.onRefreshConditionsPanelEvent(event);

        verify(view,
               never()).refreshConditionsWidget(any(List.class));
    }

    @Test
    public void onRefreshActionsPanelEventWithDecisionTableAvailable() {
        final GuidedDecisionTableView.Presenter dtPresenter = makeDecisionTable();
        final List<ActionCol52> columns = Collections.emptyList();
        final RefreshActionsPanelEvent event = new RefreshActionsPanelEvent(dtPresenter,
                                                                            columns);

        when(presenter.isDecisionTableAvailable(dtPresenter)).thenReturn(true);

        presenter.onRefreshActionsPanelEvent(event);

        verify(view,
               times(1)).refreshActionsWidget(eq(columns));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void onRefreshActionsPanelEventWithDecisionTableUnavailable() {
        final GuidedDecisionTableView.Presenter dtPresenter = makeDecisionTable();
        final List<ActionCol52> columns = Collections.emptyList();
        final RefreshActionsPanelEvent event = new RefreshActionsPanelEvent(dtPresenter,
                                                                            columns);

        when(presenter.isDecisionTableAvailable(dtPresenter)).thenReturn(false);

        presenter.onRefreshActionsPanelEvent(event);

        verify(view,
               never()).refreshActionsWidget(any(List.class));
    }

    @Test
    public void updateRadar() {
        presenter.updateRadar();

        verify(updateRadarEvent,
               times(1)).fire(any(RadarMenuBuilder.UpdateRadarEvent.class));
    }

    @Test
    public void onViewPinnedIsPinned() {
        final ArgumentCaptor<DecisionTablePinnedEvent> pinnedEventCaptor = ArgumentCaptor.forClass(DecisionTablePinnedEvent.class);

        presenter.onViewPinned(true);

        verify(pinnedEvent,
               times(1)).fire(pinnedEventCaptor.capture());
        final DecisionTablePinnedEvent pinnedEvent = pinnedEventCaptor.getValue();
        assertNotNull(pinnedEvent);

        assertEquals(presenter,
                     pinnedEvent.getPresenter());
        assertTrue(pinnedEvent.isPinned());
        verify(view).setPinnedModeIndicatorVisibility(true);
    }

    @Test
    public void onViewPinnedIsNotPinned() {
        final ArgumentCaptor<DecisionTablePinnedEvent> pinnedEventCaptor = ArgumentCaptor.forClass(DecisionTablePinnedEvent.class);

        presenter.onViewPinned(false);

        verify(pinnedEvent,
               times(1)).fire(pinnedEventCaptor.capture());
        final DecisionTablePinnedEvent pinnedEvent = pinnedEventCaptor.getValue();
        assertNotNull(pinnedEvent);

        assertEquals(presenter,
                     pinnedEvent.getPresenter());
        assertFalse(pinnedEvent.isPinned());
        verify(view).setPinnedModeIndicatorVisibility(false);
    }

    @Test
    public void testAddOnEnterPinnedModeCommand() {

        final Command command = mock(Command.class);
        final GridLayer gridLayer = mock(GridLayer.class);

        doReturn(gridLayer).when(view).getGridLayerView();

        presenter.addOnEnterPinnedModeCommand(command);

        verify(gridLayer).addOnEnterPinnedModeCommand(command);
    }

    @Test
    public void testAddOnExitPinnedModeCommand() {

        final Command command = mock(Command.class);
        final GridLayer gridLayer = mock(GridLayer.class);

        doReturn(gridLayer).when(view).getGridLayerView();

        presenter.addOnExitPinnedModeCommand(command);

        verify(gridLayer).addOnExitPinnedModeCommand(command);
    }

    @Test
    public void updateLinks() {
        final GuidedDecisionTableView.Presenter dtPresenter1 = makeDecisionTable();
        final GuidedDecisionTableView.Presenter dtPresenter2 = makeDecisionTable();
        final Set<GuidedDecisionTableView.Presenter> availableDecisionTables = new HashSet<GuidedDecisionTableView.Presenter>() {{
            add(dtPresenter1);
            add(dtPresenter2);
        }};

        when(presenter.isDecisionTableAvailable(eq(dtPresenter1))).thenReturn(true);
        when(presenter.getAvailableDecisionTables()).thenReturn(availableDecisionTables);

        presenter.updateLinks();

        verify(dtPresenter1,
               times(1)).link(eq(availableDecisionTables));
        verify(dtPresenter2,
               times(1)).link(eq(availableDecisionTables));
        verify(gridLayer,
               times(1)).refreshGridWidgetConnectors();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkRemoveLinksPreservesOtherTables() {
        final GuidedDecisionTableView.Presenter dtPresenter1 = makeDecisionTable();
        final GuidedDecisionTableView.Presenter dtPresenter2 = makeDecisionTable();
        final Set<GuidedDecisionTableView.Presenter> availableDecisionTables = new HashSet<GuidedDecisionTableView.Presenter>() {{
            add(dtPresenter2);
        }};

        final GridColumn dtPresenter1Column1 = makeUiColumn();
        final GridColumn dtPresenter1Column2 = makeUiColumn();
        final GridColumn dtPresenter2Column1 = makeUiColumn();
        final GridColumn dtPresenter2Column2 = makeUiColumn();
        dtPresenter1.getView().getModel().appendColumn(dtPresenter1Column1);
        dtPresenter1.getView().getModel().appendColumn(dtPresenter1Column2);
        dtPresenter2.getView().getModel().appendColumn(dtPresenter2Column1);
        dtPresenter2.getView().getModel().appendColumn(dtPresenter2Column2);

        dtPresenter1Column2.setLink(dtPresenter2Column2);
        dtPresenter2Column1.setLink(dtPresenter1Column1);

        when(presenter.getAvailableDecisionTables()).thenReturn(availableDecisionTables);

        //Check setup
        verify(dtPresenter1Column2,
               times(1)).setLink(eq(dtPresenter2Column2));
        verify(dtPresenter2Column1,
               times(1)).setLink(eq(dtPresenter1Column1));

        reset(dtPresenter1Column1,
              dtPresenter1Column2,
              dtPresenter2Column1,
              dtPresenter2Column2);

        //Check links after removal
        presenter.removeLinksForDecisionTable(dtPresenter1);

        verify(dtPresenter1Column1,
               never()).setLink(any());
        verify(dtPresenter1Column2,
               never()).setLink(any());

        verify(dtPresenter2Column1,
               times(1)).setLink(eq(null));
        verify(dtPresenter2Column2,
               never()).setLink(any());
    }

    @Test
    public void testHandlePermissionsWhenGuidedDecisionTableHasEditableColumns() {

        doReturn(true).when(dtablePresenter).hasEditableColumns();

        presenter.handlePermissions(dtablePresenter);

        verify(view).enableColumnOperationsMenu();
    }

    @Test
    public void testHandlePermissionsWhenGuidedDecisionTableDoesNotHaveEditableColumns() {

        doReturn(false).when(dtablePresenter).hasEditableColumns();

        presenter.handlePermissions(dtablePresenter);

        verify(view, never()).enableColumnOperationsMenu();
    }

    @Test
    public void testIsColumnCreationEnabledWhenGuidedDecisionTableIsEditableAndHasEditableColumns() {

        doReturn(model).when(dtablePresenter).getModel();
        doReturn(false).when(dtablePresenter).isReadOnly();
        doReturn(true).when(dtablePresenter).hasEditableColumns();

        final boolean isEnabled = presenter.isColumnCreationEnabled(dtablePresenter);

        assertTrue(isEnabled);
    }

    @Test
    public void testIsColumnCreationEnabledWhenGuidedDecisionTableDoesNotHaveEditableColumns() {

        doReturn(model).when(dtablePresenter).getModel();
        doReturn(false).when(dtablePresenter).isReadOnly();
        doReturn(false).when(dtablePresenter).hasEditableColumns();

        final boolean isEnabled = presenter.isColumnCreationEnabled(dtablePresenter);

        assertFalse(isEnabled);
    }

    @Test
    public void testIsColumnCreationEnabledWhenGuidedDecisionTableIsNotEditable() {

        doReturn(model).when(dtablePresenter).getModel();
        doReturn(true).when(dtablePresenter).isReadOnly();
        doReturn(true).when(dtablePresenter).hasEditableColumns();

        final boolean isEnabled = presenter.isColumnCreationEnabled(dtablePresenter);

        assertFalse(isEnabled);
    }

    @Test
    public void testIsColumnCreationEnabledWhenGuidedDecisionTableIsNotEditableNeitherHasEditableColumns() {

        doReturn(model).when(dtablePresenter).getModel();
        doReturn(true).when(dtablePresenter).isReadOnly();
        doReturn(false).when(dtablePresenter).hasEditableColumns();

        final boolean isEnabled = presenter.isColumnCreationEnabled(dtablePresenter);

        assertFalse(isEnabled);
    }

    @Test
    public void testRefreshDefinitionsPanel() {

        doReturn(model).when(dtablePresenter).getModel();
        doReturn(true).when(presenter).isColumnCreationEnabled(dtablePresenter);

        presenter.refreshDefinitionsPanel(dtablePresenter);

        verify(view).setEnableColumnCreation(true);
        verify(view).refreshAttributeWidget(model.getAttributeCols());
        verify(view).refreshMetaDataWidget(model.getMetadataCols());
        verify(view).refreshConditionsWidget(model.getConditions());
        verify(view).refreshActionsWidget(model.getActionCols());
        verify(view).refreshColumnsNote(dtablePresenter.hasColumnDefinitions());
    }

    @Test
    public void testIsColumnCreationEnabledToActiveDecisionTableWhenActiveDecisionTableIsNull() {

        doReturn(null).when(presenter).getActiveDecisionTable();
        doReturn(true).when(presenter).isColumnCreationEnabled(any());

        final boolean isEnabled = presenter.isColumnCreationEnabledToActiveDecisionTable();

        assertFalse(isEnabled);
    }

    @Test
    public void testIsColumnCreationEnabledToActiveDecisionTableWhenActiveDecisionTableIsNotNull() {

        doReturn(dtablePresenter).when(presenter).getActiveDecisionTable();
        doReturn(true).when(presenter).isColumnCreationEnabled(dtablePresenter);

        final boolean isEnabled = presenter.isColumnCreationEnabledToActiveDecisionTable();

        assertTrue(isEnabled);
    }
}
