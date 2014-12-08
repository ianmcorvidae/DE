package org.iplantc.de.client.models;

import java.util.Map;

@SuppressWarnings("nls")
public class DEProperties {
    private static final String PATH_LIST_FILE_IDENTIFIER = "org.iplantc.pathList.fileIdentifier";
    /**
     * The prefix used in each of the property names.
     */
    private static final String PROPERTY_NAME_PREFIX = "org.iplantc.discoveryenvironment.";

    /**
     * The base URL used to access the Mule services.
     */
    private static final String MULE_SERVICE_BASE_URL = PROPERTY_NAME_PREFIX + "muleServiceBaseUrl";

    /**
     * The base URL used to access the Mule services.
     */
    private static final String UNPROTECTED_MULE_SERVICE_BASE_URL = PROPERTY_NAME_PREFIX
            + "unprotectedMuleServiceBaseUrl";

    /**
     * Properties key of the base URL of the data management services.
     */
    private static final String DATA_MGMT_BASE_URL = "org.iplantc.services.de-data-mgmt.base";

    /**
     * Properties key of the base URL of the file I/O services.
     */
    private static final String FILE_IO_BASE_URL = "org.iplantc.services.file-io.base.secured";

    /**
     * Properties key of the notification polling interval
     */
    private static final String NOTIFICATION_POLL_INTERVAL = "org.iplantc.discoveryenvironment.notifications.poll-interval";

    /**
     * Properties key of the context click enabled option
     */
    private static final String CONTEXT_CLICK_ENABLED = "org.iplantc.discoveryenvironment.contextMenu.enabled";

    /**
     * The prefix used in each of the private workspace property names.
     */
    private static final String WORKSPACE_PREFIX = "org.iplantc.discoveryenvironment.workspace.";

    /**
     * Properties key for the private workspace
     */
    private static final String PRIVATE_WORKSPACE = WORKSPACE_PREFIX + "rootAppCategory";

    /**
     * Properties key for the private workspace items
     */
    private static final String PRIVATE_WORKSPACE_ITEMS = WORKSPACE_PREFIX + "defaultAppCategories";

    /**
     * Properties key for the default Beta Category ID
     */
    private static final String DEFAULT_BETA_CATEGORY_ID = WORKSPACE_PREFIX
            + "defaultBetaAppCategoryId";

    /**
     * Properties key for the default output folder name
     */
    private static final String DEFAULT_OUTPUT_FOLDER_NAME = WORKSPACE_PREFIX
            + "defaultOutputFolderName";

    /**
     * The prefix used for each of the keepalive configuration parameters.
     */
    private static final String KEEPALIVE_PREFIX = PROPERTY_NAME_PREFIX + "keepalive.";

    /**
     * The URL that we use for keepalive requests.
     */
    private static final String KEEPALIVE_TARGET = KEEPALIVE_PREFIX + "target";

    /**
     * The number of minutes between keepalive requests.
     */
    private static final String KEEPALIVE_INTERVAL = KEEPALIVE_PREFIX + "interval";

    /**
     * The single instance of this class.
     */
    private static DEProperties instance;

    /**
     * The base URL of the data management services.
     */
    private String dataMgmtBaseUrl;

    /**
     * The base URL of the file I/O services.
     */
    private String fileIoBaseUrl;

    /**
     * The polling interval
     */
    private int notificationPollInterval;

    /**
     * Context click option
     */
    private boolean contextClickEnabled;

    /**
     * private workspace name
     */
    private String privateWorkspace;

    /**
     * private workspace items
     *
     */
    private String privateWorkspaceItems;

    /**
     * ID of the default Beta Workspace Category
     */
    private String defaultBetaCategoryId;

    /**
     *
     * Default output folder name
     *
     */
    private String defaultOutputFolderName;

    private String pathListFileIdentifier;

    public String getPathListFileIdentifier() {
        return pathListFileIdentifier;
    }

    /**
     * @return the contextClickEnabled
     */
    public boolean isContextClickEnabled() {
        return contextClickEnabled;
    }

    /**
     * The base URL used to access the DE Mule services.
     */
    private String muleServiceBaseUrl;

    /**
     * The base URL used to access the DE Unprotected Mule services.
     */
    private String unproctedMuleServiceBaseUrl;

    /**
     * @return the unproctedMuleServiceBaseUrl
     */
    public String getUnproctedMuleServiceBaseUrl() {
        return unproctedMuleServiceBaseUrl;
    }

    /**
     * The target URL that we use for keepalive requests.
     */
    private String keepaliveTarget;

    /**
     * The number of minutes between keepalive requests.
     */
    private int keepaliveInterval;

    /**
     * Force the constructor to be private.
     */
    private DEProperties() {
    }

    /**
     * Gets the single instance of this class.
     *
     * @return the instance.
     */
    public static DEProperties getInstance() {
        if (instance == null) {
            instance = new DEProperties();
        }
        return instance;
    }

    /**
     * Initializes this class from the given set of properties.
     *
     * @param properties the properties that were fetched from the server.
     */
    public void initialize(Map<String, String> properties) {
        dataMgmtBaseUrl = properties.get(DATA_MGMT_BASE_URL);
        fileIoBaseUrl = properties.get(FILE_IO_BASE_URL);
        muleServiceBaseUrl = properties.get(MULE_SERVICE_BASE_URL);
        unproctedMuleServiceBaseUrl = properties.get(UNPROTECTED_MULE_SERVICE_BASE_URL);
        privateWorkspace = properties.get(PRIVATE_WORKSPACE);
        privateWorkspaceItems = properties.get(PRIVATE_WORKSPACE_ITEMS);
        defaultBetaCategoryId = properties.get(DEFAULT_BETA_CATEGORY_ID);
        defaultOutputFolderName = properties.get(DEFAULT_OUTPUT_FOLDER_NAME);
        contextClickEnabled = getBoolean(properties, CONTEXT_CLICK_ENABLED, false);
        notificationPollInterval = getInt(properties, NOTIFICATION_POLL_INTERVAL, 60);
        keepaliveTarget = properties.get(KEEPALIVE_TARGET);
        keepaliveInterval = getInt(properties, KEEPALIVE_INTERVAL, -1);
        pathListFileIdentifier = properties.get(PATH_LIST_FILE_IDENTIFIER);
    }

    /**
     * Obtains a boolean property value.
     *
     * @param properties the property map.
     * @param name the name of the property.
     * @param defaultValue the default value to use.
     * @return the property value or its default value.
     */
    private boolean getBoolean(Map<String, String> properties, String name, boolean defaultValue) {
        try {
            return Boolean.parseBoolean(properties.get(name));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Obtains an integer property value.
     *
     * @param properties the property map.
     * @param name the name of the property.
     * @param defaultValue the default value to use.
     * @return the property value or its default value.
     */
    private int getInt(Map<String, String> properties, String name, int defaultValue) {
        try {
            return Integer.parseInt(properties.get(name));
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Gets the polling interval for the MessagePoller of the Notification agent service.
     *
     * @return the poll interval in seconds as an int.
     */
    public int getNotificationPollInterval() {
        return notificationPollInterval;
    }

    /**
     * Gets the base URL of the data management services.
     *
     * @return the URL as a string.
     */
    public String getDataMgmtBaseUrl() {
        return dataMgmtBaseUrl;
    }

    /**
     * Gets the base URL of the file I/O services.
     *
     * @return the URL as a string.
     */
    public String getFileIoBaseUrl() {
        return fileIoBaseUrl;
    }

    /**
     * Gets the base URL used to access the DE Mule services.
     *
     * @return the URL as a string.
     */
    public String getMuleServiceBaseUrl() {
        return muleServiceBaseUrl;
    }

    /**
     * @return the privateWorkspace
     */
    public String getPrivateWorkspace() {
        return privateWorkspace;
    }

    /**
     * @return the privateWorkspaceItems
     */
    public String getPrivateWorkspaceItems() {
        return privateWorkspaceItems;
    }

    /**
     * @return the unique ID for the Beta category.
     */
    public String getDefaultBetaCategoryId() {
        return defaultBetaCategoryId;
    }

    /**
     * @return the defaultOutputFolderName
     */
    public String getDefaultOutputFolderName() {
        return defaultOutputFolderName;
    }

    /**
     * @return the URL that we use for keepalive requests.
     */
    public String getKeepaliveTarget() {
        return keepaliveTarget;
    }

    /**
     * @return the number of minutes between keepalive requests.
     */
    public int getKeepaliveInterval() {
        return keepaliveInterval;
    }

}