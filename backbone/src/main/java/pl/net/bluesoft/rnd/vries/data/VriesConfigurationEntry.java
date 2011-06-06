package pl.net.bluesoft.rnd.vries.data;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Represents a configuration entry. The data should be kept in <code>vries_configuration</code> table in <code>public</code> schema.
 */
@Entity
@Table(schema = "public", name = "vries_configuration")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class VriesConfigurationEntry implements Serializable {
    /**
     * Primary key.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @PrimaryKeyJoinColumn
    @Column(name = "id", nullable = false, length = 10)
    private Integer id;

    /**
     * Readable description of this entry.
     */
    @Column
    private String description;

    /**
     * The key.
     */
    @Column
    private String key;

    /**
     * The value.
     */
    @Column
    private String value;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
