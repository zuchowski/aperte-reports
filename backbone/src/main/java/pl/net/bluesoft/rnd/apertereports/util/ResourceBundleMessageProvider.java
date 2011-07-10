package pl.net.bluesoft.rnd.apertereports.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * Message provider based on {@link java.util.ResourceBundle} class. Supports locales.
 */

public class ResourceBundleMessageProvider {
    /**
     * ResourceBundle internal cache.
     */
    private Map<Locale, ResourceBundle> resourceBundleMap;

    /**
     * Internal default locale.
     */
    private Locale locale;

    /**
     * Bundle name.
     */
    private String resourceName;

    /**
     * Constructor with specified resource bundle name. Initializes default locale.
     *
     * @param resourceName Resource name
     */
    public ResourceBundleMessageProvider(String resourceName) {
        this.resourceName = resourceName;
        initialize();
    }

    /**
     * Gets message by key. Uses internal default locale if set or <code>Locale.getDefault()</code>.
     *
     * @param messageKey Message key
     * @return Message content
     */
    public String getMessage(String messageKey) {
        return getMessage(locale != null ? locale : Locale.getDefault(), messageKey);
    }

    /**
     * Gets localized message by messageKey.
     *
     * @param messageLocale Message locale
     * @param messageKey    Message key
     * @return Message content
     */
    public String getMessage(Locale messageLocale, String messageKey) {
        ResourceBundle messages = resourceBundleMap.get(messageLocale);
        if (messages == null) {
            resourceBundleMap.put(messageLocale, messages = ResourceBundle.getBundle(resourceName, messageLocale));
        }
        return messages.getString(messageKey);
    }

    /**
     * Used to initialize message provider.
     */
    public final void initialize() {
        resourceBundleMap = new HashMap<Locale, ResourceBundle>();
        resourceBundleMap.put(Locale.getDefault(), ResourceBundle.getBundle(resourceName));
    }

    /**
     * Gets internal default locale.
     *
     * @return Locale
     */
    public Locale getLocale() {
        return locale;
    }

    /**
     * Sets internal default locale.
     *
     * @param locale Locale
     */
    public void setLocale(Locale locale) {
        this.locale = locale;
    }
}
