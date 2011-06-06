package pl.net.bluesoft.rnd.vries.components;

import com.liferay.portal.model.User;
import com.vaadin.data.Buffered;
import com.vaadin.data.Validator;
import com.vaadin.data.validator.RegexpValidator;
import com.vaadin.ui.*;
import eu.livotov.tpt.i18n.TM;
import net.sf.jasperreports.engine.JRException;
import org.apache.commons.lang.StringUtils;
import pl.net.bluesoft.rnd.vries.AbstractLazyLoaderComponent;
import pl.net.bluesoft.rnd.vries.AbstractVriesApplication;
import pl.net.bluesoft.rnd.vries.dao.DictionaryDAO;
import pl.net.bluesoft.rnd.vries.engine.ReportMaster;
import pl.net.bluesoft.rnd.vries.engine.ReportParameter;
import pl.net.bluesoft.rnd.vries.engine.ReportProperty;
import pl.net.bluesoft.rnd.vries.util.*;
import pl.net.bluesoft.rnd.vries.util.wrappers.DictionaryItemsWrapper;
import pl.net.bluesoft.rnd.vries.util.wrappers.FieldContainer;
import pl.net.bluesoft.rnd.vries.util.wrappers.FieldProperties;
import pl.net.bluesoft.rnd.vries.util.wrappers.FilterContainer;
import pl.net.bluesoft.rnd.vries.wrappers.DictionaryItem;
import pl.net.bluesoft.rnd.vries.xml.ReportConfig;
import pl.net.bluesoft.rnd.vries.xml.ReportConfigParameter;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.text.ParseException;
import java.util.*;

import static pl.net.bluesoft.rnd.vries.util.Constants.InputTypes.*;

/**
 * Displays report parameters taken from JRXML parameters section as Vaadin fields in a form. Supports lazy loading.
 */
public class ReportParametersComponent extends AbstractLazyLoaderComponent {
    private Form form = new Form();
    private HashMap<String, FilterContainer> filters;
    private List<FieldContainer> fields = new LinkedList<FieldContainer>();
    private OptionGroup format;

    private ReportMaster reportMaster;

    private String reportSource;
    private Integer cacheId;
    private List<ReportConfigParameter> reportParameters;

    private boolean includeReportFormat = true;
    private boolean readonly = false;

    public ReportParametersComponent(ReportMaster reportMaster) throws JRException {
        this.reportMaster = reportMaster;
        init();
    }

    public ReportParametersComponent(String reportSource, Integer cacheId, boolean lazyLoad) throws JRException {
        this.reportSource = reportSource;
        this.cacheId = cacheId;
        if (!lazyLoad) {
            reportMaster = new ReportMaster(reportSource, cacheId);
            init();
        }
    }

    public ReportParametersComponent(String reportSource, Integer cacheId, ReportConfig reportConfig,
                                     boolean includeReportFormat, boolean lazyLoad) throws JRException {
        this.reportSource = reportSource;
        this.cacheId = cacheId;
        this.reportParameters = reportConfig != null ? reportConfig.getParameters() : null;
        this.includeReportFormat = includeReportFormat;
        if (!lazyLoad) {
            reportMaster = new ReportMaster(reportSource, cacheId);
            init();
        }
    }

    public ReportParametersComponent(String reportSource, Integer cacheId, List<ReportConfigParameter> reportParameters,
                                     boolean includeReportFormat, boolean lazyLoad, boolean readonly) throws JRException {
        this.reportSource = reportSource;
        this.cacheId = cacheId;
        this.reportParameters = reportParameters;
        this.includeReportFormat = includeReportFormat;
        this.readonly = readonly;
        if (!lazyLoad) {
            reportMaster = new ReportMaster(reportSource, cacheId);
            init();
        }
    }

    public ReportParametersComponent(String reportSource, Integer cacheId, ReportConfig reportConfig,
                                     boolean includeReportFormat, boolean lazyLoad, boolean readonly) throws JRException {
        this.reportSource = reportSource;
        this.cacheId = cacheId;
        this.reportParameters = reportConfig != null ? reportConfig.getParameters() : null;
        this.includeReportFormat = includeReportFormat;
        this.readonly = readonly;
        if (!lazyLoad) {
            reportMaster = new ReportMaster(reportSource, cacheId);
            init();
        }
    }

    /**
     * Returns a map of report parameters collected from the generated form fields.
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
            String value = TextUtil.encodeObjectToSQL(rawValue);
            parameters.put(field.getName(), value);
        }

        parameters.put("login", getLogin());
        return parameters;

    }

    /**
     * Returns Liferay login of the currently logged user.
     *
     * @return A user login
     */
    private String getLogin() {
        String login = "";
        try {
            User user = ((AbstractVriesApplication) getApplication()).getLiferayUser();
            if (user == null || user.getEmailAddress() == null) {
                return login;
            }
            login = user.getLogin();
        }
        catch (Exception e) {
            ExceptionUtil.logWarningException(TM.get("liferay.get.login.exception"), e);
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
        reportMaster = new ReportMaster(reportSource, cacheId);
        init();
    }

    /**
     * Validates all the fields in the form before commit.
     *
     * @return <code>TRUE</code> if the form is valid
     */
    public boolean validateForm() {
        boolean result = true;
        for (FieldContainer field : fields) {
            field.validate();
        }
        try {
            form.commit();
        }
        catch (Validator.InvalidValueException e) {
            ExceptionUtil.logSevereException(e);
            result = false;
        }
        catch (Buffered.SourceException e) {
            ExceptionUtil.logSevereException(e);
            result = false;
        }
        return result;
    }

    /**
     * Builds a new field container carrying the generated Vaadin field with all its properties.
     * Returns <code>null</code> on {@link BuildingFailedException} which may occur
     * on special script invocation.
     *
     * @param param Report parameter
     * @param fieldProperties Field properties
     * @return A new field container
     */
    private FieldContainer buildField(ReportParameter param, FieldProperties fieldProperties) {
        try {
            FieldContainer container = new FieldContainer();
            List<DictionaryItem> items = null;

            /** DICT_QUERY **/
            if (StringUtils.isNotEmpty(fieldProperties.getDictQuery())) {
                String login = getLogin();
                String dictQuery = fieldProperties.getDictQuery().replaceAll("\\$LOGIN", login);
                items = DictionaryDAO.fetchDictionary(dictQuery);
            }

            /** DICT_ITEM_LIST **/
            if (StringUtils.isNotEmpty(fieldProperties.getDictItemList())) {
                items = DictionaryDAO.readDictionaryFromString(fieldProperties.getDictItemList());
            }

            /** SPECIAL_DATA_QUERY_CODE **/
            items = executeSpecialDataQueryCode(fieldProperties, items);

            /** INPUT_TYPE **/
            Field field = buildFieldBaseForInputType(fieldProperties, items, container);

            if (field != null && field instanceof Field) {
                applyAttributes(fieldProperties, container, field);

            }

            // obsluga kontrolek multi-level select: filter i filtered_select
            if (fieldProperties.getInputType() == Constants.InputTypes.FILTERED_SELECT
                    || fieldProperties.getInputType() == Constants.InputTypes.FILTER) {
                FilterContainer filterContainer = getFilterContainer(fieldProperties.getFilterGroup());
                filterContainer.addFilter((Select) field, fieldProperties.getLevel(), items);
                container.setFieldComponent(filterContainer);
            }

            /** SPECIAL_VALIDATION_CODE **/
            attachSpecialValidators(fieldProperties, container);
            container.setComponentType(fieldProperties.getInputType());
            container.setOrder(fieldProperties.getOrder());
            container.setName(param.getName());
            return container;
        }
        catch (BuildingFailedException e) {
            return null;
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

        /** WIDTH **/
        if (StringUtils.isNotEmpty(fieldProperties.getWidth())) {
            field.setWidth(StringUtils.lowerCase(fieldProperties.getWidth()));
        }

        /** SELECT_ALL **/
        container.setSelectAll(fieldProperties.getSelectAll());

        /**             **/
        /** VALIDATORS **/
        /**             **/

        /** REQUIRED **/
        if (fieldProperties.isRequired()) {
            field.setRequired(fieldProperties.isRequired());
            field.setRequiredError(TM.get(fieldProperties.getRequiredError(), fieldProperties.getCaption()));
        }

        /** REGEXP **/
        if (StringUtils.isNotEmpty(fieldProperties.getRegexp())) {
            field.addValidator(new RegexpValidator(fieldProperties.getRegexp(), TM.get(
                    fieldProperties.getRegexpError(), fieldProperties.getCaption(), fieldProperties.getRegexp())));
        }

        /** MAXCHARS **/
        if (StringUtils.isNotEmpty(fieldProperties.getMaxchars()) && field instanceof TextField) {
            try {
                ((TextField) field).setMaxLength(Integer.valueOf(fieldProperties.getMaxchars()));
            }
            catch (NumberFormatException e) {
                ExceptionUtil.logSevereException(e);
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
            }
            catch (ScriptException e) {
                NotificationUtil.showExceptionNotification(getWindow(),
                        "invoker.form.special_validation_code.script_exception", e);
                ExceptionUtil.logSevereException(e);
                throw new BuildingFailedException();
            }
            catch (NoSuchMethodException e) {
                NotificationUtil.showExceptionNotification(getWindow(),
                        "invoker.form.special_validation_code.no_such_method_exception", e);
                ExceptionUtil.logSevereException(e);
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
            case TEXT:
                field = new TextField(fieldProperties.getCaption());
                break;
            case CHECKBOX:
                field = new CheckBox(fieldProperties.getCaption());
                break;
            case DATE:
                DateField dateField = new DateField(fieldProperties.getCaption()) {
                    @Override
                    protected Date handleUnparsableDateString(String dateString) throws ConversionException {
                        try {
                            return super.handleUnparsableDateString(dateString);
                        }
                        catch (ConversionException e) {
                            throw new ConversionException(TM.get("form.errors.unparsable_date", getCaption(), dateString));
                        }
                    }
                };
                dateField.setDateFormat(TimeUtil.getDefaultDateFormat());
                dateField.setResolution(DateField.RESOLUTION_MIN);
                field = dateField;
                break;
            case TEXTAREA:
                TextField text = new TextField(fieldProperties.getCaption());
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
                }
                catch (NullPointerException e) {
                    NotificationUtil.showExceptionNotification(getWindow(), "invoker.form.no_items_for_exception",
                            fieldProperties.getCaption());
                    throw new BuildingFailedException();
                }

                select.setMultiSelect(fieldProperties.getInputType() == Constants.InputTypes.MULTISELECT);
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
                }
                catch (NullPointerException e) {
                    NotificationUtil.showExceptionNotification(getWindow(), "invoker.form.no_items_for_exception",
                            fieldProperties.getCaption());
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
                /** SPECIAL_CONTROL_CODE **/
                if (fieldProperties.getSei() != null && StringUtils.isNotEmpty(fieldProperties.getSpecialControlCode())) {
                    try {
                        fieldProperties.getSe().eval(fieldProperties.getSpecialControlCode());
                        fieldProperties.getSei().invokeFunction("specialControl", container, items,
                                fieldProperties.getWidth(), fieldProperties.getMaxchars(), fieldProperties.isRequired(),
                                fieldProperties.getRequiredError(), fieldProperties.getRegexp(),
                                fieldProperties.getRegexpError(), fieldProperties.getCaption());
                    }
                    catch (ScriptException e) {
                        NotificationUtil.showExceptionNotification(getWindow(),
                                "invoker.form.special_control_code.script_exception", e);
                        ExceptionUtil.logSevereException(e);
                        throw new BuildingFailedException();
                    }
                    catch (NoSuchMethodException e) {
                        NotificationUtil.showExceptionNotification(getWindow(),
                                "invoker.form.special_control_code.no_such_method_exception", e);
                        ExceptionUtil.logSevereException(e);
                        throw new BuildingFailedException();
                    }
                }
                Component component = container.getFieldComponent();
                if (component instanceof Field) {
                    field = (Field) component;
                }
                break;

            default:
                field = new TextField(fieldProperties.getCaption());
                break;
        }
        return field;
    }

    /**
     * Invokes a special script query using the invokable field property. If the invokable property is not present
     * a default list is returned.
     *
     * @param fieldProperties Field properties
     * @param items A default list of items
     * @return Updated list of items
     * @throws BuildingFailedException on script invocation error
     */
    public List<DictionaryItem> executeSpecialDataQueryCode(FieldProperties fieldProperties, List<DictionaryItem> items) throws BuildingFailedException {
        if (fieldProperties.getSei() != null && StringUtils.isNotEmpty(fieldProperties.getSpecialDataQueryCode())) {
            try {
                DictionaryItemsWrapper itemsWrapper = new DictionaryItemsWrapper();
                fieldProperties.getSe().eval(fieldProperties.getSpecialDataQueryCode());
                fieldProperties.getSei().invokeFunction("specialQuery", itemsWrapper, fieldProperties.getDictQuery());
                items = itemsWrapper.getItems();
            }
            catch (ScriptException e) {
                ExceptionUtil.logSevereException(e);
                NotificationUtil.showExceptionNotification(getWindow(),
                        "invoker.form.special_data_query_code.script_exception", e);
                throw new BuildingFailedException();
            }
            catch (NoSuchMethodException e) {
                ExceptionUtil.logSevereException(e);
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
     * Retrieves the value from a report parameter properties. If the input type cannot be determined a default value is returned.
     *
     * @param props Input parameter properties
     * @param key Search key type
     * @param inputTypes Possible input types
     * @param defaultValue Default value
     * @return Parameter value
     */
    private String getValueFromMap(Map<Constants.Keys, ReportProperty> props, Constants.Keys key, Enum<?>[] inputTypes,
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
        if (reportParameters != null && !reportParameters.isEmpty()) {
            for (ReportConfigParameter p : reportParameters) {
                for (FieldContainer field : fields) {
                    if (p.getName().equals(field.getName())) {
                        try {
                            Field fieldComponent = field.getFieldComponent() instanceof Field ? (Field) field.getFieldComponent() : null;
                            Class<?> fieldType = fieldComponent == null ? null :
                                    fieldComponent.getPropertyDataSource() == null ? null :
                                            fieldComponent.getPropertyDataSource().getType();
                            if (DATE.equals(field.getComponentType())) {
                                fieldType = Date.class;
                            }
                            else if (MULTISELECT.equals(field.getComponentType()) ||
                                    CHECKBOXES.equals(field.getComponentType()) ||
                                    FILTERED_SELECT.equals(field.getComponentType())) {
                                fieldType = Collection.class;
                            }
                            Object v = TextUtil.encodeSQLToObject(fieldType, p.getValue());
                            field.setValue(v);
                        }
                        catch (ParseException e) {
                            ExceptionUtil.logSevereException(e);
                            NotificationUtil.showExceptionNotification(getWindow(), TM.get("exception.gui.error"), e);
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Creates a form with generated Vaadin fields.
     */
    private void initView() {
        List<ReportParameter> parameters = reportMaster.getParameters();
        for (ReportParameter param : parameters) {
            Map<Constants.Keys, ReportProperty> props = param.getProperties();
            if (props == null || props.isEmpty()) {
                continue;
            }
            FieldProperties fieldProperties = parseFieldProperties(param, props);
            FieldContainer container = buildField(param, fieldProperties);
            fields.add(container);
        }

        /** ORDER FIELDS **/
        Collections.sort(fields, new Comparator<FieldContainer>() {
            @Override
            public int compare(FieldContainer o1, FieldContainer o2) {
                return o1.getOrder().compareTo(o2.getOrder());
            }
        });

        /** ADD ALL FIELDS TO FORM **/
        FormLayout layout = new FormLayout();
        form.setLayout(layout);
        // form.addField("id", new TextField("pole testowe"));
        for (FieldContainer field : fields) {
            if (field != null) {
                field.placeYourselfInForm(form, layout);
            }
        }

        /** ADD FORMAT SELECTION **/
        if (includeReportFormat) {
            format = new OptionGroup(TM.get("invoker.form.select_format"), new ArrayList<String>(
                    Constants.ReportType.values().length) {
                {
                    for (Constants.ReportType reportType : Constants.ReportType.values()) {
                        add(reportType.toString());
                    }
                }
            });
            format.setValue(Constants.ReportType.PDF.toString());
            form.addField("format", format);
        }

        if (fields.isEmpty()) {
            Label l = new Label(TM.get("invoker.form.header.nofields"));
            l.setWidth("400px");
            form.getLayout().addComponent(l);

        }
        else {
            form.setDescription(readonly ? TM.get("invoker.form.header.readonly") : TM.get("invoker.form.header"));
        }
        form.setImmediate(true);
        form.setValidationVisible(true);
        form.setValidationVisibleOnCommit(true);
        form.setInvalidCommitted(false);
        form.setWriteThrough(false);
    }

    /**
     * Populates Vaadin field properties based on report parameters taken from JRXML.
     *
     * @param param Report parameter
     * @param props Parameter properties
     * @return Filled field properties
     */
    private FieldProperties parseFieldProperties(ReportParameter param, Map<Constants.Keys, ReportProperty> props) {
        FieldProperties fieldProperties = new FieldProperties();

        /** INPUT_TYPE **/
        String inputTypeString = getValueFromMap(props, Constants.Keys.INPUT_TYPE, Constants.InputTypes.values(),
                Constants.InputTypes.CHECKBOX.name());
        fieldProperties.setInputType(Constants.InputTypes.valueOf(StringUtils.upperCase(inputTypeString)));

        /** WIDTH **/
        fieldProperties.setWidth(getValueFromMap(props, Constants.Keys.WIDTH, null, ""));

        /** CAPTION **/
        String caption = getValueFromMap(props, Constants.Keys.LABEL, null, param.getName());
        fieldProperties.setCaption(StringUtils.capitaliseAllWords(StringUtils.lowerCase(caption)));

        /** ORDER **/
        String orderString = getValueFromMap(props, Constants.Keys.ORDER, null, "1000");
        try {
            fieldProperties.setOrder(Integer.valueOf(orderString));
        }
        catch (NumberFormatException e) {
            fieldProperties.setOrder(1000);
        }
        /** REQUIRED **/
        String requiredString = getValueFromMap(props, Constants.Keys.REQUIRED, Constants.BooleanValues.values(),
                "false");
        fieldProperties.setRequired(Boolean.valueOf(requiredString));
        fieldProperties.setRequiredError(getValueFromMap(props, Constants.Keys.REQUIRED_ERROR, null,
                "form.errors.required"));

        /** REGEXP **/
        fieldProperties.setRegexp(getValueFromMap(props, Constants.Keys.REGEXP, null, ""));
        fieldProperties.setRegexpError(getValueFromMap(props, Constants.Keys.REGEXP_ERROR, null, "form.errors.regexp"));

        /** MAXCHARS **/
        fieldProperties.setMaxchars(getValueFromMap(props, Constants.Keys.MAXCHARS, null, ""));

        /** LEVEL **/
        String levelString = getValueFromMap(props, Constants.Keys.LEVEL, null, "1");
        try {
            fieldProperties.setLevel(Integer.valueOf(levelString));
        }
        catch (NumberFormatException e) {
            fieldProperties.setOrder(1);
        }

        /** FILTER_GROUP **/
        fieldProperties.setFilterGroup(getValueFromMap(props, Constants.Keys.FILTER_GROUP, null, ""));

        /** SELECT_ALL **/
        String selectAllString = getValueFromMap(props, Constants.Keys.SELECT_ALL, Constants.BooleanValues.values(),
                "false");
        fieldProperties.setSelectAll(Boolean.valueOf(selectAllString));

        /** MULTIPLE_CHOICE **/
        String multipleChoiceString = getValueFromMap(props, Constants.Keys.MULTIPLE_CHOICE,
                Constants.BooleanValues.values(), "false");
        fieldProperties.setMultipleChoice(Boolean.valueOf(multipleChoiceString));

        /**             **/
        /** DATA FETCH **/
        /**             **/

        /** DICT_QUERY **/
        fieldProperties.setDictQuery(getValueFromMap(props, Constants.Keys.DICT_QUERY, null, ""));

        fieldProperties.setDictItemList(getValueFromMap(props, Constants.Keys.DICT_ITEM_LIST, null, ""));

        /**         **/
        /** SCRIPTS **/
        /**         **/

        /** SCRIPT_LANGUAGE **/
        String scriptLang = getValueFromMap(props, Constants.Keys.SCRIPT_LANGUAGE, null, "");
        if (StringUtils.isNotEmpty(scriptLang)) {
            ScriptEngine se;
            ScriptEngineManager sem = new ScriptEngineManager();
            try {
                se = sem.getEngineByName(scriptLang);
            }
            catch (NoSuchMethodError e) {
                try {
                    se = sem.getEngineByName(scriptLang);
                }
                catch (NoSuchMethodError e1) {
                    try {
                        se = sem.getEngineByName(scriptLang);
                    }
                    catch (NoSuchMethodError e2) {
                        se = sem.getEngineByName(scriptLang);
                    }
                }
            }
            Invocable sei = (Invocable) se;
            fieldProperties.setSei(sei);
            fieldProperties.setSe(se);
        }

        /** SPECIAL_CONTROL_CODE **/
        fieldProperties.setSpecialControlCode(getValueFromMap(props, Constants.Keys.SPECIAL_CONTROL_CODE, null, ""));

        /** SPECIAL_VALIDATION_CODE **/
        fieldProperties.setSpecialValidationCode(getValueFromMap(props, Constants.Keys.SPECIAL_VALIDATION_CODE, null,
                ""));

        /** SPECIAL_VALIDATION_ERROR **/
        fieldProperties.setSpecialValidationError(getValueFromMap(props, Constants.Keys.SPECIAL_VALIDATION_ERROR, null,
                ""));

        /** SPECIAL_DATA_QUERY_CODE **/
        fieldProperties
                .setSpecialDataQueryCode(getValueFromMap(props, Constants.Keys.SPECIAL_DATA_QUERY_CODE, null, ""));
        return fieldProperties;
    }

    private class BuildingFailedException extends Exception {

    }

    /**
     * Attaches the component in the application. The reason the initialization of the view is here
     * is that we need to access user's login which may be unavailable at th = nulle time of creation of the component.
     */
    @Override
    public void attach() {
        super.attach();
        initView();
        initFieldsFromConfig();
    }
}
