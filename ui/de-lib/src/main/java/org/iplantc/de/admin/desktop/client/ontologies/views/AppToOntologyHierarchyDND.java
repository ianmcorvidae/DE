package org.iplantc.de.admin.desktop.client.ontologies.views;

import org.iplantc.de.admin.apps.client.AdminAppsGridView;
import org.iplantc.de.admin.desktop.client.ontologies.OntologiesView;
import org.iplantc.de.client.models.apps.App;
import org.iplantc.de.client.models.ontologies.OntologyHierarchy;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.EventTarget;
import com.google.gwt.user.client.ui.Widget;

import com.sencha.gxt.dnd.core.client.DndDragEnterEvent;
import com.sencha.gxt.dnd.core.client.DndDragMoveEvent;
import com.sencha.gxt.dnd.core.client.DndDragStartEvent;
import com.sencha.gxt.dnd.core.client.DndDropEvent;
import com.sencha.gxt.dnd.core.client.StatusProxy;
import com.sencha.gxt.fx.client.DragMoveEvent;

import java.util.List;

/**
 * @author aramsey
 */
public class AppToOntologyHierarchyDND implements DndDragStartEvent.DndDragStartHandler,
                                                  DndDropEvent.DndDropHandler,
                                                  DndDragMoveEvent.DndDragMoveHandler,
                                                  DndDragEnterEvent.DndDragEnterHandler {

    OntologiesView.OntologiesViewAppearance appearance;
    AdminAppsGridView.Presenter oldGridPresenter;
    AdminAppsGridView.Presenter newGridPresenter;
    Widget oldGridView;
    Widget newGridView;
    OntologiesView.Presenter presenter;
    boolean moved;

    public AppToOntologyHierarchyDND(OntologiesView.OntologiesViewAppearance appearance,
                                     AdminAppsGridView.Presenter oldGridPresenter,
                                     AdminAppsGridView.Presenter newGridPresenter,
                                     OntologiesView.Presenter presenter) {
        this.appearance = appearance;
        this.oldGridPresenter = oldGridPresenter;
        this.newGridPresenter = newGridPresenter;
        this.presenter = presenter;
        this.oldGridView = oldGridPresenter.getView().asWidget();
        this.newGridView = newGridPresenter.getView().asWidget();
    }

    @Override
    public void onDragEnter(DndDragEnterEvent event) {
        moved = false;

        Widget dragSource = event.getDragEnterEvent().getSource().getDragWidget();
        List<App> apps = getDragSources(dragSource);
        DragMoveEvent dragEnterEvent = event.getDragEnterEvent();
        EventTarget target = dragEnterEvent.getNativeEvent().getEventTarget();
        OntologyHierarchy hierarchy = getDropTargetHierarchy(Element.as(target));

        if (!validateDropStatus(hierarchy, apps, event.getStatusProxy())) {
            event.setCancelled(true);
        }
    }

    @Override
    public void onDragMove(DndDragMoveEvent event) {
        moved = true;

        Widget dragSource = event.getDragMoveEvent().getSource().getDragWidget();
        List<App> apps = getDragSources(dragSource);
        EventTarget target = event.getDragMoveEvent().getNativeEvent().getEventTarget();
        OntologyHierarchy hierarchy  = getDropTargetHierarchy(Element.as(target));

        if (!validateDropStatus(hierarchy, apps, event.getStatusProxy())) {
            event.setCancelled(true);
        }
    }


    @Override
    public void onDragStart(DndDragStartEvent event) {
        moved = false;

        Widget dragSource = event.getDragStartEvent().getSource().getDragWidget();
        List<App> apps = getDragSources(dragSource);

        if (apps == null) {
            // Cancel drag
            event.setCancelled(true);
        } else {
            event.setData(apps);
            event.getStatusProxy().update(getAppLabels(apps));
            event.getStatusProxy().setStatus(true);
            event.setCancelled(false);
        }
    }

    @Override
    public void onDrop(DndDropEvent event) {
        if (!moved) return;

        Widget dragSource = event.getDragEndEvent().getSource().getDragWidget();
        List<App> apps = getDragSources(dragSource);
        EventTarget target = event.getDragEndEvent().getNativeEvent().getEventTarget();
        OntologyHierarchy hierarchy  = getDropTargetHierarchy(Element.as(target));

        if (validateDropStatus(hierarchy, apps, event.getStatusProxy())) {
            presenter.appsDNDtoHierarchy(apps, hierarchy);
        }

    }

    private List<App> getDragSources(Widget dragWidget) {
        if (isOldGridApp(dragWidget)) {
            return oldGridPresenter.getSelectedApps();
        }
        if (isNewGridApp(dragWidget)) {
            return newGridPresenter.getSelectedApps();
        }
        return null;
    }

    private boolean isOldGridApp(Widget dragSource) {
        return dragSource == oldGridView;
    }

    private boolean isNewGridApp(Widget dragSource) {
        return dragSource == newGridView;
    }

    private OntologyHierarchy getDropTargetHierarchy(Element eventTarget) {
        return presenter.getHierarchyFromElement(eventTarget);
    }

    private boolean validateDropStatus(final OntologyHierarchy targetHierarchy,
                                       final List<App> apps,
                                       final StatusProxy status) {
        // Verify we have drag data.
        if (apps == null || apps.size() == 0) {
            status.setStatus(false);
            status.update("");
            return false;
        }

        String appList = getAppLabels(apps);

        // Verify we have a drop target.
        if (targetHierarchy == null) {
            status.setStatus(false);
            status.update(appList);
            return false;
        }

        // Reset status message
        status.setStatus(true);
        status.update(appList + " > " + targetHierarchy.getLabel());
        return true;
    }

    private String getAppLabels(List<App> apps) {
        List<String> labels = Lists.newArrayList();
        for (App app: apps) {
            labels.add(app.getName());
        }
        return Joiner.on(",").join(labels);
    }
}
