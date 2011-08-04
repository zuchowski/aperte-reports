package pl.net.bluesoft.rnd.apertereports.engine;

import java.util.HashMap;
import java.util.Map;

public class EmptySubreportProvider implements SubreportProvider {

	@Override
	public Map<String, Subreport> getSubreports(String... reportNames) {
		return new HashMap<String, Subreport>();
	}

}
