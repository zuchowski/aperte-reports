package pl.net.bluesoft.rnd.apertereports.common.exception;

/**
 * A runtime exception thrown when an internal error occurs.
 */
public class VriesRuntimeException extends RuntimeException {
    private String vriesErrorPrefix = null;

    public VriesRuntimeException(String vriesErrorPrefix) {
        this.vriesErrorPrefix = vriesErrorPrefix;
    }

    public VriesRuntimeException(String message, String vriesErrorPrefix) {
        super(message);
        this.vriesErrorPrefix = vriesErrorPrefix;
    }

    public VriesRuntimeException(String message, String vriesErrorPrefix, Throwable cause) {
        super(message, cause);
        this.vriesErrorPrefix = vriesErrorPrefix;
    }

    public VriesRuntimeException(String vriesErrorPrefix, Throwable cause) {
        super(cause);
        this.vriesErrorPrefix = vriesErrorPrefix;
    }

    public String getVriesErrorPrefix() {
        return vriesErrorPrefix;
    }

    public void setVriesErrorPrefix(String vriesErrorPrefix) {
        this.vriesErrorPrefix = vriesErrorPrefix;
    }
}
