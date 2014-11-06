package org.iplantc.de.diskResource.client.views.dialogs;


import org.iplantc.de.client.models.HasPath;
import org.iplantc.de.client.models.UserSettings;
import org.iplantc.de.client.models.diskResources.Folder;
import org.iplantc.de.client.util.CommonModelUtils;
import org.iplantc.de.commons.client.validators.DiskResourceNameValidator;
import org.iplantc.de.commons.client.views.gxt3.dialogs.IPlantDialog;
import org.iplantc.de.diskResource.client.events.FolderSelectionEvent;
import org.iplantc.de.diskResource.client.gin.DiskResourceInjector;
import org.iplantc.de.diskResource.client.views.DiskResourceView;
import org.iplantc.de.resources.client.messages.I18N;

import com.google.common.base.Strings;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;

import com.sencha.gxt.widget.core.client.button.TextButton;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer;
import com.sencha.gxt.widget.core.client.container.VerticalLayoutContainer.VerticalLayoutData;
import com.sencha.gxt.widget.core.client.form.FieldLabel;
import com.sencha.gxt.widget.core.client.form.TextField;

public class SaveAsDialog extends IPlantDialog {

	private final DiskResourceView.Presenter presenter;
	private final TextField selectedFolderField = new TextField();
	private final TextField fileNameField = new TextField();
	private Folder selectedFolder = null;

	public SaveAsDialog(Folder selectedFolder) {
		selectedFolderField.setAllowBlank(false);
		selectedFolderField.setReadOnly(true);
		selectedFolderField.setAutoValidate(true);
		fileNameField.setAllowBlank(false);
		fileNameField.addValidator(new DiskResourceNameValidator());
		fileNameField.setAutoValidate(true);
		setHideOnButtonClick(false);

		initDialog();

		addKeyHandlers(getOkButton());

		presenter = DiskResourceInjector.INSTANCE
				.getDiskResourceViewPresenter();

		final FieldLabel fl1 = new FieldLabel(selectedFolderField,
				I18N.DISPLAY.selectedFolder());
		final FieldLabel fl2 = new FieldLabel(fileNameField,
				I18N.DISPLAY.fileName());

		VerticalLayoutContainer vlc = buildLayout(fl1, fl2);

		initPresenter(getOkButton(), vlc);

		setDefaultSelectedFolder(selectedFolder);

	}

	private void setDefaultSelectedFolder(Folder selectedFolder) {
		// if not refresh and currently nothing was selected and remember path
		// is enabled, the go
		// back to last selected folder
		UserSettings instance = UserSettings.getInstance();
//        String path = instance.getDefaultFileSelectorPath();
        String path = instance.getLastPath();
		boolean remember = instance.isRememberLastPath();
		if(selectedFolder != null) {
            presenter.setSelectedFolderByPath(selectedFolder);
        } else if (remember && !Strings.isNullOrEmpty(path)) {
            HasPath folder = CommonModelUtils.createHasPathFromString(path);
            presenter.setSelectedFolderByPath(folder);
		}
	}

	private void initPresenter(TextButton okButton, VerticalLayoutContainer vlc) {
		presenter.getView().setSouthWidget(vlc, 60);
        presenter.addFolderSelectedEventHandler(new FolderSelectionChangedHandler());
		presenter.builder().hideNorth().hideCenter().hideEast().singleSelect()
				.go(this);
	}

	private VerticalLayoutContainer buildLayout(final FieldLabel fl1,
			final FieldLabel fl2) {
		VerticalLayoutContainer vlc = new VerticalLayoutContainer();
		vlc.add(fl1, new VerticalLayoutData(.9, -1));
		vlc.add(fl2, new VerticalLayoutData(.9, -1));
		return vlc;
	}

	private void addKeyHandlers(final TextButton okBtn) {
		fileNameField.addKeyDownHandler(new KeyDownHandler() {

			@Override
			public void onKeyDown(KeyDownEvent event) {
				boolean vaild = isValid();
				if (vaild) {
					if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
						onButtonPressed(okBtn);
					}
				}
			}
		});
	}

	private void initDialog() {
		setResizable(true);
		setSize("480", "425");
		setHeadingText(I18N.DISPLAY.saveAs());
	}

	public void cleanUp() {
		presenter.cleanUp();
	}

	@Override
	public void onHide() {
		cleanUp();
	}

	public boolean isValid() {
		return selectedFolderField.isValid() & fileNameField.isValid();
	}

	private final class FolderSelectionChangedHandler implements FolderSelectionEvent.FolderSelectionEventHandler {

		private FolderSelectionChangedHandler() {
		}

        @Override
        public void onFolderSelected(FolderSelectionEvent event) {
			if (event.getSelectedFolder() == null) {
				return;
			}
			selectedFolder = event.getSelectedFolder();
			selectedFolderField.setValue(selectedFolder.getPath(), true);
			selectedFolderField.validate();

        }

	}

	public String getFileName() {
		return fileNameField.getCurrentValue();
	}

	public Folder getSelectedFolder() {
		return selectedFolder;
	}

}
