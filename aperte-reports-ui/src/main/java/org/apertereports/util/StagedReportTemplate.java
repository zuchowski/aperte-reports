package org.apertereports.util;

import org.apertereports.model.ReportTemplate;
import com.liferay.portal.model.StagedModel;


public interface StagedReportTemplate extends StagedModel{

	public ReportTemplate getAperteReport(String companyId);	
}
