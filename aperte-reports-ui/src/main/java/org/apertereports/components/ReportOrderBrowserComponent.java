package org.apertereports.components;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.apertereports.common.exception.AperteReportsRuntimeException;
import org.apertereports.common.utils.ReportGeneratorUtils;
import org.apertereports.common.xml.config.XmlReportConfigLoader;
import org.apertereports.dao.ReportOrderDAO;
import org.apertereports.model.ReportOrder;
import org.apertereports.model.ReportOrder.Status;
import org.apertereports.model.ReportTemplate;
import org.apertereports.util.ComponentFactory;
import org.apertereports.util.FileStreamer;

import com.vaadin.data.util.BeanItem;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.data.util.PropertysetItem;
import com.vaadin.event.FieldEvents.TextChangeEvent;
import com.vaadin.event.FieldEvents.TextChangeListener;
import com.vaadin.ui.*;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.themes.BaseTheme;
import org.apertereports.common.users.User;
import org.apertereports.ui.UiFactory;
import org.apertereports.ui.UiFactory.FAction;
import org.apertereports.ui.UiFactoryExt;
import org.apertereports.ui.UiIds;

@SuppressWarnings("serial")
public class ReportOrderBrowserComponent extends Panel {

    private static final int PAGE_SIZE = 10;
    private static final String COMPONENT_STYLE = "borderless light";
    private static final String CREATE_DATE = "createDate";
    private static final String REPORTNAME_STYLE = "h4";
    private static final String REPORTNAME = "reportname";
    private static final String REPORT_STATUS = "reportStatus";
    private PaginatedPanelList<ReportOrder, ReportOrderPanel> list;
    private User user;

    public ReportOrderBrowserComponent() {
    }

    public void initData(User user) {
        this.user = user;
        list.filter(null);
    }

    @Override
    public void attach() {
        super.attach();
        init();
    }

    private void init() {
        HorizontalLayout header = UiFactory.createHLayout(this, FAction.SET_FULL_WIDTH);

        TextField filterField = UiFactory.createSearchBox(UiIds.LABEL_FILTER, header, new TextChangeListener() {

            @Override
            public void textChange(TextChangeEvent event) {
                list.filter(event.getText());
            }
        });
        filterField.setWidth("150px");

        UiFactory.createSpacer(header, "10px", null);
        UiFactory.createSpacer(header, FAction.SET_FULL_WIDTH, FAction.SET_EXPAND_RATIO_1_0);

        UiFactory.createButton(UiIds.LABEL_REFRESH, header, new ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                list.refresh();
            }
        });

        list = new PaginatedPanelList<ReportOrder, ReportOrderBrowserComponent.ReportOrderPanel>(PAGE_SIZE) {

            @Override
            protected ReportOrderPanel transform(ReportOrder object) {
                return new ReportOrderPanel(object);
            }

            @Override
            protected int getListSize(String filter) {
                return ReportOrderDAO.count(user, filter);
            }

            @Override
            protected Collection<ReportOrder> fetch(String filter, int firstResult, int maxResults) {
                return ReportOrderDAO.fetch(user, filter, firstResult, maxResults);
            }
        };

        list.setMargin(true, false, false, false);
        addComponent(list);
        list.filter(null);
        setStyleName(COMPONENT_STYLE);
    }

    private class ReportOrderPanel extends Panel {

        private ReportOrder order;
        private ReportOrderParamsPanel params;
        private boolean paramsVisible;

        public ReportOrderPanel(ReportOrder order) {
            this.order = order;
        }

        @Override
        public void attach() {
            super.attach();
            init();
        }

        private void init() {

            setStyleName(COMPONENT_STYLE);
            ((AbstractLayout) getContent()).setMargin(false, false, false, false);
            BeanItem<ReportOrder> item = new BeanItem<ReportOrder>(order);
            GridLayout grid = new GridLayout(6, 1);
            grid.setWidth("100%");
            grid.setSpacing(true);
            grid.setColumnExpandRatio(1, 1);
            addComponent(grid);
            ComponentFactory.createIcon(item, REPORT_STATUS, grid);
            UiFactory.createLabel(new BeanItem<ReportTemplate>(order.getReport()), REPORTNAME, grid, REPORTNAME_STYLE);

            UiFactoryExt.createCalendarLabel(item, CREATE_DATE, grid);
            Button previewButton = UiFactory.createButton(UiIds.LABEL_PREVIEW, grid, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    showReport();
                }
            });
            UiFactory.createButton(UiIds.LABEL_PARAMETERS, grid, BaseTheme.BUTTON_LINK, new ClickListener() {

                @Override
                public void buttonClick(ClickEvent event) {
                    toggleParams();
                }
            });

            if (order.getReportStatus() != Status.SUCCEEDED) {
                previewButton.setEnabled(false);
            }

            params = new ReportOrderParamsPanel(order.getParametersXml());
        }

        private void showReport() {
            byte[] reportData;
            try {
                reportData = ReportGeneratorUtils.decodeContent(this.order.getReportResult());
            } catch (UnsupportedEncodingException e) {
                throw new AperteReportsRuntimeException(e);
            }
            FileStreamer.showFile(getApplication(), this.order.getReport().getReportname(), reportData,
                    this.order.getOutputFormat());
        }

        private void toggleParams() {
            paramsVisible = !paramsVisible;
            if (paramsVisible) {
                addComponent(params);
            } else {
                removeComponent(params);
            }
        }
    }

    private class ReportOrderParamsPanel extends Panel {

        public ReportOrderParamsPanel(String paramsXml) {
            Map<String, String> params = XmlReportConfigLoader.getInstance().xmlAsMap(paramsXml);
            List<String> sortedParamNames = new ArrayList<String>(params.keySet());
            Collections.sort(sortedParamNames);
            PropertysetItem item = new PropertysetItem();
            for (String string : sortedParamNames) {
                item.addItemProperty(string, new ObjectProperty<String>(params.get(string)));
            }
            Form form = new Form();
            form.setItemDataSource(item);
            form.setReadOnly(true);
            addComponent(form);
            form.getLayout().setMargin(false);
            ((AbstractLayout) getContent()).setMargin(false, true, false, true);
        }
    }
}
