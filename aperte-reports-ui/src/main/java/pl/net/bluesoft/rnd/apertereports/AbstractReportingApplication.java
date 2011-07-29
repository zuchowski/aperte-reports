package pl.net.bluesoft.rnd.apertereports;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.util.PortalUtil;
import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.ui.Window;
import eu.livotov.tpt.TPTApplication;
import eu.livotov.tpt.i18n.TM;
import pl.net.bluesoft.rnd.apertereports.common.exception.VriesException;
import pl.net.bluesoft.rnd.apertereports.common.exception.VriesRuntimeException;
import pl.net.bluesoft.rnd.apertereports.common.utils.ExceptionUtils;
import pl.net.bluesoft.rnd.apertereports.util.NotificationUtil;

import javax.portlet.*;
import java.util.Locale;

/**
 * This is a stub abstract class for all application portlets. Extending classes should initialize themselves
 * by overriding {@link #portletInit()}.
 */
public abstract class AbstractReportingApplication extends TPTApplication implements
        PortletApplicationContext2.PortletListener {
    /**
     * Application theme name.
     */
//    private static final String VRIES_THEME = "apertereports-chameleon";

    /**
     * Liferay user.
     */
    protected User user;
    /**
     * User locale.
     */
    protected Locale locale;

    /**
     * Initializes the application context.
     */
    @Override
    public void applicationInit() {
        if (getContext() instanceof PortletApplicationContext2) {
            ((PortletApplicationContext2) getContext()).removePortletListener(this, this);
            ((PortletApplicationContext2) getContext()).addPortletListener(this, this);
        }

        TM.getDictionary().setDefaultLanguage(getLocale().getLanguage());
//        setTheme(VRIES_THEME);
        reloadDictionary();
        portletInit();
    }

    /**
     * Initializes the portlet GUI.
     */
    protected abstract void portletInit();

    /**
     * This method should be overriden to implement a custom behavior on a first application startup.
     */
    @Override
    public void firstApplicationStartup() {
        // override to implement a custom behavior
    }

    /**
     * Reloads the dictionary manually.
     */
    private void reloadDictionary() {
//        File themeFolder = new File(getContext().getBaseDirectory(), String.format("VAADIN/themes/%s", VRIES_THEME));
//        if (themeFolder.exists() && themeFolder.isDirectory()) {
//            try {
//                TM.getDictionary().loadTranslationFilesFromThemeFolder(themeFolder);
//            }
//            catch (IOException e) {
//                ExceptionUtils.logSevereException(e);
//            }
//        }
    }

    /**
     * Logs uncaught exceptions to logger and presents them to the user.
     *
     * @param event Error event
     */
    @Override
    public void terminalError(com.vaadin.terminal.Terminal.ErrorEvent event) {
        Throwable throwable = event.getThrowable();
        if (throwable instanceof Exception) {
            ExceptionUtils.logSevereException((Exception) throwable);
        }
        if (throwable instanceof VriesRuntimeException) {
            VriesRuntimeException vre = (VriesRuntimeException) throwable;
            NotificationUtil.showExceptionNotification(getMainWindow(),
                    vre.getVriesErrorPrefix() != null ? vre.getVriesErrorPrefix() : "exception.gui.error", vre);
        }
        else if (throwable instanceof VriesException) {
            NotificationUtil.showExceptionNotification(getMainWindow(), (VriesException) throwable);
        }
        else {
            super.terminalError(event);
        }
    }

    /**
     * Provides information about user and the locale for the application.
     *
     * @param request  Render request
     * @param response Render response
     * @param window   Vaadin window
     */
    @Override
    public void handleRenderRequest(RenderRequest request, RenderResponse response, Window window) {
        if (getContext() instanceof PortletApplicationContext2) {
            try {
                user = PortalUtil.getUser(request);
                locale = PortalUtil.getLocale(request);
            }
            catch (PortalException e) {
                ExceptionUtils.logSevereException(e);
            }
            catch (SystemException e) {
                ExceptionUtils.logSevereException(e);
            }
        }
    }

    /**
     * Handles portlet action request however not used in the application.
     *
     * @param request  Render request
     * @param response Render response
     * @param window   Vaadin window
     */
    @Override
    public void handleActionRequest(ActionRequest request, ActionResponse response, Window window) {
        // do nothing
    }

    /**
     * Handles portlet event request however not used in the application.
     *
     * @param request  Render request
     * @param response Render response
     * @param window   Vaadin window
     */
    @Override
    public void handleEventRequest(EventRequest request, EventResponse response, Window window) {
        // do nothing
    }

    /**
     * Handles portlet resource request however not used in the application.
     *
     * @param request  Render request
     * @param response Render response
     * @param window   Vaadin window
     */
    @Override
    public void handleResourceRequest(ResourceRequest request, ResourceResponse response, Window window) {
        // do nothing
    }

    /**
     * Returns a liferay user of the last request.
     *
     * @return a user
     */
    public User getLiferayUser() {
        return user;
    }
}
