package org.apertereports.components;

import java.util.Collection;

import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import org.apertereports.ui.UiFactory;
import org.apertereports.ui.UiFactory.FAction;
import org.apertereports.ui.UiIds;

/**
 *
 * @author Zbigniew Malinowski
 *
 * Component paginating GUI lists.
 *
 * @param <O> datamodel backing class
 * @param <W> class used to display properties of backing class objects in GUI
 */
@SuppressWarnings("serial")
public abstract class CssPaginatedPanelList<O, W extends Panel> extends CssLayout {

    private String filter;
    private int pageSize;
    private int listSize;
    private int pageNumber;

    public CssPaginatedPanelList(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * Fetches filtered list's fragment.
     *
     * @return
     */
    protected abstract Collection<O> fetch(String filter, int firstResult, int maxResults);

    /**
     * Creates GUI component displaying backing object's properties.
     *
     * @param object backing object
     * @return GUI component
     */
    protected abstract W transform(O object);

    /**
     * Counts filtered list's size.
     *
     * @return
     */
    protected abstract int getListSize(String filter);

    public void filter(String filter) {
        this.filter = filter;
        pageNumber = 0;
        refresh(true);
    }

    public void refresh() {
        refresh(true);
    }

    private void refresh(boolean getListSize) {
        if (getListSize) {
            listSize = getListSize(filter);
        }
        Collection<O> list = fetch(filter, calculateFirstResult(), pageSize);
        removeAllComponents();
        for (O o : list) {
            addComponent(transform(o));
        }
        if (listSize > pageSize) {
            addFooter();
        }
    }

    private int calculateFirstResult() {
        return pageNumber * pageSize;
    }

    private void addFooter() {
        HorizontalLayout hl = UiFactory.createHLayout(this, FAction.SET_SPACING);
        hl.setMargin(true, false, false, false);
        if (hasPrevious()) {
        	Button tmpButton = UiFactory.createButton(UiIds.LABEL_PREVIOUS, hl, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    previousPage();
                }
            });
        	tmpButton.addStyleName("btn");
        } else {
            UiFactory.createLabel(UiIds.LABEL_PREVIOUS, hl);
        }
        for (int i = 0; i < countPages(); i++) {
            final int pageIndex = i;
            String caption = "" + (pageIndex + 1);
            if (pageIndex == pageNumber) {
                UiFactory.createLabel(caption, hl);
            } else {
            	Button tmpButton2 = UiFactory.createButton(caption, hl, BaseTheme.BUTTON_LINK, new ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        showPage(pageIndex);
                    }
                });
            	tmpButton2.addStyleName("btn");
            }
        }
        if (hasNext()) {
        	Button tmpButton3 = UiFactory.createButton(UiIds.LABEL_NEXT, hl, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    nextPage();
                }
            });
        	tmpButton3.addStyleName("btn");
        } else {
            UiFactory.createLabel(UiIds.LABEL_NEXT, hl);
        }
    }

    private void showPage(int pageNumber) {
        if (hasPage(pageNumber)) {
            this.pageNumber = pageNumber;
            refresh(false);
        }
    }

    private void nextPage() {
        if (hasNext()) {
            pageNumber++;
        }
        refresh(false);
    }

    private void previousPage() {
        if (hasPrevious()) {
            pageNumber--;
        }
        refresh(false);
    }

    private int countPages() {

        int count = listSize / pageSize + 1;
        if (listSize % pageSize == 0) {
            count--;
        }
        return count;
    }

    private boolean hasPage(int pageNumber) {
        return pageNumber >= 0 && pageNumber < countPages();
    }

    private boolean hasPrevious() {
        return pageNumber > 0;
    }

    private boolean hasNext() {
        return pageNumber < countPages() - 1;
    }
}
