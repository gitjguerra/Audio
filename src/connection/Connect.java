/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package connection;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;
import model.DocFile;
import util.Constants;
 
/**
 * Basic connection to PostgreSQL database. 
 *
 * @author jguerra
 */
public class Connect {
 
    private String driver = null;
    private String path = null;
    private String user = null;
    private String password = null;    
    private Properties prop = new Properties();
    private InputStream input = null;        

    public Connect() {
        try {
            
            String cwd = System.getProperty("user.dir");        
            input = new FileInputStream(cwd + Constants.getPROPERTIES_FILE());

            // load a properties file
            prop.load(input);

            // get the property value and print it out
            driver = prop.getProperty("driver");
            path = prop.getProperty("path");
            user = prop.getProperty("user");
            password = prop.getProperty("password");

        } catch (FileNotFoundException ex) {
            Logger.getLogger(Connect.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Connect.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
                if (input != null) {
                        try {
                           input.close();
                        } catch (IOException e) {
                            System.out.println("Error al intentar leer los parametros del archivo config.properties " + e);
                        }
                }
        }            
    }
        
    /**
     * We establish the connection with the database <b>customerdb</b>.
     * @return connection
     */
    public Connection connectDb() {
        
        try {
            // Register the PostgreSQL driver
            Class.forName(driver);

            Connection connection = null;
            connection = DriverManager.getConnection(path, user, password);
            return connection;

        } catch (java.sql.SQLException sqle) {
            System.out.println("Error al conectar con la base de datos de PostgreSQL " + sqle);
        } catch (ClassNotFoundException ex) {
            System.out.println("Error al registrar el driver de PostgreSQL: " + ex);
        }            
        return null;
    }
 
    /**
     * Method to connect to the database by passing parameters.
     * 
     * @param host <code>String</code> host name or ip. Nombre del host o ip.
     * @param port <code>String</code> listening database port. Puerto en el que escucha la base de datos.
     * @param database <code>String</code> database name for the connection. Nombre de la base de datos para la conexión.
     * @param user <code>String</code> user name. Nombre de usuario.
     * @param password  <code>String</code> user password. Password del usuario.
     * @return connection
     */
    public Connection connectDb(String host, String port, String database,String user, String password) {
        String url = "";
        try {
            // Register the PostgreSQL driver
            try {
                Class.forName(driver);
            } catch (ClassNotFoundException ex) {
                System.out.println("Error al registrar el driver de PostgreSQL: " + ex);
            }
            Connection connection = null;
            url = "jdbc:postgresql://" + host + ":" + port + "/" + database;
            connection = DriverManager.getConnection(url,user, password);           
            return connection;
        } catch (java.sql.SQLException sqle) { 
            JOptionPane.showMessageDialog(null, "Error al conectar con la base de datos de PostgreSQL (" + url + "): " + sqle);
            System.out.println("Error al conectar con la base de datos de PostgreSQL (" + url + "): " + sqle);
        }
        return null;
    }
        
    public static void saveAudio(Connection connection, DocFile docFile) {
        PreparedStatement pst = null;
        try {
            pst = connection.prepareStatement("insert into docfile (rep_id, doc_desc, doc_path, "
                    + " doc_filename, doc_status, doc_type, creation, user_id, uniqueid) " 
                    + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");
             
            pst.setInt(1, docFile.getRepId());
            pst.setString(2, docFile.getDocDesc());
            pst.setString(3, docFile.getDocPath());
            pst.setString(4, docFile.getDocFileName());
            pst.setInt(5, docFile.getDocStatus());
            pst.setString(6, docFile.getDocType());
            pst.setTimestamp(7, docFile.getCreation());
            pst.setInt(8, docFile.getUserId());
            pst.setString(9, docFile.getUniqueId());
            pst.execute();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(null, e.getMessage());
            System.out.println("Error al intentar insertar en la base de datos de PostgreSQL " + e);
        }finally{
            if(pst!=null){
                try{
                    pst.close();
                }catch (SQLException e){
                    JOptionPane.showMessageDialog(null, e.getMessage());
                    System.out.println("Error al intentar cerrar el objeto PreparedStatement " + e);
                }
            }              
            if(connection!=null){
                try{
                    connection.close();
                }catch (SQLException e){
                    JOptionPane.showMessageDialog(null, e.getMessage());
                    System.out.println("Error al intentar cerrar el objeto Connection " + e);
                }
            }
        }
    }
}
