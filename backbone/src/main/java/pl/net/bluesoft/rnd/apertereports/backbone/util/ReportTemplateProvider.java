package pl.net.bluesoft.rnd.apertereports.backbone.util;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;

import pl.net.bluesoft.rnd.apertereports.common.utils.ReportGeneratorUtils;
import pl.net.bluesoft.rnd.apertereports.dao.ReportTemplateDAO;
import pl.net.bluesoft.rnd.apertereports.engine.SubreportNotFoundException;
import pl.net.bluesoft.rnd.apertereports.engine.SubreportProvider;
import pl.net.bluesoft.rnd.apertereports.model.ReportTemplate;

public class ReportTemplateProvider implements SubreportProvider {
	private static final Logger logger = Logger.getLogger(ReportTemplateProvider.class.getName());

	@Override
	public Map<String, Subreport> getSubreports(String... reportNames) throws SubreportNotFoundException {
		List<ReportTemplate> list = ReportTemplateDAO.fetchReportsByNames(reportNames);
		Map<String, Subreport> map = new HashMap<String, Subreport>(list.size());

		for (ReportTemplate temp : list) {
			byte[] content = new byte[] {};
			try {
				content = ReportGeneratorUtils.decodeContent(temp.getContent());
			} catch (UnsupportedEncodingException e) {
				logger.log(Level.SEVERE, "Exception decoding report", e);
			}
			map.put(temp.getReportname(), new Subreport(temp.getReportname(), temp.getId().toString(), content));
		}
		
		Collection<String> notFound = CollectionUtils.subtract(Arrays.asList(reportNames), map.keySet());
		if(!notFound.isEmpty()){
			String[] notFoundArray = (String[]) notFound.toArray(new String[notFound.size()]);
			String message = "Subreports not found: " + StringUtils.join(notFoundArray, ", ");
			throw new SubreportNotFoundException(message, notFoundArray);
		}

		return map;
	}
}
