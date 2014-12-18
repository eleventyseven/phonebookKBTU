package kz.kbtu.edu.test;

import junit.framework.TestCase;
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

    public void testInsertContact() {
        assertNotNull(DB.getContacts());
    }
}
