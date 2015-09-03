package org.apertereports.components;

import com.vaadin.ui.Component;
import com.vaadin.ui.ComponentContainer;
import com.vaadin.ui.CssLayout;

public class LiferayAccordion extends CssLayout {

	CssLayout contentWrapper;

	public LiferayAccordion(CssLayout header, Component content) {
		super();
		CssLayout topRow = new CssLayout();
		this.addComponent(topRow);
		this.addStyleName("accordion-group lfr-panel lfr-panel-extended");
		topRow.addStyleName("accordion-heading toggler-header toggler-header-expanded");
		CssLayout row = new CssLayout();
		topRow.addComponent(row);
		row.addStyleName("accordion-toggle");
		row.addStyleName("clear");
		row.addComponent(header);

		addContent(content);
	}

	public void addContent(Component content) {
		if (content != null) {
			contentWrapper = new CssLayout();
			contentWrapper.addStyleName("toggler-content-wrapper");

			CssLayout contentWrapperB = new CssLayout();
			contentWrapperB.addStyleName("toggler-content toggler-content-expanded");
			contentWrapper.addComponent(contentWrapperB);

			content.addStyleName("accordion-inner");			
			contentWrapperB.addComponent(content);

			this.addComponent(contentWrapper);
			content.setSizeUndefined();
		}
	}

	public void removeContent() {
		if (contentWrapper != null) {
			this.removeComponent(contentWrapper);
		}
	}

}
