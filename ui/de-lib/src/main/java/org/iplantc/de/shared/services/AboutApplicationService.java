package org.iplantc.de.shared.services;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * Provides a synchronous service for retrieving information about the application.
 * 
 * This information will include the user agent browsing the application along with the versions of
 * client software and the build number. Client software versions stated will be for GWT and GXT.
 * 
 * @author lenards
 * @author jstroot
 */
@RemoteServiceRelativePath("about.rpc")
public interface AboutApplicationService extends RemoteService {
    /**
     * Retrieve information about the application.
     * 
     * The information will be encoded in JSON format.
     * 
     * @return a JSON string containing information about the application
     */
    String getAboutInfo();
}
