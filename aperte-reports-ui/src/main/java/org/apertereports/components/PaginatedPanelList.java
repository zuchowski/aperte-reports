package org.apertereports.components;

import java.util.Collection;

import org.apertereports.util.ComponentFactory;

import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.BaseTheme;

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

    private static final String PAGINATED_LIST_BUTTON_NEXT = "paginated-list.button.next";
    private static final String PAGINATED_LIST_BUTTON_PREVIOUS = "paginated-list.button.previous";
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
        listSize = getListSize(filter);
        refresh();
    }

    public void refresh() {

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
        HorizontalLayout hl = ComponentFactory.createHLayout(this);
        hl.setMargin(true, false, false, false);
        if (hasPrevious()) {
            ComponentFactory.createButton(PAGINATED_LIST_BUTTON_PREVIOUS, BaseTheme.BUTTON_LINK, hl,
                    new ClickListener() {

                        @Override
                        public void buttonClick(ClickEvent event) {
                            previousPage();
                        }
                    });
        } else {
            ComponentFactory.createSimpleLabel(PAGINATED_LIST_BUTTON_PREVIOUS, "", hl);
        }
        for (int i = 0; i < countPages(); i++) {
            final int pageIndex = i;
            String caption = "" + (pageIndex + 1);
            if (pageIndex == pageNumber) {
                ComponentFactory.createSimpleLabel(caption, "", hl);
            } else {
                ComponentFactory.createButton(caption, BaseTheme.BUTTON_LINK, hl, new ClickListener() {

                    @Override
                    public void buttonClick(ClickEvent event) {
                        showPage(pageIndex);
                    }
                });
            }
        }
        if (hasNext()) {
            ComponentFactory.createButton(PAGINATED_LIST_BUTTON_NEXT, BaseTheme.BUTTON_LINK, hl, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    nextPage();
                }
            });
        } else {
            ComponentFactory.createSimpleLabel(PAGINATED_LIST_BUTTON_NEXT, "", hl);
        }

    }

    private void showPage(int pageNumber) {
        if (hasPage(pageNumber)) {
            this.pageNumber = pageNumber;
            refresh();
        }
    }

    private void nextPage() {
        if (hasNext()) {
            pageNumber++;
        }
        refresh();
    }

    private void previousPage() {
        if (hasPrevious()) {
            pageNumber--;
        }
        refresh();
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
