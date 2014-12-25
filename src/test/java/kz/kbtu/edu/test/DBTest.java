package kz.kbtu.edu.test;

import java.util.Date;
import junit.framework.TestCase;
import kz.test.phonebook.Contact;
import kz.test.phonebook.DB;

/**
 *
 * @author FanskiY
 */
public class DBTest extends TestCase {

    public DBTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
    }

    //initial data
    String name = "SERIK";
    String surname = "NURMYSHEV";
    String patronomyc = "ARMANOVICH";
    String tel = "87079602090";
    Date birth = new Date();
    String address = "ASTANA";

    public void testInsertContact() {
//        delete all contacts from database to make tests from zero
        DB.deleteAllContacts();
//        insert contact with data
        DB.insertContact(name, surname, patronomyc, birth, address, tel, null);
//        check if contact successfully inserted, it should not be equal to 0
        assertNotSame(0, DB.getAllContacts().size());
//        find contact, check whether inserted contact's data match with searched contact
        Contact contact = DB.getContactByNameAndSurname(name, surname);
//        compare name
        assertEquals(name, contact.getName());
//        compare surname
        assertEquals(surname, contact.getSurname());
//        compare address
        assertEquals(address, contact.getAddress());
//        compare telephone
        assertEquals(tel, contact.getTelephone());

    }

//    updated data
    String toUpdname = "SERIK";
    String toUpdsurname = "NURMYSHEV";
    String toUpdpatronomyc = "ARMANOVICH";
    String toUpdtel = "87079602090";
    Date toUpdbirth = new Date();
    String toUpdaddress = "ASTANA";

    public void testUpdate() {
//        to fill with data
        DB.deleteAllContacts();
        testInsertContact();

//        find contact, check whether inserted contact's data match with searched contact
        Contact contact = DB.getContactByNameAndSurname(name, surname);
//        update contact
        DB.updateContact(contact.getId(), toUpdname, toUpdsurname, toUpdpatronomyc, toUpdbirth, toUpdaddress, toUpdtel, null);
//        find updated contact, check whether updated contact's data match with searched contact        
        contact = DB.getContactByNameAndSurname(toUpdname, toUpdsurname);
        assertEquals(toUpdname, contact.getName());
        assertEquals(toUpdsurname, contact.getSurname());
        assertEquals(toUpdaddress, contact.getAddress());
        assertEquals(toUpdtel, contact.getTelephone());
    }

    public void testDeleteContact() {
//        tp fill with data
        testUpdate();

//        find updated contact,
        Contact contact = DB.getContactByNameAndSurname(toUpdname, toUpdsurname);
//        delete contact
        DB.deleteContact(contact.getId());
//        search deleted contact, should return null
        contact = DB.getContactByNameAndSurname(toUpdname, toUpdsurname);
//        check for null
        assertNull(contact);
//        also there should be no record in database and contacts size == 0
        assertEquals(0, DB.getAllContacts().size());
    }

    public void failureTest() {
        assertNull("asdasd");
    }

}
