/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package kz.test.phonebook;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.sql.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import oracle.jdbc.pool.OracleDataSource;

/**
 *
 * @author KKK
 */
public class DB {

    private static String dbName = "phonebook";
    private static String userid = "phonebook";
    private static String password = "book";
    private static String jdbcUrl = "jdbc:oracle:thin:@localhost:1521:ORCL";

    /**
     * Open connection to database
     *
     * @return the opened connection
     */
    private static Connection getConnection() throws SQLException {
        OracleDataSource ds;
        ds = new OracleDataSource();
        ds.setURL(jdbcUrl);
        Connection conn = ds.getConnection(userid, password);
        return conn;
    }

    /**
     * Insert the new created contact to database, using stored procedure in
     * database
     *
     *
     * @param name of the Contact
     * @param surname of the Contact
     * @param patronomyc of the Contact
     * @param birthdate of the Contact
     * @param address of the Contact
     * @param telephone of the Contact
     * @param image of the Contact
     *
     */
    public static void insertContact(String name, String surname, String patronomyc, java.util.Date birthdate, String address, String telephone, byte[] image) {
        Connection conn = null;
        CallableStatement proc = null;

        try {
            conn = getConnection();

            proc = conn.prepareCall("INSERT INTO PHONEBOOK.CONTACT (ID,\"NAME\", SURNAME, PATRONOMYC, BIRTHDATE, ADDRESS, TELEPHONE, PHOTO) \n"
                    + "	VALUES (CONTACT_SEQ.nextval, ?, ?, ?, ?, ?, ?, ?)\n"
                    + "");
            proc.setString(1, name);
            proc.setString(2, surname);
            proc.setString(3, patronomyc);
            proc.setDate(4, birthdate != null ? new Date(birthdate.getTime()) : null);
            proc.setString(5, address);
            proc.setString(6, telephone);
            proc.setBytes(7, image);
            proc.execute();
        } catch (Exception ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                proc.close();
            } catch (SQLException e) {
            }
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static Contact getContactByNameAndSurname(String name, String surname) {
        Connection conn = null;
        CallableStatement proc = null;

        try {
            conn = getConnection();

            proc = conn.prepareCall("SELECT ID, NAME, SURNAME, PATRONOMYC, BIRTHDATE, ADDRESS, TELEPHONE FROM CONTACT"
                    + " WHERE NAME = ? AND SURNAME = ?");
            proc.setString(1, name);
            proc.setString(2, surname);
            ResultSet rs = proc.executeQuery();
            if (!rs.next()) {
                return null;
            }
            Contact contact = new Contact();
            contact.setId(rs.getLong(1));
            contact.setName(rs.getString(2));
            contact.setSurname(rs.getString(3));
            contact.setPatronomyc(rs.getString(4));
            contact.setBirthdate(rs.getDate(5));
            contact.setAddress(rs.getString(6));
            contact.setTelephone(rs.getString(7));
            return contact;
        } catch (Exception ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                proc.close();
            } catch (SQLException e) {
            }
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return null;
    }

    /**
     * Update contact with the given id, using stored procedure in database
     *
     *
     * @param id of the Contact
     * @param name of the Contact
     * @param surname of the Contact
     * @param patronomyc of the Contact
     * @param birthdate of the Contact
     * @param address of the Contact
     * @param telephone of the Contact
     * @param image of the Contact
     *
     */
    public static void updateContact(Long id, String name, String surname, String patronomyc, java.util.Date birthdate, String address, String telephone, byte[] image) {
        Connection conn = null;
        CallableStatement proc = null;

        try {
            conn = getConnection();

            proc = conn.prepareCall("UPDATE CONTACT SET NAME = ?, SURNAME = ?, PATRONOMYC = ?, BIRTHDATE = ?, ADDRESS = ?, TELEPHONE = ?, PHOTO = ? \n"
                    + "	WHERE ID = ?");
            proc.setString(1, name);
            proc.setString(2, surname);
            proc.setString(3, patronomyc);
            proc.setDate(4, birthdate != null ? new Date(birthdate.getTime()) : null);
            proc.setString(5, address);
            proc.setString(6, telephone);
            proc.setBytes(7, image);
            proc.setLong(8, id);
            proc.execute();
        } catch (Exception ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                proc.close();
            } catch (SQLException e) {
            }
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Delete contact with the given id, using stored procedure in database
     *
     *
     * @param id of the Contact
     *
     */
    public static void deleteContact(Long id) {
        Connection conn = null;
        CallableStatement proc = null;

        try {
            conn = getConnection();

            proc = conn.prepareCall("{ call PHONEBOOK.DELETE_CONTACT(?) }");
            proc.setLong(1, id);
            proc.execute();
        } catch (Exception ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                proc.close();
            } catch (SQLException e) {
            }
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void deleteAllContacts() {
        Connection conn = null;
        CallableStatement proc = null;

        try {
            conn = getConnection();

//            proc = conn.prepareCall("DELETE FROM CONTACT");
            Statement statement = conn.createStatement();
            statement.execute("DELETE FROM CONTACT where ID > 0");
//            proc.execute();
        } catch (Exception ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
//            try {
//                proc.close();
//            } catch (SQLException e) {
//            }
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Get the photo of the contact according to the given id, using stored
     * procedure in database
     *
     *
     * @param id of the Contact
     *
     */
    public static byte[] getPhoto(Long id) {
        Connection conn = null;
        CallableStatement proc = null;

        try {
            conn = getConnection();

            proc = conn.prepareCall("SELECT PHOTO FROM CONTACT WHERE ID = ?");
            proc.setLong(1, id);
            proc.execute();
            ResultSet rs = proc.executeQuery();
            if (!rs.next()) {
                return null;
            }
            byte[] bytes = rs.getBytes(1);
            return bytes;
        } catch (Exception ex) {
            Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } finally {
            try {
                proc.close();
            } catch (SQLException e) {
            }
            try {
                conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    /**
     * Get all contacts from the database
     *
     *
     * @return List of contacts
     */
    public static List<Contact> getAllContacts() {
        Connection conn = null;
        Statement stmt = null;
        List<Contact> result = new ArrayList<Contact>();
        String query
                = "select * from " + dbName + ".CONTACT";

        try {
            conn = getConnection();
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                Contact c = new Contact();
                c.setName(rs.getString("name"));
                c.setSurname(rs.getString("surname"));
                c.setPatronomyc(rs.getString("patronomyc"));
                c.setBirthdate(rs.getDate("birthdate"));
                c.setAddress(rs.getString("address"));
                c.setTelephone(rs.getString("telephone"));
                c.setId(rs.getLong("id"));
                result.add(c);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            if (stmt != null && conn != null) {
                try {
                    stmt.close();
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(DB.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return result;
    }

}
