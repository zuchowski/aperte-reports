package pl.net.bluesoft.rnd.apertereports.exception;

/**
 * The exception that indicates an error anywhere. The GUI tier should catch this exception and
 * present it to the user.
 */
public class VriesException extends Exception {
    private String title;

    public VriesException() {
    }

    public VriesException(String message) {
        super(message);
    }

    public VriesException(String title, String message) {
        super(message);
        this.title = title;
    }

    public VriesException(String title, String message, Throwable cause) {
        super(message, cause);
        this.title = title;
    }

    public VriesException(String message, Throwable cause) {
        super(message, cause);
    }

    public VriesException(Throwable cause) {
        super(cause);
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

}
