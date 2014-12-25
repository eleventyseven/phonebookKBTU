package kz.test.phonebook;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javax.servlet.annotation.WebServlet;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.event.FieldEvents;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.server.FileResource;
import com.vaadin.server.Page;
import com.vaadin.server.ResourceReference;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.GridLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Panel;
import com.vaadin.ui.Table;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.themes.Reindeer;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import jxl.CellView;
import jxl.Workbook;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;

@Theme("mytheme")
@SuppressWarnings("serial")
public class MyVaadinUI extends UI {

    @WebServlet(value = "/*", asyncSupported = true)
    @VaadinServletConfiguration(productionMode = false, ui = MyVaadinUI.class, widgetset = "kz.test.phonebook.AppWidgetSet")
    public static class Servlet extends VaadinServlet {
    }

    private Table contactList = new Table();
    private IndexedContainer contactContainer;
    public static final String ID = "Id";
    public static final String NAME = "Name";
    public static final String SURNAME = "Surname";
    public static final String PATRONOMYC = "Patronomyc";
    public static final String BIRTHDATE = "Birthdate";
    public static final String ADDRESS = "Address";
    public static final String TELEPHONE = "Telephone";
    private SimpleDateFormat formatter;
    private Object selectedObjectId;

    private Button createButton = new Button("Create");
    private Button updateButton = new Button("Update");
    private Button deleteButton = new Button("Delete");
    private Button printXlsBtn = new Button("Print xls");
    private Button printPdfBtn = new Button("Print pdf");

    private static final String[] fieldNames = new String[]{ID, NAME, SURNAME,
        PATRONOMYC, BIRTHDATE, ADDRESS, TELEPHONE};
    private GridLayout gridSearch = new GridLayout(1, 1);
    private HorizontalLayout buttonBar;

    @Override
    protected void init(VaadinRequest request) {
        initLayout();
    }

    /**
     * Initialize main window
     */
    private void initLayout() {
        contactContainer = fillTable();
        VerticalLayout layout = new VerticalLayout();
        Panel panelSearch = new Panel("Phonebook");

        panelSearch.setContent(gridSearch);
        panelSearch.setWidth("80%");

        layout.setMargin(true);
        layout.setSpacing(true);

        layout.addComponents(panelSearch, contactList, buttonBar = createButtonBar());

        layout.setComponentAlignment(panelSearch, Alignment.MIDDLE_CENTER);
        layout.setComponentAlignment(contactList, Alignment.BOTTOM_CENTER);
        layout.setComponentAlignment(buttonBar, Alignment.BOTTOM_CENTER);

        setContent(layout);
        initContactList();
        prepareSearch();
    }

    /**
     * initialize table, visible columns and listeners of the table
     *
     */
    private void initContactList() {
        contactList.setContainerDataSource(contactContainer);
        contactList.setVisibleColumns(new String[]{NAME, SURNAME, PATRONOMYC, BIRTHDATE, ADDRESS, TELEPHONE});
        contactList.setSelectable(true);
        contactList.setImmediate(true);
        contactList.setWidth("80%");
//        listener for table click and unclick events, when unclick disable update and delete buttons
        contactList.addItemClickListener(new ItemClickEvent.ItemClickListener() {

            @Override
            public void itemClick(ItemClickEvent event) {
                updateButton.setEnabled(true);
                deleteButton.setEnabled(true);
                selectedObjectId = event.getItemId();
                if (contactList.isSelected(event.getItemId())) {
                    updateButton.setEnabled(false);
                    deleteButton.setEnabled(false);
                    selectedObjectId = null;
                }
            }
        });
    }

    /**
     * initialization of create, update, delete, print xls and pdf buttons
     *
     * @return resulting buttonbar which contains all initialized buttons
     */
    private HorizontalLayout createButtonBar() {
        HorizontalLayout horizontal = new HorizontalLayout();
        horizontal.setSpacing(true);
        horizontal.setMargin(true);
        horizontal.setWidth("400px");

        horizontal.addComponents(createButton, updateButton, deleteButton, printXlsBtn, printPdfBtn);
        horizontal.setComponentAlignment(createButton, Alignment.BOTTOM_RIGHT);
        horizontal.setComponentAlignment(updateButton, Alignment.BOTTOM_RIGHT);
        horizontal.setComponentAlignment(deleteButton, Alignment.BOTTOM_RIGHT);
        horizontal.setComponentAlignment(printXlsBtn, Alignment.BOTTOM_RIGHT);
        horizontal.setComponentAlignment(printPdfBtn, Alignment.BOTTOM_RIGHT);

//        open new window with create contact fields
        createButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {

                InputFormWindow window = new InputFormWindow(null);
                window.setWidth("350px");
                window.setResizable(false);
                window.center();
                window.setModal(true);

                UI.getCurrent().addWindow(window);

            }
        });

        updateButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (selectedObjectId != null) {
                    InputFormWindow window = new InputFormWindow(contactContainer.getItem(selectedObjectId));
                    window.setWidth("350px");
                    window.setResizable(false);
                    window.center();
                    window.setModal(true);

                    UI.getCurrent().addWindow(window);

                }
            }
        });

        deleteButton.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                Long id = (Long) contactContainer.getItem(selectedObjectId).getItemProperty(ID).getValue();
                DB.deleteContact(id);
                contactList.removeItem(selectedObjectId);
                deleteButton.setEnabled(false);
                updateButton.setEnabled(false);
            }
        });

        printXlsBtn.setStyleName(Reindeer.BUTTON_LINK);
        printXlsBtn.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    File file;
                    WritableWorkbook workbook = Workbook.createWorkbook(file = File.createTempFile(UUID.randomUUID().toString().substring(0, 8), ".xls"));
                    WritableSheet sheet = workbook.createSheet("First Sheet", 0);

//                    Set font to Times New Roman, font size to 12, and text to BOLD
                    WritableFont times16font = new WritableFont(WritableFont.TIMES, 12, WritableFont.BOLD, false);
                    WritableCellFormat times16format = new WritableCellFormat(times16font);

//                    Init headers of table
                    for (int i = 1; i < fieldNames.length; i++) {
                        Label label = new Label(i - 1, 0, fieldNames[i], times16format);
                        sheet.addCell(label);
                    }

                    int row = 1;
//                    Fill data from IndexedContainer, which contains filtered data
                    for (Object itemId : contactContainer.getItemIds()) {
                        for (int i = 1; i < fieldNames.length; i++) {
                            Object value = contactList.getItem(itemId).getItemProperty(fieldNames[i]).getValue();
                            Label label = new Label(i - 1, row, value != null ? value.toString() : null);
                            sheet.addCell(label);
                        }
                        row++;
                    }
//                    Autosize the width of cells
                    for (int x = 0; x < fieldNames.length - 1; x++) {
                        CellView cell = sheet.getColumnView(x);
                        cell.setAutosize(true);
                        sheet.setColumnView(x, cell);
                    }

//                    Finally download the file
                    String MYKEY = "download";
                    FileResource res = new FileResource(file);
                    setResource(MYKEY, res);
                    ResourceReference rr = ResourceReference.create(res, UI.getCurrent().getUI(), MYKEY);
                    Page.getCurrent().open(rr.getURL(), null);
//                    file.delete();
                    workbook.write();
                    workbook.close();
                } catch (Exception ex) {
                    Logger.getLogger(MyVaadinUI.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        });

        printPdfBtn.setStyleName(Reindeer.BUTTON_LINK);
        printPdfBtn.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                try {
                    Document document = new Document();
                    File outFile;
                    PdfWriter.getInstance(document, new FileOutputStream(outFile = File.createTempFile(UUID.randomUUID().toString().substring(0, 8), ".pdf")));
                    document.open();

//                    Init headers of table
                    PdfPTable table = new PdfPTable(fieldNames.length - 1);
                    for (int i = 1; i < fieldNames.length; i++) {
                        PdfPCell cell = new PdfPCell(new Phrase(fieldNames[i]));
                        cell.setBackgroundColor(BaseColor.GRAY);
                        table.addCell(cell);
                    }

//                    Fill data from IndexedContainer, which contains filtered data
                    for (Object itemId : contactContainer.getItemIds()) {
                        for (int i = 1; i < fieldNames.length; i++) {
                            Object value = contactList.getItem(itemId).getItemProperty(fieldNames[i]).getValue();
                            PdfPCell cell = new PdfPCell(new Phrase(value != null ? value.toString() : ""));
                            cell.setVerticalAlignment(1);
                            table.addCell(cell);
                        }
                    }

                    document.add(table);
                    document.close();
//                    Finally download the file
                    String MYKEY = "download2";
                    FileResource res = new FileResource(outFile);
                    setResource(MYKEY, res);
                    ResourceReference rr = ResourceReference.create(res, UI.getCurrent().getUI(), MYKEY);
                    Page.getCurrent().open(rr.getURL(), null);
//                    outFile.delete();
                } catch (Exception ex) {
                    Logger.getLogger(MyVaadinUI.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        });

        updateButton.setEnabled(false);
        deleteButton.setEnabled(false);
        return horizontal;
    }

    /**
     * Fill table with information from database
     *
     * @return IndexedContainer which is to be added to table
     */
    private IndexedContainer fillTable() {
        formatter = new SimpleDateFormat("yyyy-MM-dd");
        IndexedContainer ic = new IndexedContainer();

//        identify the field type for IndexedContainer
        for (String p : fieldNames) {
            if (p.equalsIgnoreCase(ID)) {
                ic.addContainerProperty(p, Long.class, null);
                continue;
            }
            ic.addContainerProperty(p, String.class, "");
        }

        DB db = new DB();
        List<Contact> contacts = db.getAllContacts();
        for (Contact c : contacts) {
            Object id = ic.addItem();
            ic.getContainerProperty(id, ID).setValue(c.getId());
            ic.getContainerProperty(id, NAME).setValue(c.getName());
            ic.getContainerProperty(id, SURNAME).setValue(c.getSurname());
            ic.getContainerProperty(id, PATRONOMYC).setValue(c.getPatronomyc());
            ic.getContainerProperty(id, BIRTHDATE).setValue(c.getBirthdate() == null ? null : formatter.format(c.getBirthdate()));
            ic.getContainerProperty(id, ADDRESS).setValue(c.getAddress());
            ic.getContainerProperty(id, TELEPHONE).setValue(c.getTelephone());
        }
        return ic;
    }

    private List<TextField> searchList;

    /**
     * create fields for filtering and searching
     *
     * @param index the index of the element
     */
    public void prepareSearch() {

        searchList = new ArrayList<TextField>();
        gridSearch.setMargin(true);
        gridSearch.setSpacing(true);
        gridSearch.removeAllComponents();
        gridSearch.setColumns(7);
        gridSearch.setRows(2);

        int col = 0, row = 0;

//        add to each field it's own filter
        for (int i = 1; i < fieldNames.length; i++) {
            TextField searchTextField = new TextField(fieldNames[i]);
            searchList.add(searchTextField);
            searchTextField.setWidth("100%");
            final String fieldName = fieldNames[i];
            searchTextField.addTextChangeListener(new FieldEvents.TextChangeListener() {

                @Override
                public void textChange(FieldEvents.TextChangeEvent event) {
                    contactContainer.removeAllContainerFilters();
                    if (!event.getText().isEmpty()) {
//                        map filter
                        contactContainer.addContainerFilter(fieldName, event.getText(), true, false);
                    }
                }
            });

            gridSearch.addComponent(searchTextField, col, row);
            col++;
        }
        Button btnSearch = new Button("Search");
        Button btnClear = new Button("Clear");

        btnClear.addClickListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                contactContainer.removeAllContainerFilters();
                for (TextField f : searchList) {
                    f.setValue("");
                }
            }
        });

        gridSearch.addComponent(btnSearch, 6, 0);
        gridSearch.setComponentAlignment(btnSearch, Alignment.BOTTOM_CENTER);
        gridSearch.addComponent(btnClear, 6, 1);
        gridSearch.setComponentAlignment(btnClear, Alignment.BOTTOM_CENTER);
    }

}
