package pl.net.bluesoft.rnd.vries.context;

/**
 * Liferay context reload handler.
 */
public interface ContextReloadHandler {
    /**
     * Invoked on resource request.
     *
     * @param holder A bean with common context parameters.
     */
    void resource(PortletContextHolder holder);

    /**
     * Invoked on render request.
     *
     * @param holder A bean with common context parameters.
     */
    void render(PortletContextHolder holder);
}
