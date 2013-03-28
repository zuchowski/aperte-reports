package org.apertereports.components;

import static org.apertereports.common.ARConstants.InputTypes.CHECKBOXES;
import static org.apertereports.common.ARConstants.InputTypes.DATE;
import static org.apertereports.common.ARConstants.InputTypes.FILTER;
import static org.apertereports.common.ARConstants.InputTypes.FILTERED_SELECT;
import static org.apertereports.common.ARConstants.InputTypes.MULTISELECT;

import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import net.sf.jasperreports.engine.JRParameter;

import org.apache.commons.lang.StringUtils;
import org.apertereports.AbstractLazyLoaderComponent;
import org.apertereports.AbstractReportingApplication;
import org.apertereports.backbone.util.ReportTemplateProvider;
import org.apertereports.common.ARConstants;
import org.apertereports.common.ARConstants.ReportType;
import org.apertereports.common.exception.ARException;
import org.apertereports.common.utils.TextUtils;
import org.apertereports.common.utils.TimeUtils;
import org.apertereports.common.wrappers.DictionaryItem;
import org.apertereports.common.xml.config.ReportConfigParameter;
import org.apertereports.engine.ReportMaster;
import org.apertereports.engine.ReportParameter;
import org.apertereports.engine.ReportProperty;
import org.apertereports.util.ComponentFactory;
import org.apertereports.util.NotificationUtil;
import org.apertereports.util.VaadinUtil;
import org.apertereports.util.wrappers.DictionaryItemsWrapper;
import org.apertereports.util.wrappers.FieldContainer;
import org.apertereports.util.wrappers.FieldProperties;
import org.apertereports.util.wrappers.FilterContainer;

import com.vaadin.data.Buffered;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.AbstractSelect;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.DateField;
import com.vaadin.ui.Field;
import com.vaadin.ui.Form;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.OptionGroup;
import com.vaadin.ui.Select;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.TextField;
import java.util.Locale;
import org.apertereports.common.users.User;
import org.apertereports.common.utils.LocaleUtils;
import org.apertereports.ui.UiFactoryExt;
import org.apertereports.ui.UiIds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Displays report parameters taken from JRXML parameters section as Vaadin
 * fields in a form. Supports lazy loading.
 */
public class ReportParametersComponent extends AbstractLazyLoaderComponent {

    private Form form = new Form();
    private Map<String, FilterContainer> filters;
    private List<FieldContainer> fields = new LinkedList<FieldContainer>();
    private ComboBox format;
    private ComboBox localeComboBox;
    private ReportMaster reportMaster;
    private String reportSource;
    private Integer cacheId;
    private boolean includeReportFormat = true;
    private boolean readonly = false;
    private boolean includeLocale = true;
    private boolean viewInitialized = false;
    private final List<ReportConfigParameter> params;
    private static Logger logger = LoggerFactory.getLogger(ReportParametersComponent.class);

    public ReportParametersComponent(ReportMaster rm, boolean showFormat, List<ReportConfigParameter> params) throws ARException {
        this.reportMaster = rm;
        this.includeReportFormat = showFormat;
        if (params == null) {
            params = new LinkedList<ReportConfigParameter>();
        }
        this.params = params;

        init();
    }

//	public ReportParametersComponent(String reportSource, Integer cacheId, boolean lazyLoad) throws AperteReportsException {
//		this.reportSource = reportSource;
//		this.cacheId = cacheId;
//		if (!lazyLoad) {
//			reportMaster = new ReportMaster(reportSource, cacheId.toString(), new ReportTemplateProvider());
//			init();
//		}
//	}
//
//	public ReportParametersComponent(String reportSource, Integer cacheId, ReportConfig reportConfig,
//		boolean includeReportFormat, boolean lazyLoad) throws AperteReportsException {
//		this.reportSource = reportSource;
//		this.cacheId = cacheId;
//		this.reportParameters = reportConfig != null ? reportConfig.getParameters() : null;
//		this.includeReportFormat = includeReportFormat;
//		if (!lazyLoad) {
//			reportMaster = new ReportMaster(reportSource, cacheId.toString(), new ReportTemplateProvider());
//			init();
//		}
//	}
//
//	public ReportParametersComponent(String reportSource, Integer cacheId, List<ReportConfigParameter> reportParameters,
//		boolean includeReportFormat, boolean lazyLoad, boolean readonly) throws AperteReportsException {
//		this.reportSource = reportSource;
//		this.cacheId = cacheId;
//		this.reportParameters = reportParameters;
//		this.includeReportFormat = includeReportFormat;
//		this.readonly = readonly;
//		if (!lazyLoad) {
//			reportMaster = new ReportMaster(reportSource, cacheId.toString(), new ReportTemplateProvider());
//			init();
//		}
//	}
//
//	public ReportParametersComponent(String reportSource, Integer cacheId, ReportConfig reportConfig,
//		boolean includeReportFormat, boolean lazyLoad, boolean readonly) throws AperteReportsException {
//		this.reportSource = reportSource;
//		this.cacheId = cacheId;
//		this.reportParameters = reportConfig != null ? reportConfig.getParameters() : null;
//		this.includeReportFormat = includeReportFormat;
//		this.readonly = readonly;
//		if (!lazyLoad) {
//			reportMaster = new ReportMaster(reportSource, cacheId.toString(), new ReportTemplateProvider());
//			init();
//		}
//	}
    /**
     * Returns a map of report parameters collected from the generated form
     * fields.
     *
     * @return A map of report parameters
     */
    public Map<String, String> collectParametersValues() {
        Map<String, String> parameters = new HashMap<String, String>(fields.size());
        for (FieldContainer field : fields) {
            if (field.getComponentType().equals(FILTER)) {
                continue;
            }
            Object rawValue = field.getValue();
            if (rawValue == null) {
                continue;
            }
            String value = TextUtils.encodeObjectToSQL(rawValue);
            parameters.put(field.getName(), value);
        }

        //xxxs move id to id, move management of user properties to base class (?)
        parameters.put("login", getLogin());
//        xxx: use property set underneath the form
        parameters.put(JRParameter.REPORT_LOCALE, localeComboBox.getValue() == null ? null : localeComboBox.getValue().toString());
        return parameters;

    }

    /**
     * Returns Liferay login of the currently logged user.
     *
     * @return A user login
     */
    //xxx user
    private String getLogin() {
        String login = "";
        try {
            User user = ((AbstractReportingApplication) getApplication()).getArUser();
            if (user == null) {
                return login;
            }
            login = user.getLogin();
        } catch (Exception e) {
            logger.warn(VaadinUtil.getValue("liferay.get.login.exception"), e);
            throw new RuntimeException(e);
        }
        if (login.contains("@")) {
            login = login.split("@", 2)[0];
        }
        return login;
    }

    /**
     * Returns selected report format.
     *
     * @return Format string
     */
    public String getSelectedFormat() {
        return format != null ? format.getValue().toString() : null;
    }

    /**
     * Lazily loads the data.
     *
     * @throws Exception on Jasper error
     */
    @Override
    public void lazyLoad() throws Exception {
        reportMaster = new ReportMaster(reportSource, cacheId.toString(), new ReportTemplateProvider());
        init();
    }

    /**
     * Validates all the fields in the form before commit.
     *
     * @return
     * <code>TRUE</code> if the form is valid
     */
    public boolean validateForm() {
        boolean result = true;
        for (FieldContainer field : fields) {
            field.validate();
        }
        try {
            form.commit();
        } catch (Validator.InvalidValueException e) {
            result = false;
        } catch (Buffered.SourceException e) {
            logger.error(e.getMessage(), e);
            result = false;
        }
        return result;
    }

    /**
     * Builds a new field container carrying the generated Vaadin field with all
     * its properties. Returns
     * <code>null</code> on {@link BuildingFailedException} which may occur on
     * special script invocation.
     *
     * @param param Report parameter
     * @param fieldProperties Field properties
     * @return A new field container
     */
    private FieldContainer buildField(ReportParameter param, FieldProperties fieldProperties) {
        try {
            FieldContainer container = new FieldContainer();
            List<DictionaryItem> items = null;

            // DICT_QUERY
            if (StringUtils.isNotEmpty(fieldProperties.getDictQuery())) {
                String login = getLogin();
                String dictQuery = fieldProperties.getDictQuery().replaceAll("\\$LOGIN", login);
                items = org.apertereports.dao.DictionaryDAO.fetchDictionary(dictQuery);
            }

            // DICT_ITEM_LIST
            if (StringUtils.isNotEmpty(fieldProperties.getDictItemList())) {
                items = org.apertereports.dao.DictionaryDAO.readDictionaryFromString(fieldProperties.getDictItemList());
            }

            // SPECIAL_DATA_QUERY_CODE
            items = executeSpecialDataQueryCode(fieldProperties, items);

            // INPUT_TYPE
            Field field = buildFieldBaseForInputType(fieldProperties, items, container);

            if (field != null) {
                applyAttributes(fieldProperties, container, field);
            }

            // obsluga kontrolek multi-level select: filter i filtered_select
            if (fieldProperties.getInputType() == ARConstants.InputTypes.FILTERED_SELECT
                    || fieldProperties.getInputType() == ARConstants.InputTypes.FILTER) {
                FilterContainer filterContainer = getFilterContainer(fieldProperties.getFilterGroup());
                filterContainer.addFilter((Select) field, fieldProperties.getLevel(), items);
                container.setFieldComponent(filterContainer);
            }

            // SPECIAL_VALIDATION_CODE
            attachSpecialValidators(fieldProperties, container);
            container.setComponentType(fieldProperties.getInputType());
            container.setOrder(fieldProperties.getOrder());
            container.setName(param.getName());
            return container;
        } catch (BuildingFailedException e) {
//            return null;
            throw new RuntimeException(e);
        }
    }

    /**
     * Applies view options for the generated field.
     *
     * @param fieldProperties Field properties
     * @param container Field container
     * @param field Field the attributes are applied to
     */
    protected void applyAttributes(FieldProperties fieldProperties, FieldContainer container, Field field) {
        container.setFieldComponent(field);

        // WIDTH
        if (StringUtils.isNotEmpty(fieldProperties.getWidth())) {
            field.setWidth(StringUtils.lowerCase(fieldProperties.getWidth()));
        }

        // SELECT_ALL
        container.setSelectAll(fieldProperties.getSelectAll());

        //            
        // VALIDATORS
        //            

        // REQUIRED
        if (fieldProperties.isRequired()) {
            field.setRequired(fieldProperties.isRequired());
            field.setRequiredError(VaadinUtil.getValue(fieldProperties.getRequiredError(), fieldProperties.getCaption()));
        }

        // REGEXP
        if (StringUtils.isNotEmpty(fieldProperties.getRegexp())) {
            field.addValidator(new RegexpValidator(fieldProperties.getRegexp(), VaadinUtil.getValue(
                    fieldProperties.getRegexpError(), fieldProperties.getCaption(), fieldProperties.getRegexp())));
        }

        // MAXCHARS
        if (StringUtils.isNotEmpty(fieldProperties.getMaxchars()) && field instanceof TextField) {
            try {
                ((TextField) field).setMaxLength(Integer.valueOf(fieldProperties.getMaxchars()));
            } catch (NumberFormatException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Adds special validators using the invokable field property.
     *
     * @param fieldProperties Field properties
     * @param container Field container
     * @throws BuildingFailedException on script invocation error
     */
    protected void attachSpecialValidators(FieldProperties fieldProperties, FieldContainer container) throws BuildingFailedException {
        if (fieldProperties.getSei() != null && StringUtils.isNotEmpty(fieldProperties.getSpecialValidationCode())) {
            try {
                fieldProperties.getSe().eval(fieldProperties.getSpecialValidationCode());
                Validator validator = (Validator) fieldProperties.getSei().invokeFunction("specialValidator",
                        container, fieldProperties.getSpecialValidationError());
                container.addValidator(validator);
            } catch (ScriptException e) {
                NotificationUtil.showExceptionNotification(getWindow(),
                        "invoker.form.special_validation_code.script_exception", e);
                logger.error(e.getMessage(), e);
                throw new BuildingFailedException();
            } catch (NoSuchMethodException e) {
                NotificationUtil.showExceptionNotification(getWindow(),
                        "invoker.form.special_validation_code.no_such_method_exception", e);
                logger.error(e.getMessage(), e);
                throw new BuildingFailedException();
            }
        }
    }

    /**
     * Build a base Vaadin component for input field properties.
     *
     * @param fieldProperties Field properties
     * @param items Dictionary items
     * @param container Field container
     * @return A newly created Vaadin field
     * @throws BuildingFailedException on special control building error
     */
    protected Field buildFieldBaseForInputType(FieldProperties fieldProperties, List<DictionaryItem> items,
            FieldContainer container) throws BuildingFailedException {
        Field field = null;
        switch (fieldProperties.getInputType()) {
            case CHECKBOX:
                field = new CheckBox(fieldProperties.getCaption());
                break;
            case DATE:
                DateField dateField = new DateField(fieldProperties.getCaption()) {

                    @Override
                    protected Date handleUnparsableDateString(String dateString) throws ConversionException {
                        try {
                            return super.handleUnparsableDateString(dateString);
                        } catch (ConversionException e) {
                            throw new ConversionException(VaadinUtil.getValue("form.errors.unparsable_date", getCaption(), dateString));
                        }
                    }
                };
                dateField.setDateFormat(TimeUtils.getDefaultDateFormat());
                dateField.setResolution(DateField.RESOLUTION_MIN);
                field = dateField;
                break;
            case TEXTAREA:
                TextArea text = new TextArea(fieldProperties.getCaption());
                text.setRows(3);
                field = text;
                break;
            case MULTISELECT:
            case SELECT:
                Select select = new Select(fieldProperties.getCaption());
                select.setImmediate(true);
                try {
                    for (DictionaryItem item : items) {
                        select.addItem(item.getCode());
                        select.setItemCaption(item.getCode(), item.getDescription());
                    }
                } catch (NullPointerException e) {
                    NotificationUtil.showErrorNotification(getWindow(),
                            VaadinUtil.getValue("invoker.form.no_items_for_exception") + " " + fieldProperties.getCaption());
                    throw new BuildingFailedException();
                }

                select.setMultiSelect(fieldProperties.getInputType() == ARConstants.InputTypes.MULTISELECT);
                select.setFilteringMode(AbstractSelect.Filtering.FILTERINGMODE_CONTAINS);
                field = select;
                break;

            case CHECKBOXES:
            case RADIOBUTTONS:
                OptionGroup optionGroup = new OptionGroup(fieldProperties.getCaption());
                try {
                    for (DictionaryItem item : items) {
                        optionGroup.addItem(item.getCode());
                        optionGroup.setItemCaption(item.getCode(), item.getDescription());
                    }
                } catch (NullPointerException e) {
                    NotificationUtil.showErrorNotification(getWindow(),
                            VaadinUtil.getValue("invoker.form.no_items_for_exception") + " " + fieldProperties.getCaption());
                    throw new BuildingFailedException();
                }
                optionGroup.setMultiSelect(fieldProperties.getInputType() == CHECKBOXES);
                field = optionGroup;
                break;
            case FILTER:
            case FILTERED_SELECT:
                Select filterSelect = new Select(fieldProperties.getCaption());
                filterSelect.setImmediate(true);
                filterSelect.setMultiSelect(fieldProperties.isMultipleChoice());
                field = filterSelect;
                break;
            case SPECIAL_CONTROL:
                // SPECIAL_CONTROL_CODE
                if (fieldProperties.getSei() != null && StringUtils.isNotEmpty(fieldProperties.getSpecialControlCode())) {
                    try {
                        fieldProperties.getSe().eval(fieldProperties.getSpecialControlCode());
                        fieldProperties.getSei().invokeFunction("specialControl", container, items,
                                fieldProperties.getWidth(), fieldProperties.getMaxchars(), fieldProperties.isRequired(),
                                fieldProperties.getRequiredError(), fieldProperties.getRegexp(),
                                fieldProperties.getRegexpError(), fieldProperties.getCaption());
                    } catch (ScriptException e) {
                        NotificationUtil.showExceptionNotification(getWindow(),
                                "invoker.form.special_control_code.script_exception", e);
                        logger.error(e.getMessage(), e);
                        throw new BuildingFailedException();
                    } catch (NoSuchMethodException e) {
                        NotificationUtil.showExceptionNotification(getWindow(),
                                "invoker.form.special_control_code.no_such_method_exception", e);
                        logger.error(e.getMessage(), e);
                        throw new BuildingFailedException();
                    }
                }
                Component component = container.getFieldComponent();
                if (component instanceof Field) {
                    field = (Field) component;
                }
                break;

            case TEXT:
            default:
                field = new TextField(fieldProperties.getCaption());
                break;
        }
        return field;
    }

    /**
     * Invokes a special script query using the invokable field property. If the
     * invokable property is not present a default list is returned.
     *
     * @param fieldProperties Field properties
     * @param items A default list of items
     * @return Updated list of items
     * @throws BuildingFailedException on script invocation error
     */
    public List<DictionaryItem> executeSpecialDataQueryCode(FieldProperties fieldProperties,
            List<DictionaryItem> items) throws BuildingFailedException {
        if (fieldProperties.getSei() != null && StringUtils.isNotEmpty(fieldProperties.getSpecialDataQueryCode())) {
            try {
                DictionaryItemsWrapper itemsWrapper = new DictionaryItemsWrapper();
                fieldProperties.getSe().eval(fieldProperties.getSpecialDataQueryCode());
                fieldProperties.getSei().invokeFunction("specialQuery", itemsWrapper, fieldProperties.getDictQuery());
                items = itemsWrapper.getItems();
            } catch (ScriptException e) {
                logger.error(e.getMessage(), e);
                NotificationUtil.showExceptionNotification(getWindow(),
                        "invoker.form.special_data_query_code.script_exception", e);
                throw new BuildingFailedException();
            } catch (NoSuchMethodException e) {
                logger.error(e.getMessage(), e);
                NotificationUtil.showExceptionNotification(getWindow(),
                        "invoker.form.special_data_query_code.no_such_method_exception", e);
                throw new BuildingFailedException();
            }
        }
        return items;
    }

    /**
     * Creates a new filter container for specified filter identifier.
     *
     * @param filterId Input id
     * @return A filter container
     */
    private FilterContainer getFilterContainer(String filterId) {
        if (filters == null) {
            filters = new HashMap<String, FilterContainer>();
        }
        if (!filters.containsKey(filterId)) {
            filters.put(filterId, new FilterContainer());
        }
        return filters.get(filterId);
    }

    /**
     * Retrieves the value from a report parameter properties. If the input type
     * cannot be determined a default value is returned.
     *
     * @param props Input parameter properties
     * @param key Search key type
     * @param inputTypes Possible input types
     * @param defaultValue Default value
     * @return Parameter value
     */
    private String getValueFromMap(Map<ARConstants.Keys, ReportProperty> props, ARConstants.Keys key, Enum<?>[] inputTypes,
            String defaultValue) {
        if (props.containsKey(key)) {
            String value = props.get(key).getValue();
            if (inputTypes == null) {
                return value;
            }
            value = StringUtils.upperCase(value);
            for (Enum<?> enum1 : inputTypes) {
                if (enum1.name().equals(value)) {
                    return value;
                }
            }
        }
        return defaultValue;
    }

    /**
     * Sets the build form as the composition root.
     */
    private void init() {
        setCompositionRoot(form);
        form.setReadOnly(readonly);
    }

    /**
     * Loads data to generated Vaadin fields from the XML stored elsewhere.
     */
    private void initFieldsFromConfig() {

        for (ReportConfigParameter param : params) {

            //xxx maybe it could be easier to have linked map as parameters instead of list
            if (param.getName().equals(JRParameter.REPORT_LOCALE)) {
                if (localeComboBox != null) {
                    Locale locale = LocaleUtils.createLocale(param.getValue());
                    localeComboBox.setValue(locale);
                }
                continue;
            }

            for (FieldContainer field : fields) {
                if (param.getName().equals(field.getName())) {
                    try {
                        Field fieldComponent = field.getFieldComponent() instanceof Field ? (Field) field.getFieldComponent() : null;
                        Class<?> fieldType = fieldComponent == null ? null
                                : fieldComponent.getPropertyDataSource() == null ? null
                                : fieldComponent.getPropertyDataSource().getType();
                        if (DATE.equals(field.getComponentType())) {
                            fieldType = Date.class;
                        } else if (MULTISELECT.equals(field.getComponentType())
                                || CHECKBOXES.equals(field.getComponentType())
                                || FILTERED_SELECT.equals(field.getComponentType())) {
                            fieldType = Collection.class;
                        }
                        Object v = TextUtils.encodeSQLToObject(fieldType, param.getValue());
                        field.setValue(v);
                    } catch (ParseException e) {
                        logger.error(e.getMessage(), e);
                        NotificationUtil.showExceptionNotification(getWindow(), VaadinUtil.getValue("exception.gui.error"), e);
                        throw new RuntimeException(e);
                    }
                    break;
                }
            }
        }
    }

    /**
     * Creates a form with generated Vaadin fields.
     */
    private void initView() {
        //at the moment there is a chance that this method will be invoked twice per instance
        //we want only on initialization process of the view
        if (viewInitialized) {
            return;
        }
        viewInitialized = true;

        List<ReportParameter> parameters = reportMaster.getParameters();
        for (ReportParameter param : parameters) {
            Map<ARConstants.Keys, ReportProperty> props = param.getProperties();
            if (props == null || props.isEmpty()) {
                continue;
            }
            FieldProperties fieldProperties = parseFieldProperties(param, props);
            FieldContainer container = buildField(param, fieldProperties);
            fields.add(container);
        }

        // ORDER FIELDS
        Collections.sort(fields, new Comparator<FieldContainer>() {

            @Override
            public int compare(FieldContainer o1, FieldContainer o2) {
                return o1.getOrder().compareTo(o2.getOrder());
            }
        });

        // ADD ALL FIELDS TO FORM
        FormLayout layout = new FormLayout();
        form.setLayout(layout);
        // form.addField("id", new TextField("pole testowe"));
        for (FieldContainer field : fields) {
            if (field != null) {
                field.placeYourselfInForm(form, layout);
            }
        }

        // ADD FORMAT SELECTION
        if (includeReportFormat) {
            format = ComponentFactory.createFormatCombo(ReportType.PDF, UiIds.LABEL_FORMAT);
            format.setSizeUndefined();
            form.addField("format", format);
        }

        if (includeLocale) {
            localeComboBox = UiFactoryExt.createLocaleCombo(UiIds.LABEL_LOCALE, getLocale());
            form.addField(JRParameter.REPORT_LOCALE, localeComboBox);
        } else {
            form.setDescription(readonly ? VaadinUtil.getValue("invoker.form.header.readonly") : VaadinUtil.getValue("invoker.form.header"));
        }
        form.setImmediate(true);
        form.setValidationVisible(true);
        form.setValidationVisibleOnCommit(true);
        form.setInvalidCommitted(false);
        form.setWriteThrough(false);
    }

    /**
     * Populates Vaadin field properties based on report parameters taken from
     * JRXML.
     *
     * @param param Report parameter
     * @param props Parameter properties
     * @return Filled field properties
     */
    private FieldProperties parseFieldProperties(ReportParameter param, Map<ARConstants.Keys, ReportProperty> props) {
        FieldProperties fieldProperties = new FieldProperties();

        // INPUT_TYPE
        String inputTypeString = getValueFromMap(props, ARConstants.Keys.INPUT_TYPE, ARConstants.InputTypes.values(),
                ARConstants.InputTypes.CHECKBOX.name());
        fieldProperties.setInputType(ARConstants.InputTypes.valueOf(StringUtils.upperCase(inputTypeString)));
        // WIDTH
        fieldProperties.setWidth(getValueFromMap(props, ARConstants.Keys.WIDTH, null, ""));

        // CAPTION
        String caption = getValueFromMap(props, ARConstants.Keys.LABEL, null, param.getName());
        fieldProperties.setCaption(StringUtils.capitaliseAllWords(StringUtils.lowerCase(caption)));

        // ORDER
        String orderString = getValueFromMap(props, ARConstants.Keys.ORDER, null, "1000");
        try {
            fieldProperties.setOrder(Integer.valueOf(orderString));
        } catch (NumberFormatException e) {
            fieldProperties.setOrder(1000);
        }
        // REQUIRED
        String requiredString = getValueFromMap(props, ARConstants.Keys.REQUIRED, ARConstants.BooleanValues.values(),
                "false");
        fieldProperties.setRequired(Boolean.valueOf(requiredString));
        fieldProperties.setRequiredError(getValueFromMap(props, ARConstants.Keys.REQUIRED_ERROR, null,
                "form.errors.required"));

        // REGEXP
        fieldProperties.setRegexp(getValueFromMap(props, ARConstants.Keys.REGEXP, null, ""));
        fieldProperties.setRegexpError(getValueFromMap(props, ARConstants.Keys.REGEXP_ERROR, null, "form.errors.regexp"));

        // MAXCHARS
        fieldProperties.setMaxchars(getValueFromMap(props, ARConstants.Keys.MAXCHARS, null, ""));

        // LEVEL
        String levelString = getValueFromMap(props, ARConstants.Keys.LEVEL, null, "1");
        try {
            fieldProperties.setLevel(Integer.valueOf(levelString));
        } catch (NumberFormatException e) {
            fieldProperties.setOrder(1);
        }

        // FILTER_GROUP
        fieldProperties.setFilterGroup(getValueFromMap(props, ARConstants.Keys.FILTER_GROUP, null, ""));

        // SELECT_ALL
        String selectAllString = getValueFromMap(props, ARConstants.Keys.SELECT_ALL, ARConstants.BooleanValues.values(),
                "false");
        fieldProperties.setSelectAll(Boolean.valueOf(selectAllString));

        // MULTIPLE_CHOICE
        String multipleChoiceString = getValueFromMap(props, ARConstants.Keys.MULTIPLE_CHOICE,
                ARConstants.BooleanValues.values(), "false");
        fieldProperties.setMultipleChoice(Boolean.valueOf(multipleChoiceString));

        //
        // DATA FETCH
        //

        // DICT_QUERY
        fieldProperties.setDictQuery(getValueFromMap(props, ARConstants.Keys.DICT_QUERY, null, ""));

        fieldProperties.setDictItemList(getValueFromMap(props, ARConstants.Keys.DICT_ITEM_LIST, null, ""));

        //
        // SCRIPTS
        //

        // SCRIPT_LANGUAGE
        String scriptLang = getValueFromMap(props, ARConstants.Keys.SCRIPT_LANGUAGE, null, "");
        if (StringUtils.isNotEmpty(scriptLang)) {
            ScriptEngine se;
            ScriptEngineManager sem = new ScriptEngineManager();
            try {
                se = sem.getEngineByName(scriptLang);
            } catch (NoSuchMethodError e) {
                try {
                    se = sem.getEngineByName(scriptLang);
                } catch (NoSuchMethodError e1) {
                    try {
                        se = sem.getEngineByName(scriptLang);
                    } catch (NoSuchMethodError e2) {
                        se = sem.getEngineByName(scriptLang);
                    }
                }
            }
            Invocable sei = (Invocable) se;
            fieldProperties.setSei(sei);
            fieldProperties.setSe(se);
        }

        // SPECIAL_CONTROL_CODE
        fieldProperties.setSpecialControlCode(getValueFromMap(props, ARConstants.Keys.SPECIAL_CONTROL_CODE, null, ""));

        // SPECIAL_VALIDATION_CODE
        fieldProperties.setSpecialValidationCode(getValueFromMap(props, ARConstants.Keys.SPECIAL_VALIDATION_CODE, null,
                ""));

        // SPECIAL_VALIDATION_ERROR
        fieldProperties.setSpecialValidationError(getValueFromMap(props, ARConstants.Keys.SPECIAL_VALIDATION_ERROR, null,
                ""));

        // SPECIAL_DATA_QUERY_CODE
        fieldProperties.setSpecialDataQueryCode(getValueFromMap(props, ARConstants.Keys.SPECIAL_DATA_QUERY_CODE, null, ""));
        return fieldProperties;
    }

    private class BuildingFailedException extends Exception {
    }

    /**
     * Attaches the component in the application. The reason the initialization
     * of the view is here is that we need to access user's login which may be
     * unavailable at th = nulle time of creation of the component.
     */
    @Override
    public void attach() {
        super.attach();
        initView();
        initFieldsFromConfig();
    }
}
