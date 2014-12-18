/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.test.phonebook;

import com.vaadin.data.Item;
import com.vaadin.data.Validator;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.StreamResource;
import com.vaadin.server.ThemeResource;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Link;
import com.vaadin.ui.Notification;
import com.vaadin.ui.PopupDateField;
import com.vaadin.ui.TextField;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.Upload.SucceededEvent;
import com.vaadin.ui.Upload.SucceededListener;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author KKK
 */
public class InputFormWindow extends Window {

    private Button save;
    private Button cancel;
    private TextField nameField;
    private TextField surnameField;
    private TextField patronomycField;
    private PopupDateField birthdateField;
    private TextField addressField;
    private TextField telephoneField;
    private Item item;
    private Link image;
    private byte[] bimage;
    private Upload upload;

    /**
     * Constructor of the form window
     *
     * @param item the selected row from the table, which contains contact info
     *
     */
    public InputFormWindow(final Item item) {
        this.setCaption("Contact");
        this.item = item;

        VerticalLayout vLay = new VerticalLayout();
        vLay.setSpacing(true);
        FormLayout form = new FormLayout();
//initialize fields
        nameField = new TextField("Name");
        nameField.setRequired(true);
        nameField.setRequiredError("Name field is required");

        surnameField = new TextField("Surname");
        surnameField.setRequired(true);
        surnameField.setRequiredError("Surame field is required");

        patronomycField = new TextField("Patronomyc");
        birthdateField = new PopupDateField("Birthdate");
        addressField = new TextField("Address");
        telephoneField = new TextField("Telephone");

//        default image for contact without avatar
        ThemeResource resource = new ThemeResource("1.png");
        image = new Link();
        image.setIcon(resource);
        image.setStyleName("mylogo");

//        invoked when contact is updated
        if (item != null) {
//            fill the fields with contact data from table
            fillForm();
//            render image of contact, getting it from database
            byte[] photo = DB.getPhoto((Long) item.getItemProperty(MyVaadinUI.ID).getValue());
            if (photo != null) {
                final ByteArrayInputStream bis = new ByteArrayInputStream(photo);
                image.setIcon(new StreamResource(new StreamResource.StreamSource() {

                    @Override
                    public InputStream getStream() {
                        return bis;
                    }
                }, "Click to change"));
            }

        }

        ImageUploader receiver = new ImageUploader();

// Create the upload with a caption and set receiver later
        upload = new Upload("", receiver);
        upload.setButtonCaption("Start Upload");
        upload.addSucceededListener(receiver);

// Put the components in a panel
        VerticalLayout imageContent = new VerticalLayout();
        imageContent.addComponents(upload, image);
        imageContent.setComponentAlignment(image, Alignment.MIDDLE_CENTER);
        vLay.addComponents(imageContent, form);

        save = new Button("save");
        cancel = new Button("cancel");

        HorizontalLayout hor = new HorizontalLayout();
        hor.setSpacing(true);
        hor.setMargin(true);
        hor.addComponents(save, cancel);

        form.addComponents(nameField, surnameField, patronomycField, birthdateField, addressField, telephoneField, hor);
        form.setComponentAlignment(hor, Alignment.MIDDLE_CENTER);
        form.setMargin(true);
        setContent(vLay);

        initClickListeners();
    }

    /**
     * Listeners for save and cancel buttons
     *
     *
     */
    private void initClickListeners() {
        save.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (validateFields()) {
                    if (item == null) {
//                        insert contact to database
                        DB.insertContact(nameField.getValue(), surnameField.getValue(), patronomycField.getValue(),
                                birthdateField.getValue(), addressField.getValue(), telephoneField.getValue(), bimage);
                    } else {
//                        update contact in database
                        DB.updateContact((Long) item.getItemProperty(MyVaadinUI.ID).getValue(), nameField.getValue(), surnameField.getValue(), patronomycField.getValue(),
                                birthdateField.getValue(), addressField.getValue(), telephoneField.getValue(), bimage);
                    }
                    Page.getCurrent().reload();
                    close();
                }
            }
        });

        cancel.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                close();
            }
        });

    }

    /**
     * Fill the fields with contact data from table
     *
     */
    public void fillForm() {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date parse = null;
        try {
            parse = item.getItemProperty(MyVaadinUI.BIRTHDATE).getValue() != null ? formatter.parse(item.getItemProperty(MyVaadinUI.BIRTHDATE).getValue().toString()) : null;
        } catch (ParseException ex) {
            Logger.getLogger(InputFormWindow.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        nameField.setValue(item.getItemProperty(MyVaadinUI.NAME).getValue() != null ? item.getItemProperty(MyVaadinUI.NAME).getValue().toString() : "");
        surnameField.setValue(item.getItemProperty(MyVaadinUI.SURNAME).getValue() != null ? item.getItemProperty(MyVaadinUI.SURNAME).getValue().toString() : "");
        patronomycField.setValue(item.getItemProperty(MyVaadinUI.PATRONOMYC).getValue() != null ? item.getItemProperty(MyVaadinUI.PATRONOMYC).getValue().toString() : "");
        birthdateField.setValue(parse);
        addressField.setValue(item.getItemProperty(MyVaadinUI.ADDRESS).getValue() != null ? item.getItemProperty(MyVaadinUI.ADDRESS).getValue().toString() : "");
        telephoneField.setValue(item.getItemProperty(MyVaadinUI.TELEPHONE).getValue() != null ? item.getItemProperty(MyVaadinUI.TELEPHONE).getValue().toString() : "");
    }

    /**
     * Check if required fields are valid, if not show notification
     *
     */
    private boolean validateFields() {
        try {
            nameField.validate();
            surnameField.validate();
        } catch (Validator.InvalidValueException ex) {
            Notification notif = new Notification(ex.getMessage());
            notif.show(Page.getCurrent());
            return false;
        }
        return true;
    }

    class ImageUploader implements Receiver, SucceededListener {

        public File file;

//        Method for uploading image to avatar
        public OutputStream receiveUpload(String filename,
                String mimeType) {
            // Create upload stream
            FileOutputStream fos = null; // Stream to write to
            try {
                // Open the file for writing.
                file = File.createTempFile(filename, null);
                fos = new FileOutputStream(file);
            } catch (Exception e) {
                new Notification("Could not open file<br/>",
                        e.getMessage(),
                        Notification.Type.ERROR_MESSAGE)
                        .show(Page.getCurrent());
                return null;
            }
            return fos;
        }
// if succeeded then change the avatar, and save to temp, after clicking save will commit to database

        public void uploadSucceeded(SucceededEvent event) {
            image.setIcon(new FileResource(file));
            try {
                bimage = FileUtils.readFileToByteArray(file);
            } catch (IOException ex) {
                Logger.getLogger(InputFormWindow.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    };

}
