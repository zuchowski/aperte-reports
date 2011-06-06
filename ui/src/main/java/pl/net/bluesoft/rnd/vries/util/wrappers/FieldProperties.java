package pl.net.bluesoft.rnd.vries.util.wrappers;

import pl.net.bluesoft.rnd.vries.util.Constants.InputTypes;

import javax.script.Invocable;
import javax.script.ScriptEngine;

/**
 * A bean class that represents a single report parameter defined in Jasper's JRXML.
 */
public class FieldProperties {
    /**
     * An input type of the parameter.
     */
    private InputTypes inputType;
    /**
     * A level for multilevel selects.
     */
    private Integer level;
    /**
     * Filtering group.
     */
    private String filterGroup;
    /**
     * Is multiple choice?
     */
    private Boolean multipleChoice;
    /**
     * Show "select all" box?
     */
    private Boolean selectAll;
    /**
     * A certain width of the Vaadin component.
     */
    private String width;
    /**
     * The caption of the field shown in the form.
     */
    private String caption;
    /**
     * Is field required to fill by the user?
     */
    private Boolean required;
    /**
     * The error message shown on the validation error.
     */
    private String requiredError;
    /**
     * The regexp the value of the field should match.
     */
    private String regexp;
    /**
     * The error message shown on regexp validation failure.
     */
    private String regexpError;
    /**
     * Maximum characters filled.
     */
    private String maxchars;
    /**
     * A dictionary query. This is basically a string which looks like this:
     * <p/>
     * <pre>
     *     java:comp/env/jdbc/unirep;SELECT DISTINCT reportname, reportname FROM unirep_configuration
     * </pre>
     * The string is split by a semicolon. The first part should be a JNDI name of the datasource, while
     * the second - the SQL query to use.
     */
    private String dictQuery;
    /**
     * A static dictionary list items. For example this parameter could look like this:
     * <p/>
     * <pre>
     *     chart:Display chart;table:Display table;projects:Display list of projects
     * </pre>
     */
    private String dictItemList;
    /**
     * Defines the order of the field in the form.
     */
    private Integer order;
    /**
     * Script engine to use in a custom language parameters.
     */
    private ScriptEngine se;
    /**
     * The invocable instance to use by the script engine.
     */
    private Invocable sei;
    /**
     * Special control code. Used along with the {@link ScriptEngine}.
     */
    private String specialControlCode;
    /**
     * Special validation code. Used along with the {@link ScriptEngine}.
     */
    private String specialValidationCode;
    /**
     * Error shown on validation error of a special control code. Used along with the {@link ScriptEngine}.
     */
    private String specialValidationError;
    /**
     * Special code data query.
     */
    private String specialDataQueryCode;

    public FieldProperties() {
    }

    public FieldProperties(InputTypes inputType, Integer level, String filterGroup, Boolean multipleChoice,
                           String width, String caption, Boolean required, String requiredError, String regexp,
                           String regexpError, String maxchars, String dictQuery, ScriptEngine se, Invocable sei,
                           String specialControlCode, String specialValidationCode, String specialValidationError,
                           String specialDataQueryCode) {
        this.inputType = inputType;
        this.level = level;
        this.filterGroup = filterGroup;
        this.multipleChoice = multipleChoice;
        this.width = width;
        this.caption = caption;
        this.required = required;
        this.requiredError = requiredError;
        this.regexp = regexp;
        this.regexpError = regexpError;
        this.maxchars = maxchars;
        this.dictQuery = dictQuery;
        this.se = se;
        this.sei = sei;
        this.specialControlCode = specialControlCode;
        this.specialValidationCode = specialValidationCode;
        this.specialValidationError = specialValidationError;
        this.specialDataQueryCode = specialDataQueryCode;
    }

    public String getCaption() {
        return caption;
    }

    public String getDictItemList() {
        return dictItemList;
    }

    public String getDictQuery() {
        return dictQuery;
    }

    public String getFilterGroup() {
        return filterGroup;
    }

    public InputTypes getInputType() {
        return inputType;
    }

    public Integer getLevel() {
        return level;
    }

    public String getMaxchars() {
        return maxchars;
    }

    public Integer getOrder() {
        return order;
    }

    public String getRegexp() {
        return regexp;
    }

    public String getRegexpError() {
        return regexpError;
    }

    public String getRequiredError() {
        return requiredError;
    }

    public ScriptEngine getSe() {
        return se;
    }

    public Invocable getSei() {
        return sei;
    }

    public Boolean getSelectAll() {
        return selectAll;
    }

    public String getSpecialControlCode() {
        return specialControlCode;
    }

    public String getSpecialDataQueryCode() {
        return specialDataQueryCode;
    }

    public String getSpecialValidationCode() {
        return specialValidationCode;
    }

    public String getSpecialValidationError() {
        return specialValidationError;
    }

    public String getWidth() {
        return width;
    }

    public Boolean isMultipleChoice() {
        return multipleChoice;
    }

    public Boolean isRequired() {
        return required;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

    public void setDictItemList(String dictItemList) {
        this.dictItemList = dictItemList;
    }

    public void setDictQuery(String dictQuery) {
        this.dictQuery = dictQuery;
    }

    public void setFilterGroup(String filterGroup) {
        this.filterGroup = filterGroup;
    }

    public void setInputType(InputTypes inputType) {
        this.inputType = inputType;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public void setMaxchars(String maxchars) {
        this.maxchars = maxchars;
    }

    public void setMultipleChoice(Boolean multipleChoice) {
        this.multipleChoice = multipleChoice;
    }

    public void setOrder(Integer order) {
        this.order = order;
    }

    public void setRegexp(String regexp) {
        this.regexp = regexp;
    }

    public void setRegexpError(String regexpError) {
        this.regexpError = regexpError;
    }

    public void setRequired(Boolean required) {
        this.required = required;
    }

    public void setRequiredError(String requiredError) {
        this.requiredError = requiredError;
    }

    public void setSe(ScriptEngine se) {
        this.se = se;
    }

    public void setSei(Invocable sei) {
        this.sei = sei;
    }

    public void setSelectAll(Boolean selectAll) {
        this.selectAll = selectAll;
    }

    public void setSpecialControlCode(String specialControlCode) {
        this.specialControlCode = specialControlCode;
    }

    public void setSpecialDataQueryCode(String specialDataQueryCode) {
        this.specialDataQueryCode = specialDataQueryCode;
    }

    public void setSpecialValidationCode(String specialValidationCode) {
        this.specialValidationCode = specialValidationCode;
    }

    public void setSpecialValidationError(String specialValidationError) {
        this.specialValidationError = specialValidationError;
    }

    public void setWidth(String width) {
        this.width = width;
    }
}
