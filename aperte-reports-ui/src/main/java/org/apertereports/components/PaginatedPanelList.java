package org.apertereports.components;

import java.util.Collection;

import org.apertereports.util.ComponentFactory;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;
import org.apertereports.ui.UiFactory;
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
public abstract class PaginatedPanelList<O, W extends Panel> extends VerticalLayout {

    private String filter;
    private int pageSize;
    private int listSize;
    private int pageNumber;

    public PaginatedPanelList(int pageSize) {
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
        HorizontalLayout hl = UiFactory.createHLayout(this);
        hl.setMargin(true, false, false, false);
        if (hasPrevious()) {
            UiFactory.createButton(UiIds.LABEL_PREVIOUS, hl, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    previousPage();
                }
            });
        } else {
            UiFactory.createLabel(UiIds.LABEL_PREVIOUS, hl);
        }
        for (int i = 0; i < countPages(); i++) {
            final int pageIndex = i;
            String caption = "" + (pageIndex + 1);
            if (pageIndex == pageNumber) {
                UiFactory.createLabel(caption, hl);
            } else {
                UiFactory.createButton(caption, hl, BaseTheme.BUTTON_LINK, new ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        showPage(pageIndex);
                    }
                });
            }
        }
        if (hasNext()) {
            UiFactory.createButton(UiIds.LABEL_NEXT, hl, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    nextPage();
                }
            });
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
