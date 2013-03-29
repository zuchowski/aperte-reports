package org.apertereports.engine;

import java.util.HashMap;
import java.util.Map;
import org.apertereports.common.ARConstants.ErrorCode;
import org.apertereports.common.exception.ARException;

public class MapBasedSubreportProvider implements SubreportProvider {

	private Map<String, Subreport> map = new HashMap<String, Subreport>();

	public MapBasedSubreportProvider(Map<String, Subreport> subreportMap) {
		super();
		this.map = subreportMap;
	}

	@Override
	public Map<String, Subreport> getSubreports(String... reportNames) throws ARException {
		Map<String, Subreport> newMap = new HashMap<String, Subreport>();
		if (reportNames != null) {
			for (String reportName : reportNames) {
				if (map.containsKey(reportName)) {
					newMap.put(reportName, map.get(reportName));
				} else {
					throw new ARException(ErrorCode.SUBREPORT_NOT_FOUND, reportName);
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
