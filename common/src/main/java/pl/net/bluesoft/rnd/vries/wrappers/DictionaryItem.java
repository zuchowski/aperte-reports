package pl.net.bluesoft.rnd.vries.wrappers;

/**
 * Represents a dictionary item. Used in filter containers.
 */
public class DictionaryItem {
    /**
     * Item key code.
     */
    private String code;
    /**
     * Item visible description.
     */
    private String description;

    /**
     * Filter level columns.
     */
    private String[] columns;

    public DictionaryItem(int columnCount) {
        columns = new String[columnCount];
    }

    public DictionaryItem(String code, String description, String[] columns) {
        super();
        this.code = code;
        this.description = description;
        this.columns = columns;
    }

    public String getCode() {
        return code;
    }

    public String getColumn(Integer idx) {
        return columns[idx];
    }

    public String getDescription() {
        return description;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setColumn(Integer idx, String value) {
        columns[idx] = value;
    }

    public void setColumns(String[] columns) {
        this.columns = columns;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
