package org.apertereports.components;

import com.vaadin.data.Validator.InvalidValueException;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Form;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Window;
import org.apertereports.ui.UiFactory;
import org.apertereports.ui.UiFactory.FAction;

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
        //HorizontalLayout buttons = UiFactory.createHLayout(this, FAction.SET_SPACING);
        CssLayout buttons = new CssLayout() ;
        this.addComponent(buttons);
        Button tmpButton1 = UiFactory.createButton(POPUP_DISCARD, buttons, null, new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                close();
            }
        }, FAction.ALIGN_LEFT);
        tmpButton1.addStyleName("btn");
        Button tmpButton2 = UiFactory.createButton(POPUP_APPLY, buttons, new Button.ClickListener() {

            @Override
            public void buttonClick(ClickEvent event) {
                try {
                    if (Popup.this.form != null) {
                        Popup.this.form.commit();
                    }
                    close();
                } catch (InvalidValueException e) {
//                	NOOP
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
        tmpButton2.addStyleName("btn");
    }
}
