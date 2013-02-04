package org.apertereports.util;

import java.util.Arrays;
import java.util.Collection;

import org.apertereports.common.ReportConstants.ReportType;
import org.apertereports.dao.ReportTemplateDAO;
import org.apertereports.model.ReportTemplate;

import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.terminal.ClassResource;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.Embedded;
import org.apertereports.common.users.User;

/**
 * Factory class providing components in frequently used form.
 *
 * @author Zbigniew Malinowski
 *
 */
public abstract class ComponentFactory {

    private static final String ICON_PATH = "/icons/16x16/";

    public static ComboBox createFormatCombo(ReportType selectedValue, String captionKey) {
        Container all = new BeanItemContainer<String>(String.class, Arrays.asList(ReportType.stringValues()));
        ComboBox format = new ComboBox(VaadinUtil.getValue(captionKey),
                all);
        format.setValue(selectedValue.name());
        format.setStyleName("small");
        format.setNullSelectionAllowed(false);
        format.setTextInputAllowed(false);
        return format;
    }

    public static ComboBox createReportTemplateCombo(User user, ReportTemplate selectedValue, String captionKey) {

        Collection<ReportTemplate> allReports = ReportTemplateDAO.fetchActive(user);
        ComboBox reports = new ComboBox(VaadinUtil.getValue(captionKey),
                new BeanItemContainer<ReportTemplate>(ReportTemplate.class, allReports));
        reports.setItemCaptionPropertyId("reportname");
        reports.setValue(selectedValue);
        reports.setTextInputAllowed(false);
        reports.setNullSelectionAllowed(false);

        return reports;
    }

    public static Embedded createIcon(Item item, String proprtyId, ComponentContainer parent) {
        Enum<?> value = (Enum<?>) item.getItemProperty(proprtyId).getValue();
        Embedded icon = new Embedded(null, new ClassResource(AperteIcons.getIconUrl(value), parent.getApplication()));
        icon.setDescription(value.getClass().getSimpleName() + ": " + value.name());
        parent.addComponent(icon);
        return icon;

    }

    public enum AperteIcons {

        NEW,
        PROCESSING,
        FAILED,
        SUCCEEDED,
        ERROR,;

        public static String getIconUrl(Enum<?> e) {
            AperteIcons icon;
            try {
                icon = valueOf(e.name());
            } catch (IllegalArgumentException e1) {
                return ICON_PATH + ERROR.name().toLowerCase() + ".png";
            }
            return ICON_PATH + icon.name().toLowerCase() + ".png";
        }
    }
}