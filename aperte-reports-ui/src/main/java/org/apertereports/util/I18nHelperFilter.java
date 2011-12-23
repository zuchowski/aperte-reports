package org.apertereports.util;

import javax.portlet.*;
import javax.portlet.filter.*;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 *
 * @author tlipski@bluesoft.net.pl
 */
public class I18nHelperFilter implements ActionFilter, RenderFilter, ResourceFilter, EventFilter {

    @Override
    public void doFilter(ActionRequest actionRequest, ActionResponse actionResponse, FilterChain filterChain) throws IOException, PortletException {
        try {
            VaadinUtil.setThreadLocale(actionRequest.getLocale());
            filterChain.doFilter(actionRequest, actionResponse);
        }
        finally {
            VaadinUtil.unsetThreadLocale();
        }
    }

    @Override
    public void doFilter(EventRequest eventRequest, EventResponse eventResponse, FilterChain filterChain) throws IOException, PortletException {
        try {
            VaadinUtil.setThreadLocale(eventRequest.getLocale());
            filterChain.doFilter(eventRequest, eventResponse);
        }
        finally {
            VaadinUtil.unsetThreadLocale();
        }

    }

    @Override
    public void doFilter(RenderRequest renderRequest, RenderResponse renderResponse, FilterChain filterChain) throws IOException, PortletException {
        try {
            VaadinUtil.setThreadLocale(renderRequest.getLocale());
            filterChain.doFilter(renderRequest, renderResponse);
        }
        finally {
            VaadinUtil.unsetThreadLocale();
        }

    }

    @Override
    public void doFilter(ResourceRequest resourceRequest, ResourceResponse resourceResponse, FilterChain filterChain) throws IOException, PortletException {
        try {
            VaadinUtil.setThreadLocale(resourceRequest.getLocale());
            filterChain.doFilter(resourceRequest, resourceResponse);
        }
        finally {
            VaadinUtil.unsetThreadLocale();
        }

    }

    @Override
    public void init(FilterConfig filterConfig) throws PortletException {
        //nothing
    }

    @Override
    public void destroy() {
        //nothing
    }
}
