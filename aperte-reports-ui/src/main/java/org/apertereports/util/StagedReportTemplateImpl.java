package org.apertereports.util;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.apertereports.model.ReportTemplate;

import com.liferay.portal.model.Role;
import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.lar.StagedModelType;
import com.liferay.portal.service.RoleLocalServiceUtil;
import com.liferay.portlet.expando.model.ExpandoBridge;
import com.liferay.portlet.expando.util.ExpandoBridgeFactoryUtil;


public class StagedReportTemplateImpl implements StagedReportTemplate {
	
	public StagedReportTemplateImpl(ReportTemplate pReportTemplate) throws Exception {
		super();		
		
		this.reportTemplate = pReportTemplate;
		
		Set<Object> rolesWithAccess = new HashSet<Object>();
		try{
			for(long roleId : pReportTemplate.getRolesWithAccess()){
				Role role = RoleLocalServiceUtil.getRole(roleId);
				String rolename = role.getName();
				rolesWithAccess.add(rolename);
			}
		}catch(PortalException pe){
			throw new PortalException("Could not convert RoleID to RoleName.", pe);
		}

		this.rolesWithAccess = rolesWithAccess;
		this.active = pReportTemplate.getActive();
		this.content = pReportTemplate.getContent();
		Date createdDate = new Date();
		createdDate.setTime(pReportTemplate.getCreated().getTime());
		this.created = createdDate;
		this.description = pReportTemplate.getDescription();
		this.filename = pReportTemplate.getFilename();
		this.allowOnlineDisplay = pReportTemplate.getAllowOnlineDisplay();
		this.allowBackgroundOrder = pReportTemplate.getAllowBackgroundOrder();
		this.id = pReportTemplate.getId();
		this.reportname = pReportTemplate.getReportname();		
		if(pReportTemplate.getCompanyId() != null && !pReportTemplate.getCompanyId().isEmpty())
			this.setCompanyId(Long.valueOf(pReportTemplate.getCompanyId()));
	}

	//reportTemplate Object
	private transient ReportTemplate reportTemplate = null;
	
	protected Set<Object> rolesWithAccess = new HashSet<Object>();
	protected Boolean active;
	protected String content;
	protected Date created;
	protected String description;
	protected String filename;
	protected Boolean allowOnlineDisplay;
	protected Boolean allowBackgroundOrder;
	protected Integer id;
	protected String reportname;			
	private long companyId;

	public Object clone() {
		return this;
	}

	public ReportTemplate getReportTemplate() {
		return reportTemplate;
	}

	public void setReportTemplate(ReportTemplate reportTemplate) {
		this.reportTemplate = reportTemplate;
	}

	public Set<Object> getRolesWithAccess() {
		return rolesWithAccess;
	}

	public void setRolesWithAccess(Set<Object> rolesWithAccess) {
		this.rolesWithAccess = rolesWithAccess;
	}

	public Boolean getActive() {
		return active;
	}

	public void setActive(Boolean active) {
		this.active = active;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Boolean getAllowOnlineDisplay() {
		return allowOnlineDisplay;
	}

	public void setAllowOnlineDisplay(Boolean allowOnlineDisplay) {
		this.allowOnlineDisplay = allowOnlineDisplay;
	}

	public Boolean getAllowBackgroundOrder() {
		return allowBackgroundOrder;
	}

	public void setAllowBackgroundOrder(Boolean allowBackgroundOrder) {
		this.allowBackgroundOrder = allowBackgroundOrder;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getReportname() {
		return reportname;
	}

	public void setReportname(String reportname) {
		this.reportname = reportname;
	}

	@Override
	public Date getCreateDate() {
		// TODO Auto-generated method stub
		return this.created;
	}

	@Override
	public Date getModifiedDate() {
		// TODO Auto-generated method stub
		return this.getCreateDate();
	}

	@Override
	public StagedModelType getStagedModelType() {
		return new StagedModelType(this.getModelClass());
	}
	
	@Override
	public String getUuid() {
		// TODO Auto-generated method stub
		return this.id.toString();
	}

	@Override
	public void setCreateDate(Date date) {
		this.created = date;		
	}

	@Override
	public void setModifiedDate(Date date) {
		this.setCreateDate(date);
	}

	@Override
	public void setUuid(String uuid) {
		this.id = new Integer(uuid);		
	}

	@Override
	public ExpandoBridge getExpandoBridge() {
		return ExpandoBridgeFactoryUtil.getExpandoBridge(this.getCompanyId(),this.getModelClassName());
	}
	
	@Override
	public Class<?> getModelClass() {
	
		return ReportTemplate.class;
	}

	@Override
	public String getModelClassName() {
		
		return this.getModelClass().getName();
	}

	@Override
	public Serializable getPrimaryKeyObj() {
		return new Long(this.id);
	}

	@Override
	public void setPrimaryKeyObj(Serializable primaryKeyObj) {
		this.id = new Integer(primaryKeyObj.toString());
	}
	
	public void setCompanyId(long companyId) {
		this.companyId = companyId;
	}
	
	@Override
	public long getCompanyId() {
		return this.companyId;
	}
		
	public ReportTemplate getAperteReport(String companyId) throws Exception {
		
		reportTemplate = new ReportTemplate();	
		reportTemplate.setCompanyId(companyId);
		
		Set<Long> rolesWithAccess = new HashSet<Long>();
		try{
			for(Object roleName : this.rolesWithAccess){
				if(roleName instanceof String){
					String name = (String) roleName;
					Role role = RoleLocalServiceUtil.getRole(Long.parseLong(companyId), name);
					Long roleId = role.getRoleId();
					rolesWithAccess.add(roleId);
				}
			}
		}catch(PortalException pe){
			throw new PortalException("Could not convert RoleName to RoleID.", pe);
		}
		
		reportTemplate.setRolesWithAccess(rolesWithAccess);
		reportTemplate.setActive(this.active);
		reportTemplate.setContent(this.content);
		reportTemplate.setCreated(this.created);
		reportTemplate.setDescription(this.description);
		reportTemplate.setFilename(this.filename);
		reportTemplate.setAllowOnlineDisplay(this.allowOnlineDisplay);
		reportTemplate.setAllowBackgroundOrder(this.allowBackgroundOrder);
		reportTemplate.setReportname(this.reportname);
		
		return reportTemplate;
	}		
}