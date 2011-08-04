package pl.net.bluesoft.rnd.apertereports.engine;

import pl.net.bluesoft.rnd.apertereports.common.exception.SubreportNotFoundException;

import java.util.HashMap;
import java.util.Map;

public class MapBasedSubreportProvider implements SubreportProvider {

	Map<String, Subreport> map = new HashMap<String, Subreport>();

	public MapBasedSubreportProvider(Map<String, Subreport> subreportMap) {
		super();
		this.map = subreportMap;
	}

	@Override
	public Map<String, Subreport> getSubreports(String... reportNames) throws SubreportNotFoundException {
		Map<String, Subreport> newMap = new HashMap<String, Subreport>();
		if (reportNames != null) {
			for (String reportName : reportNames) {
				if (map.containsKey(reportName)) {
					newMap.put(reportName, map.get(reportName));
				} else {
					throw new SubreportNotFoundException("Subreport " + reportName + " not found", reportName);
				}
			}
		}
		return newMap;
	}

	public Map<String, Subreport> getMap() {
		return map;
	}

	public void setMap(Map<String, Subreport> map) {
		this.map = map;
	}

}
