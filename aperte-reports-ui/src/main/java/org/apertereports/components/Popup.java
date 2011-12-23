package org.apertereports.components;

import org.apertereports.util.VaadinUtil;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;

public class Popup extends Window {

	private static final String POPUP_APPLY = "popup.apply";
	private static final String POPUP_DISCARD = "popup.discard";
	private Form form;
	
	public Popup(Form form) {
		this.form = form;
		init();
	}

	public void init() {
		addComponent(form);
		setScrollable(false);
		setResizable(false);
		setModal(true);
		setCloseShortcut(KeyCode.ESCAPE, null);
		setWidth("300px");
		
		// The cancel / apply buttons
        HorizontalLayout buttons = new HorizontalLayout();
        buttons.setSpacing(true);
        Button discardChanges = new Button(VaadinUtil.getValue(POPUP_DISCARD),
                new Button.ClickListener() {
                    public void buttonClick(ClickEvent event) {
                    	close();
                    }
                });
        buttons.addComponent(discardChanges);
        buttons.setComponentAlignment(discardChanges, Alignment.MIDDLE_LEFT);

        Button apply = new Button(VaadinUtil.getValue(POPUP_APPLY), new Button.ClickListener() {
            public void buttonClick(ClickEvent event) {
                try {
                	if(Popup.this.form != null)
                	Popup.this.form.commit();
                	close();
                } catch (InvalidValueException e) {
//                	NOOP
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        buttons.addComponent(apply);
        addComponent(buttons);
	}

	
}
