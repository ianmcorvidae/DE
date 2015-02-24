package org.iplantc.de.theme.base.client.applications.cells;

import org.iplantc.de.apps.client.views.cells.AppHyperlinkCell;
import org.iplantc.de.client.models.apps.App;
import org.iplantc.de.resources.client.messages.IplantDisplayStrings;

import com.google.common.base.Strings;
import com.google.gwt.core.client.GWT;
import com.google.gwt.debug.client.DebugInfo;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

/**
 * @author jstroot
 */
public class AppHyperlinkCellDefaultAppearance implements AppHyperlinkCell.AppHyperlinkCellAppearance {

    public interface MyCss extends CssResource {
        String appName();

        String appDisabled();
    }

    public interface Resources extends ClientBundle {
        @Source("AppHyperlinkCell.css")
        MyCss css();
    }

    public interface Templates extends SafeHtmlTemplates {

        @SafeHtmlTemplates.Template("<span name='{3}' class='{0}' qtip='{2}'>{1}</span>")
        SafeHtml cell(String textClassName, SafeHtml name, String textToolTip, String elementName);

        @SafeHtmlTemplates.Template("<span id='{4}' name='{3}' class='{0}' qtip='{2}'>{1}</span>")
        SafeHtml debugCell(String textClassName, SafeHtml name, String textToolTip, String elementName, String debugId);
    }

    private final Templates templates;
    protected final Resources resources;
    private final IplantDisplayStrings iplantDisplayStrings;

    public AppHyperlinkCellDefaultAppearance() {
        this(GWT.<Templates> create(Templates.class),
             GWT.<Resources> create(Resources.class),
             GWT.<IplantDisplayStrings> create(IplantDisplayStrings.class));
    }

    AppHyperlinkCellDefaultAppearance(final Templates templates,
                                      final Resources resources,
                                      final IplantDisplayStrings iplantDisplayStrings) {
        this.templates = templates;
        this.resources = resources;
        this.iplantDisplayStrings = iplantDisplayStrings;
        this.resources.css().ensureInjected();
    }

    @Override
    public String appDisabledClass() {
        return resources.css().appDisabled();
    }

    @Override
    public String appNameClass() {
        return resources.css().appName();
    }

    @Override
    public String appUnavailable() {
        return iplantDisplayStrings.appUnavailable();
    }

    @Override
    public void render(final SafeHtmlBuilder sb,
                       App value, final String textClassName,
                       final SafeHtml safeHtmlName,
                       final String textToolTip,
                       final String debugId) {
        sb.appendHtmlConstant("&nbsp;");
        if(DebugInfo.isDebugIdEnabled()
               && !Strings.isNullOrEmpty(debugId)){
            sb.append(templates.debugCell(textClassName, safeHtmlName, textToolTip, ELEMENT_NAME, debugId));
        }else {
            sb.append(templates.cell(textClassName, safeHtmlName, textToolTip, ELEMENT_NAME));
        }
    }

    @Override
    public String run() {
        return iplantDisplayStrings.run();
    }
}
