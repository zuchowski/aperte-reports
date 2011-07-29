package pl.net.bluesoft.rnd.apertereports;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomComponent;
import eu.livotov.tpt.gui.widgets.TPTLazyLoadingLayout;
import pl.net.bluesoft.rnd.apertereports.common.utils.ExceptionUtils;
import pl.net.bluesoft.rnd.apertereports.util.NotificationUtil;
import pl.net.bluesoft.rnd.apertereports.util.VaadinUtil;

/**
 * A Vaadin component wrapper with lazy loading. Extending class should provide its heavy logic (i.e. fetching data from db)
 * in {@link #lazyLoad()} method.
 */
public abstract class AbstractLazyLoaderComponent extends CustomComponent implements TPTLazyLoadingLayout.LazyLoader {
    public abstract void lazyLoad() throws Exception;

    /**
     * Executes the lazy loading of the component.
     *
     * @param tptLazyLoadingLayout The TPT wrapper for lazy loading.
     * @return Returns the proper lazy loaded component.
     */
    @Override
    public Component lazyLoad(TPTLazyLoadingLayout tptLazyLoadingLayout) {
        try {
            lazyLoad();
        }
        catch (Exception e) {
            ExceptionUtils.logSevereException(e);
            NotificationUtil.showExceptionNotification(getWindow(), VaadinUtil.getValue("exception.gui.error"));
        }
        return this;
    }

    /**
     * Returns the message shown on lazy load (i.e. "The data is still loading...").
     *
     * @return The message
     */
    @Override
    public String getLazyLoadingMessage() {
        return VaadinUtil.getValue("loading.data");
    }
}


