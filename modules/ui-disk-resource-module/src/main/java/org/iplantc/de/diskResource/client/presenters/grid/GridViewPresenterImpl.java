package org.iplantc.de.diskResource.client.presenters.grid;

import org.iplantc.de.client.events.EventBus;
import org.iplantc.de.client.events.diskResources.OpenFolderEvent;
import org.iplantc.de.client.models.HasId;
import org.iplantc.de.client.models.dataLink.DataLink;
import org.iplantc.de.client.models.diskResources.DiskResource;
import org.iplantc.de.client.models.diskResources.File;
import org.iplantc.de.client.models.diskResources.Folder;
import org.iplantc.de.client.models.diskResources.PermissionValue;
import org.iplantc.de.client.models.diskResources.TYPE;
import org.iplantc.de.client.models.viewer.InfoType;
import org.iplantc.de.client.services.DiskResourceServiceFacade;
import org.iplantc.de.client.services.FileSystemMetadataServiceFacade;
import org.iplantc.de.client.util.DiskResourceUtil;
import org.iplantc.de.commons.client.ErrorHandler;
import org.iplantc.de.commons.client.comments.view.dialogs.CommentsDialog;
import org.iplantc.de.commons.client.info.ErrorAnnouncementConfig;
import org.iplantc.de.commons.client.info.IplantAnnouncer;
import org.iplantc.de.commons.client.views.dialogs.IPlantDialog;
import org.iplantc.de.diskResource.client.DiskResourceView;
import org.iplantc.de.diskResource.client.GridView;
import org.iplantc.de.diskResource.client.NavigationView;
import org.iplantc.de.diskResource.client.events.DiskResourceNameSelectedEvent;
import org.iplantc.de.diskResource.client.events.DiskResourcePathSelectedEvent;
import org.iplantc.de.diskResource.client.events.DiskResourceSelectionChangedEvent;
import org.iplantc.de.diskResource.client.events.FolderSelectionEvent;
import org.iplantc.de.diskResource.client.events.RequestDiskResourceFavoriteEvent;
import org.iplantc.de.diskResource.client.events.ShowFilePreviewEvent;
import org.iplantc.de.diskResource.client.events.search.SubmitDiskResourceQueryEvent;
import org.iplantc.de.diskResource.client.events.selection.EditInfoTypeSelected;
import org.iplantc.de.diskResource.client.events.selection.ManageCommentsSelected;
import org.iplantc.de.diskResource.client.events.selection.ManageMetadataSelected;
import org.iplantc.de.diskResource.client.events.selection.ManageSharingSelected;
import org.iplantc.de.diskResource.client.events.selection.ResetInfoTypeSelected;
import org.iplantc.de.diskResource.client.events.selection.ShareByDataLinkSelected;
import org.iplantc.de.diskResource.client.gin.factory.DataSharingDialogFactory;
import org.iplantc.de.diskResource.client.gin.factory.FolderContentsRpcProxyFactory;
import org.iplantc.de.diskResource.client.gin.factory.GridViewFactory;
import org.iplantc.de.diskResource.client.model.DiskResourceModelKeyProvider;
import org.iplantc.de.diskResource.client.presenters.grid.proxy.FolderContentsLoadConfig;
import org.iplantc.de.diskResource.client.presenters.grid.proxy.SelectDiskResourceByIdStoreAddHandler;
import org.iplantc.de.diskResource.client.views.dialogs.InfoTypeEditorDialog;
import org.iplantc.de.diskResource.client.views.grid.DiskResourceColumnModel;
import org.iplantc.de.diskResource.client.views.metadata.dialogs.ManageMetadataDialog;
import org.iplantc.de.diskResource.client.views.sharing.DataSharingDialog;

import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.inject.client.AsyncProvider;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;
import com.google.inject.assistedinject.Assisted;

import com.sencha.gxt.core.shared.FastMap;
import com.sencha.gxt.data.shared.ListStore;
import com.sencha.gxt.data.shared.event.StoreUpdateEvent;
import com.sencha.gxt.data.shared.loader.PagingLoadResult;
import com.sencha.gxt.data.shared.loader.PagingLoader;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.event.DialogHideEvent;
import com.sencha.gxt.widget.core.client.event.SelectEvent;
import com.sencha.gxt.widget.core.client.form.TextField;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * @author jstroot
 */
public class GridViewPresenterImpl implements GridView.Presenter,
                                              DiskResourcePathSelectedEvent.DiskResourcePathSelectedEventHandler,
                                              DiskResourceSelectionChangedEvent.DiskResourceSelectionChangedEventHandler {


    private class CreateDataLinksCallback implements AsyncCallback<List<DataLink>> {

        @Override
        public void onFailure(Throwable caught) {
            ErrorHandler.post(appearance.createDataLinksError(), caught);
        }

        @Override
        public void onSuccess(List<DataLink> result) {
            showShareLink(result.get(0).getDownloadUrl());
        }
    }
    @Inject IplantAnnouncer announcer;
    @Inject DataSharingDialogFactory dataSharingDialogFactory;
    @Inject DiskResourceServiceFacade diskResourceService;
    @Inject DiskResourceUtil diskResourceUtil;
    @Inject EventBus eventBus;
    @Inject FileSystemMetadataServiceFacade metadataService;
    @Inject AsyncProvider<InfoTypeEditorDialog> infoTypeDialogProvider;
    @Inject AsyncProvider<CommentsDialog> commentDialogProvider;
    @Inject AsyncProvider<ManageMetadataDialog> metadataDialogProvider;

    private final Appearance appearance;
    private final ListStore<DiskResource> listStore;
    private final NavigationView.Presenter navigationPresenter;
    private final HashMap<EventHandler, HandlerRegistration> registeredHandlers = Maps.newHashMap();
    private final GridView view;
    private boolean filePreviewEnabled = true;
    private DiskResourceView.Presenter parentPresenter;

    @Inject
    GridViewPresenterImpl(final GridViewFactory gridViewFactory,
                          final FolderContentsRpcProxyFactory folderContentsProxyFactory,
                          final GridView.Presenter.Appearance appearance,
                          @Assisted final NavigationView.Presenter navigationPresenter,
                          @Assisted final List<InfoType> infoTypeFilters,
                          @Assisted final TYPE entityType) {
        this.appearance = appearance;
        this.navigationPresenter = navigationPresenter;
        this.listStore = new ListStore<>(new DiskResourceModelKeyProvider());
        GridView.FolderContentsRpcProxy folderContentsRpcProxy = folderContentsProxyFactory.createWithEntityType(infoTypeFilters, entityType);

        this.view = gridViewFactory.create(this, listStore, folderContentsRpcProxy);

        // Wire up Column Model events
        DiskResourceColumnModel cm = this.view.getColumnModel();
        cm.addDiskResourceNameSelectedEventHandler(this);
        cm.addManageSharingSelectedEventHandler(this);
        cm.addManageMetadataSelectedEventHandler(this);
        cm.addShareByDataLinkSelectedEventHandler(this);
        cm.addManageFavoritesEventHandler(this);
        cm.addManageCommentsSelectedEventHandler(this);
        cm.addDiskResourcePathSelectedEventHandler(this);

        // Fetch Details
        this.view.addDiskResourceSelectionChangedEventHandler(this);

    }

    //<editor-fold desc="Handler Registrations">
    @Override
    public HandlerRegistration addStoreUpdateHandler(StoreUpdateEvent.StoreUpdateHandler<DiskResource> handler) {
        return listStore.addStoreUpdateHandler(handler);
    }
    //</editor-fold>

    //<editor-fold desc="Event Handlers">
    @Override
    public void doSubmitDiskResourceQuery(SubmitDiskResourceQueryEvent event) {
        doFolderSelected(event.getQueryTemplate());
    }

    @Override
    public void onDiskResourceNameSelected(DiskResourceNameSelectedEvent event) {

        if (!(event.getSelectedItem() instanceof File) || !filePreviewEnabled) {
            return;
        }
        eventBus.fireEvent(new ShowFilePreviewEvent((File) event.getSelectedItem(), this));
    }

    @Override
    public void onDiskResourcePathSelected(DiskResourcePathSelectedEvent event) {
        final OpenFolderEvent openFolderEvent = new OpenFolderEvent(diskResourceUtil.parseParent(event.getSelectedDiskResource().getPath()));
        openFolderEvent.requestNewView(true);
        eventBus.fireEvent(openFolderEvent);
    }

    @Override
    public void onDiskResourceSelectionChanged(DiskResourceSelectionChangedEvent event) {
        // Fetch details

        final List<DiskResource> selection = event.getSelection();
        if (selection.size() != 1) {
            // Only call get stat for single selections
            return;
        }
        fetchDetails(selection.iterator().next());
    }

    @Override
    public void onEditInfoTypeSelected(final EditInfoTypeSelected event) {
        Preconditions.checkState(event.getSelectedDiskResources().size() == 1, "Only one Disk Resource should be selected, but there are %i", getSelectedDiskResources().size());

        final String infoType = event.getSelectedDiskResources().iterator().next().getInfoType();
        infoTypeDialogProvider.get(new AsyncCallback<InfoTypeEditorDialog>() {
            @Override
            public void onFailure(Throwable caught) {

                announcer.schedule(new ErrorAnnouncementConfig("AsyncProvider failed"));
            }

            @Override
            public void onSuccess(final InfoTypeEditorDialog result) {
                result.addOkButtonSelectHandler(new SelectEvent.SelectHandler() {

                    @Override
                    public void onSelect(SelectEvent event1) {
                        String newType = result.getSelectedValue().toString();
                        setInfoType(event.getSelectedDiskResources().iterator().next(), newType);
                    }
                });
                result.show(InfoType.fromTypeString(infoType));
            }
        });
    }

    @Override
    public void onFavoriteRequest(RequestDiskResourceFavoriteEvent event) {
        final DiskResource diskResource = event.getDiskResource();
        Preconditions.checkNotNull(diskResource);
        if (!diskResource.isFavorite()) {
            metadataService.addToFavorites(diskResource.getId(), new AsyncCallback<String>() {

                @Override
                public void onFailure(Throwable caught) {
                    ErrorHandler.post(appearance.markFavoriteError(), caught);

                }

                @Override
                public void onSuccess(String result) {
                    updateFav(diskResource, true);
                }
            });
        } else {
            metadataService.removeFromFavorites(diskResource.getId(), new AsyncCallback<String>() {

                @Override
                public void onFailure(Throwable caught) {
                    ErrorHandler.post(appearance.removeFavoriteError(), caught);
                }

                @Override
                public void onSuccess(String result) {
                    updateFav(diskResource, false);
                }
            });
        }
    }

    @Override
    public void onFolderSelected(FolderSelectionEvent event) {
        doFolderSelected(event.getSelectedFolder());
    }

    @Override
    public void onManageCommentsSelected(ManageCommentsSelected event) {
        final DiskResource dr = event.getDiskResource();
        commentDialogProvider.get(new AsyncCallback<CommentsDialog>() {
            @Override
            public void onFailure(Throwable caught) {
                announcer.schedule(new ErrorAnnouncementConfig("Something happened while trying to manage comments. Please try again or contact support for help."));
            }

            @Override
            public void onSuccess(CommentsDialog result) {
                result.show(dr,
                            PermissionValue.own.equals(dr.getPermission()),
                            metadataService);
            }
        });
    }

    @Override
    public void onRequestManageMetadataSelected(ManageMetadataSelected event) {
        final DiskResource selected = event.getDiskResource();

        metadataDialogProvider.get(new AsyncCallback<ManageMetadataDialog>() {
            @Override
            public void onFailure(Throwable caught) {
                announcer.schedule(new ErrorAnnouncementConfig("Something happened while trying to view/manage metadata. Please try again or contact support for help."));
            }

            @Override
            public void onSuccess(ManageMetadataDialog result) {
                result.show(selected);
            }
        });
    }

    @Override
    public void onRequestManageSharingSelected(ManageSharingSelected event) {
        DataSharingDialog dlg = dataSharingDialogFactory.createDataSharingDialog(Sets.newHashSet(event.getDiskResourceToShare()));
        dlg.show();
        dlg.addDialogHideHandler(new DialogHideEvent.DialogHideHandler() {
            @Override
            public void onDialogHide(DialogHideEvent event) {
                final List<DiskResource> selection = getSelectedDiskResources();
                if (selection != null && selection.size() == 1) {
                    Iterator<DiskResource> it = selection.iterator();
                    DiskResource next = it.next();
                    fetchDetails(next);
                }
            }
        });
    }

    @Override
    public void onRequestShareByDataLinkSelected(ShareByDataLinkSelected event) {
        DiskResource toBeShared = event.getDiskResourceToShare();
        if (toBeShared instanceof Folder) {
            showShareLink(GWT.getHostPageBaseURL() + "?type=data&folder=" + toBeShared.getPath());
        } else {
            diskResourceService.createDataLinks(Arrays.asList(toBeShared.getPath()), new CreateDataLinksCallback());
        }
    }

    @Override
    public void onResetInfoTypeSelected(ResetInfoTypeSelected event) {
        setInfoType(event.getDiskResource(), "");
    }
    //</editor-fold>

    @Override
    public void deSelectDiskResources() {
        view.getSelectionModel().deselectAll();
    }

    @Override
    public void doMoveDiskResources(Folder targetFolder, List<DiskResource> resources) {
        parentPresenter.doMoveDiskResources(targetFolder, resources);
    }

    @Override
    public Element findGridRow(Element eventTargetElement) {
        return view.findGridRow(eventTargetElement);
    }

    @Override
    public int findGridRowIndex(Element targetRow) {
        return view.findGridRowIndex(targetRow);
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        // This method is extended by StoreUpdateHandler, so we must implement
        throw new UnsupportedOperationException();
    }

    @Override
    public List<DiskResource> getAllDiskResources() {
        return listStore.getAll();
    }

    @Override
    public List<DiskResource> getSelectedDiskResources() {
        return view.getSelectionModel().getSelectedItems();
    }

    @Override
    public Folder getSelectedUploadFolder() {
        return navigationPresenter.getSelectedUploadFolder();
    }

    @Override
    public GridView getView() {
        return view;
    }

    @Override
    public boolean isSelectAllChecked() {
        return view.getSelectionModel().isSelectAllChecked();
    }

    @Override
    public void setFilePreviewEnabled(boolean filePreviewEnabled) {
        this.filePreviewEnabled = filePreviewEnabled;
    }

    @Override
    public void setParentPresenter(DiskResourceView.Presenter parentPresenter) {
        this.parentPresenter = parentPresenter;
    }

    @Override
    public void setSelectedDiskResourcesById(List<? extends HasId> diskResourcesToSelect) {
        SelectDiskResourceByIdStoreAddHandler diskResourceByIdStoreAddHandler = new SelectDiskResourceByIdStoreAddHandler(diskResourcesToSelect, this);
        HandlerRegistration diskResHandlerReg = listStore.addStoreAddHandler(diskResourceByIdStoreAddHandler);
        registeredHandlers.put(diskResourceByIdStoreAddHandler, diskResHandlerReg);
    }

    @Override
    public void unRegisterHandler(EventHandler handler) {
        if (registeredHandlers.containsKey(handler)) {
            registeredHandlers.remove(handler).removeHandler();
        }
    }

    void doFolderSelected(final Folder selectedFolder) {
        final PagingLoader<FolderContentsLoadConfig, PagingLoadResult<DiskResource>> gridLoader = view.getGridLoader();
        gridLoader.getLastLoadConfig().setFolder(selectedFolder);
        gridLoader.getLastLoadConfig().setOffset(0);
        gridLoader.load();
    }

    DiskResource updateDiskResource(DiskResource diskResource) {
        Preconditions.checkNotNull(diskResource);
        final DiskResource modelWithKey = listStore.findModelWithKey(diskResource.getId());
        if (modelWithKey == null) {
            return null;
        }

        final DiskResource updated = diskResourceService.combineDiskResources(diskResource, modelWithKey);
        listStore.update(updated);
        return updated;
    }

    private void fetchDetails(final DiskResource resource) {
        diskResourceService.getStat(diskResourceUtil.asStringPathTypeMap(Arrays.asList(resource),
                                                                         resource instanceof File ? TYPE.FILE
                                                                             : TYPE.FOLDER),
                                    new AsyncCallback<FastMap<DiskResource>>() {
                                        @Override
                                        public void onFailure(Throwable caught) {
                                            ErrorHandler.post(appearance.retrieveStatFailed(), caught);
                                            // This unmasks the sendTo.. toolbar buttons
                                            //        presenter.unmaskVizMenuOptions();
                                        }

                                        @Override
                                        public void onSuccess(FastMap<DiskResource> drMap) {
                                            /* FIXME Fire global event to update diskresource
                                             * The toolbarview will need to listen
                                             * The gridviewpresenter will need to listen
                                             *    -- Fire another event from gridview
                                             */
                                            final DiskResource diskResource = drMap.get(resource.getPath());
                                            Preconditions.checkNotNull(diskResource, "This object cannot be null at this point.");
                                            updateDiskResource(diskResource);

                                            //        presenter.getView().unmaskDetailsPanel();
                                            //        presenter.unmaskVizMenuOptions();
                                        }
                                    });
        // Need to mask the SendTo... options from the toolbar
//        view.maskSendToCoGe();
//        view.maskSendToEnsembl();
//        view.maskSendToTreeViewer();

    }

    private void setInfoType(final DiskResource dr, String newType) {
        diskResourceService.setFileType(dr.getPath(), newType, new AsyncCallback<String>() {

            @Override
            public void onFailure(Throwable arg0) {
                ErrorHandler.post(arg0);
            }

            @Override
            public void onSuccess(String arg0) {
                // Fetching the details will update the item in the grid
                fetchDetails(dr);
            }
        });
    }

    private void showShareLink(String linkId) {
        // FIXME Fold into separate view/dlg
        // Open dialog window with text selected.
        IPlantDialog dlg = new IPlantDialog();
        dlg.setHeadingText(appearance.copy());
        dlg.setHideOnButtonClick(true);
        dlg.setResizable(false);
        dlg.setSize(appearance.shareLinkDialogWidth(), appearance.shareLinkDialogHeight());
        TextField textBox = new TextField();
        textBox.setWidth(appearance.shareLinkDialogTextBoxWidth());
        textBox.setReadOnly(true);
        textBox.setValue(linkId);
        VerticalLayoutContainer container = new VerticalLayoutContainer();
        dlg.setWidget(container);
        container.add(textBox);
        container.add(new Label(appearance.copyPasteInstructions()));
        dlg.setFocusWidget(textBox);
        dlg.show();
        textBox.selectAll();
    }

    private void updateFav(final DiskResource diskResource, boolean fav) {
        if (getSelectedDiskResources().size() > 0) {
            Iterator<DiskResource> it = getSelectedDiskResources().iterator();
            if (it.hasNext()) {
                final DiskResource next = it.next();
                if (next.getId().equals(diskResource.getId())) {
                    next.setFavorite(fav);
                    updateDiskResource(next);
                }
            }
        }
    }

}
