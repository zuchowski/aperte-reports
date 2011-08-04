package pl.net.bluesoft.rnd.apertereports.engine;

public class SubreportNotFoundException extends Exception {

	private String[] subreportNames;

	public SubreportNotFoundException(String message, String... subreportNames) {
		super(message);
		this.subreportNames = subreportNames;
	}

	public SubreportNotFoundException(String message, Throwable cause, String... subreportNames) {
		super(message, cause);
		this.subreportNames = subreportNames;
	}

	public String[] getReportName() {
		return subreportNames;
	}

}
