package org.apertereports.engine;

import java.util.HashMap;
import java.util.Map;

import org.apertereports.common.ReportConstants.ErrorCodes;
import org.apertereports.common.exception.AperteReportsException;

public class MapBasedSubreportProvider implements SubreportProvider {

	Map<String, Subreport> map = new HashMap<String, Subreport>();

	public MapBasedSubreportProvider(Map<String, Subreport> subreportMap) {
		super();
		this.map = subreportMap;
	}

	@Override
	public Map<String, Subreport> getSubreports(String... reportNames) throws AperteReportsException {
		Map<String, Subreport> newMap = new HashMap<String, Subreport>();
		if (reportNames != null) {
			for (String reportName : reportNames) {
				if (map.containsKey(reportName)) {
					newMap.put(reportName, map.get(reportName));
				} else {
					throw new AperteReportsException(ErrorCodes.SUBREPORT_NOT_FOUND, reportName);
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
