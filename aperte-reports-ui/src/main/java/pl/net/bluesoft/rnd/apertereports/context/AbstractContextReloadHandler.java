package pl.net.bluesoft.rnd.apertereports.context;

import javax.portlet.PortletMode;

/**
 * Provides a simple vaadin layer for the Liferay portlet requests.
 */
public abstract class AbstractContextReloadHandler implements ContextReloadHandler {
    /**
     * Invoked on render help view request.
     *
     * @param holder A bean containing common context parameters.
     */
    abstract public void handleHelp(PortletContextHolder holder);

    /**
     * Invoked on render edit view request.
     *
     * @param holder A bean containing common context parameters.
     */
    abstract public void handleEdit(PortletContextHolder holder);

    /**
     * Invoked on render main view request.
     *
     * @param holder A bean containing common context parameters.
     */
    abstract public void handleView(PortletContextHolder holder);

    /**
     * Invoked on get resource request.
     *
     * @param holder A bean containing common context parameters.
     */
    abstract public void handleResource(PortletContextHolder holder);

    /**
     * Dispatches the rendering to a relevant method.
     *
     * @param holder A bean with common context parameters.
     */
    @Override
    public void render(PortletContextHolder holder) {
        if (PortletMode.EDIT.equals(holder.getMode())) {
            handleEdit(holder);
        }
        else if (PortletMode.VIEW.equals(holder.getMode())) {
            handleView(holder);
        }
        else if (PortletMode.HELP.equals(holder.getMode())) {
            handleHelp(holder);
        }
    }

    /**
     * Requests a resource.
     *
     * @param holder A bean with common context parameters.
     */
    @Override
    public void resource(PortletContextHolder holder) {
        handleResource(holder);
    }
}
