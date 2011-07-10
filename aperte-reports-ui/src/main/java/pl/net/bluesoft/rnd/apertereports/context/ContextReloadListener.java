package pl.net.bluesoft.rnd.apertereports.context;

import com.vaadin.terminal.gwt.server.PortletApplicationContext2;
import com.vaadin.ui.Window;

import javax.portlet.*;

/**
 * A decorator for the {@link ContextReloadHandler} using a Liferay listener.
 * Handles the transformation of the request parameters to a {@link PortletContextHolder} instance.
 */
public class ContextReloadListener implements PortletApplicationContext2.PortletListener {
    /**
     * Context reload delegate.
     */
    private ContextReloadHandler handler;

    public ContextReloadListener(ContextReloadHandler handler) {
        this.handler = handler;
    }

    /**
     * Handles Liferay render request.
     *
     * @param request  The request
     * @param response The response
     * @param window   Vaadin window
     * @see PortletApplicationContext2.PortletListener
     */
    @Override
    public void handleRenderRequest(RenderRequest request, RenderResponse response, Window window) {
        handler.render(new PortletContextHolder(request.getWindowID(), request.getPortletMode(), request.getPreferences(), request.getPortletSession()));
    }

    /**
     * Handles Liferay action request. Does nothing at the moment.
     *
     * @param request  The request
     * @param response The response
     * @param window   Vaadin window
     * @see PortletApplicationContext2.PortletListener
     */
    @Override
    public void handleActionRequest(ActionRequest request, ActionResponse response, Window window) {
        // do nothing
    }

    /**
     * Handles Liferay event request. Does nothing at the moment.
     *
     * @param request  The request
     * @param response The response
     * @param window   Vaadin window
     * @see PortletApplicationContext2.PortletListener
     */
    @Override
    public void handleEventRequest(EventRequest request, EventResponse response, Window window) {
        // do nothing
    }

    /**
     * Handles Liferay resource request.
     *
     * @param request  The request
     * @param response The response
     * @param window   Vaadin window
     * @see PortletApplicationContext2.PortletListener
     */
    @Override
    public void handleResourceRequest(ResourceRequest request, ResourceResponse response, Window window) {
        handler.resource(new PortletContextHolder(request.getWindowID(), request.getPortletMode(), request.getPreferences(), request.getPortletSession()));
    }
}
