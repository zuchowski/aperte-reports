package org.apertereports.util;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.portlet.PortletPreferences;

import org.apertereports.common.users.User;
import org.apertereports.common.users.UserRole;
import org.apertereports.dao.ReportTemplateDAO;
import org.apertereports.model.ReportTemplate;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.kernel.lar.BasePortletDataHandler;
import com.liferay.portal.kernel.lar.PortletDataContext;
import com.liferay.portal.kernel.lar.PortletDataHandlerBoolean;
import com.liferay.portal.kernel.lar.PortletDataHandlerControl;
import com.liferay.portal.kernel.lar.StagedModelDataHandlerUtil;
import com.liferay.portal.kernel.xml.Element;
import com.liferay.portal.model.Role;
import com.liferay.portal.security.auth.PrincipalThreadLocal;
import com.liferay.portal.service.CompanyLocalServiceUtil;
import com.liferay.portal.service.UserLocalServiceUtil;

public class AperteReportsDataHandler extends BasePortletDataHandler {
	
	protected static final String NAMESPACE_REPORTS = "reports";
	
	@Override
	public PortletDataHandlerControl[] getExportControls() {
		PortletDataHandlerBoolean reportsData = new PortletDataHandlerBoolean(NAMESPACE_REPORTS, "Reports", true, false);
		return new PortletDataHandlerControl[] {reportsData};
	}
	
	@Override
	public PortletDataHandlerControl[] getImportControls() {
		return getExportControls();
	}
	
	@Override
	protected String doExportData(PortletDataContext portletDataContext, String portletId,
			PortletPreferences portletPreferences) throws Exception {
		Element rootElement = addExportDataRootElement(portletDataContext);

		if (portletDataContext.getBooleanParameter(NAMESPACE_REPORTS, "Reports")) {
			this.exportReports(portletDataContext);
		}

		return getExportDataRootElementString(rootElement);		
	}
	
	@Override
	protected PortletPreferences doImportData(PortletDataContext portletDataContext, String portletId,
			PortletPreferences portletPreferences, String data) throws Exception {
		
		//Mit dem neuen Aperte JSF Portlet wird nur noch das "Invokerportlet_WAR_apertereportsportlet" exportiert.
		//Darum muss ab sofort auf contains "Invokerportlet_WAR_apertereportsportlet" geprüft werden.
		//Früher wurde auf contains "INSTANCE" geprüft. Die INSTANCE wird aber seit dem neuen Portlet nicht mehr exportiert
		//Alte LARs funktionieren weiterhin, da deren portletId mit "Invokerportlet_WAR_apertereportsportlet_INSTANCE" beginnt
		if (!portletId.contains("Invokerportlet_WAR_apertereportsportlet"))
			return portletPreferences;

		Element productsElement = portletDataContext.getImportDataRootElement();

		if (portletDataContext.getBooleanParameter(NAMESPACE_REPORTS, "Reports")
				&& productsElement.element("ReportTemplate") != null) {

			List<Element> eTables = (List<Element>) productsElement.element("ReportTemplate").elements();
			
			for (Element eField : eTables) {
				StagedModelDataHandlerUtil.importStagedModel(portletDataContext, eField);
			}			
		}
		
		return portletPreferences;		
	}
	
	protected PortletPreferences doDeleteData(PortletDataContext portletDataContext, String portletId,
			PortletPreferences portletPreferences) throws Exception {
				
		if (portletDataContext.addPrimaryKey(
				AperteReportsDataHandler.class, "deleteData")) {
			return portletPreferences;
		}		
		this.deleteAllReports();
		
		return portletPreferences;
	}
	
	protected void exportReports(PortletDataContext portletDataContext)
			throws Exception {
		
		Collection<ReportTemplate> reportTemplates = ReportTemplateDAO.fetch(AperteReportsDataHandler.getAperteUser(), null, 0, 1000);  
				
		for (ReportTemplate reportTemplate : reportTemplates) {
			StagedReportTemplate stagedReport = new StagedReportTemplateImpl(reportTemplate);
			StagedModelDataHandlerUtil.exportStagedModel(portletDataContext, stagedReport);						
		}				
	}
	
	
	/*
	 * Get all reports and delete all reports
	 */
	protected void deleteAllReports() throws PortalException, SystemException{
		
		//check if user PK is present
		if(PrincipalThreadLocal.getUserId() == 0)
			return;
		
		Collection<ReportTemplate> reportTemplates = ReportTemplateDAO.fetch(AperteReportsDataHandler.getAperteUser(), null, 0, 1000);  
		
		for (ReportTemplate reportTemplate : reportTemplates) {
			ReportTemplateDAO.remove(reportTemplate);
		}
		
	}
	
	/*
	 * In Anlehung an AbstractReportingApplication handleRenderRequest(...)
	 */
	public static User getAperteUser() throws PortalException, SystemException{
		
		com.liferay.portal.model.User liferayUser = UserLocalServiceUtil.getUser(PrincipalThreadLocal.getUserId());
		com.liferay.portal.model.Company company = CompanyLocalServiceUtil.getCompany(liferayUser.getCompanyId());
		long userid= liferayUser.getUserId();
    	long portletGroupId= liferayUser.getGroupId();    	
    	long companyid = liferayUser.getCompanyId();
    	String webid = company.getWebId();
        String login = liferayUser.getLogin();
        String email = liferayUser.getEmailAddress();
        Set<UserRole> roles = new HashSet<UserRole>();
        boolean admin = false;

        for (Role r : liferayUser.getRoles()) {
            boolean adminRole = "administrator".equalsIgnoreCase(r.getName());
            UserRole ur = new UserRole(r.getName(), r.getRoleId(), adminRole);
            roles.add(ur);
            admin |= adminRole;
        }
        User user = new User(login, roles, admin, email, userid, portletGroupId, companyid, webid);
        return user;        
	}
	
	
}
