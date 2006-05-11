/*
 * DBManager.java
 *
 * Created on 24. Februar 2006, 19:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jarchivar.sql;

import java.util.*;
import java.sql.*;
import java.net.*;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.lang.StringBuilder;
import org.jarchivar.io.HashCalculator;
import org.jarchivar.diagnostic.Debug;

/**
 * 
 * @author Lars Wuckel
 */
public class DBManager {

    private Statement statement;

    private Connection connection;

    /** Creates a new instance of DBManager */
    public DBManager(String jdbcPath, String jdbcDriver, String jdbcUrl, String user,
            String password) throws Exception {
        
        URL[] urls = new URL[1];
        urls[0] = new URL("file://" + jdbcPath);
        URLClassLoader cl = new URLClassLoader(urls);
        Class drvCls = Class.forName(jdbcDriver, true, cl);
        Driver driver = (Driver)drvCls.newInstance(); // Driver driver = (Driver)        

        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);        
        connection =  driver.connect(jdbcUrl, props);
        // connection.setAutoCommit (false);
        statement = connection.createStatement();
    }

    public ArrayList<File> importFileData(String offsetPath, String archiveName,
        ArrayList<File> fileList, HashCalculator hashCalc) throws Exception {
        
        ArrayList<File> errorFileList = new ArrayList<File>();
        
        statement.clearBatch();

        if (fileList.size() > 0) {
                        
            File pathFile = new File(offsetPath);
            offsetPath = pathFile.getCanonicalPath();
            int offset = offsetPath.length();

            String insert = "INSERT INTO IMPORT(Archiv_Name,Pfad,Pfad_Suchmuster,Hash,Laenge,Datei_Name,Datei_Suchmuster) VALUES (";
                        
            Debug.println("insert into import");
            if (archiveName != null)
                insert += "'" + archiveName + "','";
            else
                insert += "NULL,'";

            StringBuilder builderInsert = new StringBuilder();
            StringBuilder builderFile = new StringBuilder();

            for (File file : fileList) {
                try{
                Debug.println(file.getPath());

                builderInsert.setLength(0);
                builderInsert.append(insert);

                // PFAD
//                Debug.println("pfad");
//                Debug.println(file.getParent());
                builderFile.setLength(0);
                builderFile.append(file.getParent());
                builderFile.delete(0, offset);
                replaceApo(builderFile);
                builderInsert.append(builderFile.toString());
                builderInsert.append("','");

//                Debug.println("pfad suchmuster");
                // PFAD_SUCHMUSTER
                builderFile.setLength(0);
                builderFile.append(file.getParent());
                builderFile.delete(0, offset);
                createSearchPattern(builderFile);
                builderInsert.append(builderFile.toString().toLowerCase());
                builderInsert.append("','");

//                Debug.println("hash");
                // Hash
                builderInsert.append(hashCalc.getHash(file));
                builderInsert.append("',");

//                Debug.println("laenge");
                // LAENGE
                builderInsert.append(file.length());
                builderInsert.append(",'");

//                Debug.println("datei name");
                // DATEI_NAME
                builderFile.setLength(0);
                builderFile.append(file.getName());
                replaceApo(builderFile);
                builderInsert.append(builderFile.toString());
                builderInsert.append("','");

//                Debug.println("dateisuchmuster");
                // Datei_Suchmuster
                builderFile.setLength(0);
                builderFile.append(file.getName());
                createSearchPattern(builderFile);
                builderInsert.append(builderFile.toString().toLowerCase());

                builderInsert.append("');");

                String out = builderInsert.toString();
//                Debug.println(out);
                statement.execute(out);
//                Debug.println("ausgeführt");
                }
                catch(IOException e){
                    errorFileList.add(file);
                }
            }

            Debug.println("Geschrieben!");
        }
        
        return errorFileList;
    }
    
    public void freeImport() throws Exception{
        statement.execute("DELETE FROM IMPORT;");
    }

    public void updateImport() throws Exception {
        Debug.println("Update IMPORT FROM ARCHIV");
        // 
        statement
                .execute("UPDATE IMPORT,ARCHIV SET IMPORT.archiv_id=ARCHIV.ID WHERE IMPORT.archiv_name=ARCHIV.name;");
        Debug.println("Update IMPORT FROM DATEI");
        statement
        .execute("UPDATE IMPORT,DATEI SET IMPORT.datei_id=DATEI.ID WHERE IMPORT.hash=DATEI.hash AND IMPORT.datei_name=DATEI.name AND IMPORT.laenge=DATEI.laenge;");
        Debug.println("Update IMPORT FROM PFAD");        
        statement
        .execute("UPDATE IMPORT,PFAD SET IMPORT.pfad_id=PFAD.ID WHERE IMPORT.pfad=PFAD.name;");
        Debug.println("Update IMPORT FROM DATEIPFAD");        
        statement
        .execute("UPDATE IMPORT,DATEIPFAD SET IMPORT.datei_pfad_id=DATEIPFAD.ID WHERE IMPORT.datei_id=DATEIPFAD.datei_id AND IMPORT.pfad_id=DATEIPFAD.pfad_id;");
        
//        statement.executeBatch();
    }
    
    public void saveToDatabase() throws Exception {
        
        Debug.println("save");
        // löschen der bereits importierten Zeilen
        statement
        .execute("DELETE IMPORT FROM IMPORT,DATEIPFADARCHIV WHERE IMPORT.datei_pfad_id=DATEIPFADARCHIV.dateipfad_id AND IMPORT.archiv_id=DATEIPFADARCHIV.archiv_id;");

        Debug.println("INSERT INTO ARCHIV");
        // insert Archiv
        statement
        .execute("INSERT INTO ARCHIV(name) SELECT DISTINCT archiv_name FROM IMPORT WHERE IMPORT.archiv_id is null;");
        Debug.println("UPDATE IMPORT FROM ARCHIV");
        statement
        .execute("UPDATE IMPORT,ARCHIV SET IMPORT.archiv_id=ARCHIV.ID WHERE IMPORT.archiv_name=ARCHIV.name;");

        // insert Dateien
        Debug.println("INSERT INTO DATEI");
        statement
        .execute("INSERT INTO DATEI(hash,laenge,name,suchmuster) SELECT DISTINCT hash,laenge,datei_name,datei_suchmuster FROM IMPORT WHERE IMPORT.datei_id is null;");
        Debug.println("UPDATE IMPORT FROM DATEI");
        statement
        .execute("UPDATE IMPORT,DATEI SET IMPORT.datei_id=DATEI.ID WHERE IMPORT.hash=DATEI.hash;");

        // insert Pfade
        Debug.println("INSERT INTO PFAD");
        statement
        .execute("INSERT INTO PFAD(name,suchmuster) SELECT DISTINCT pfad,pfad_suchmuster FROM IMPORT WHERE IMPORT.pfad_id is null;");
        Debug.println("UPDATE IMPORT FROM PFAD ");
        statement
        .execute("UPDATE IMPORT,PFAD SET IMPORT.pfad_id=PFAD.ID WHERE IMPORT.pfad=PFAD.name;");

        // insert dateipfadIDs
        Debug.println("INSERT INTO DATEIPFAD");
        statement
        .execute("INSERT INTO DATEIPFAD(datei_id,pfad_id) SELECT DISTINCT datei_id,pfad_id FROM IMPORT WHERE DATEI_PFAD_ID is null;");
        Debug.println("UPDATE IMPORT FROM DATEIPFAD ");
        statement
        .execute("UPDATE IMPORT,DATEIPFAD SET IMPORT.datei_pfad_id=DATEIPFAD.ID WHERE IMPORT.datei_id=DATEIPFAD.datei_id AND IMPORT.pfad_id=DATEIPFAD.pfad_id;");

        // insert dateipfadarchiv
        Debug.println("INSERT INTO DATEIPFADARCHIV");
        statement
        .execute("INSERT INTO DATEIPFADARCHIV(dateipfad_id,archiv_id) SELECT DISTINCT datei_pfad_id,archiv_id FROM IMPORT;");        
        
        Debug.println("DELETE aufgenommene Daten");
        // löschen der bereits importierten Zeilen
        statement
        .execute("DELETE IMPORT FROM IMPORT,DATEIPFADARCHIV WHERE IMPORT.datei_pfad_id=DATEIPFADARCHIV.dateipfad_id AND IMPORT.archiv_id=DATEIPFADARCHIV.archiv_id;");
//        statement.executeBatch();
    }

    public void findNewFiles() throws Exception {

        String select = "SELECT pfad,datei_name FROM IMPORT WHERE datei_id IS NULL ORDER BY datei_name;";
        
        ResultSet rs = statement.executeQuery(select);
        printResultSet(rs);
    }

    public void findExistingFiles() throws Exception {

        String select = "SELECT pfad,datei_name FROM IMPORT WHERE datei_id IS NOT NULL ORDER BY datei_name;";
        
        ResultSet rs = statement.executeQuery(select);
        printResultSet(rs);
    }
    
    public void findFileName(String name) throws Exception {

        String select = "SELECT archiv,pfad,datei_name FROM DATEIINFO WHERE datei_suchmuster like '%" + name +"%' ORDER BY archiv";
        
        ResultSet rs = statement.executeQuery(select);
        printResultSet(rs);
    }

    public void findPathName(String name) throws Exception {

        String select = "SELECT archiv,pfad,datei_name FROM DATEIINFO WHERE pfad_suchmuster like '%" + name +"%' ORDER BY archiv;";
        
        ResultSet rs = statement.executeQuery(select);
        printResultSet(rs);
    }

    public void listImportedArchive() throws Exception {

        String select = "SELECT DISTINCT archiv_name FROM IMPORT ORDER BY archiv_name;";
        
        ResultSet rs = statement.executeQuery(select);
        printResultSet(rs);
    }
    
    public void listArchive() throws Exception {

        String select = "SELECT name FROM ARCHIV ORDER BY name;";
        
        ResultSet rs = statement.executeQuery(select);
        printResultSet(rs);
    }

    public void listArchiveFiles(String archiveName) throws Exception {

        String select = "SELECT archiv,pfad,datei_name FROM DATEIINFO WHERE archiv_id = (SELECT ID FROM ARCHIV WHERE name = '" + archiveName + "') ORDER BY archiv;";
        
        ResultSet rs = statement.executeQuery(select);
        printResultSet(rs);
    }
    
    private void printResultSet(ResultSet rs) throws Exception{
        ResultSetMetaData rsmd = rs.getMetaData();        
        int count = rsmd.getColumnCount();
        
        while(rs.next()){            
            
            for(int i=1; i <= count; ++i){
                System.out.print(rs.getObject(i));
                System.out.print(" ");
            }
            System.out.println();
        }        
    }
    
    private void createSearchPattern(StringBuilder textBuilder) {
        for (int i = 0; i < textBuilder.length(); ++i) {
            switch (textBuilder.charAt(i)) {
            case 'ä':
                textBuilder.setCharAt(i, 'a');
                ++i;
                textBuilder.insert(i, 'e');
                break;
            case 'ö':
                textBuilder.setCharAt(i, 'o');
                ++i;
                textBuilder.insert(i, 'e');
                break;
            case 'ü':
                textBuilder.setCharAt(i, 'u');
                ++i;
                textBuilder.insert(i, 'e');
                break;
            case 'ß':
                textBuilder.setCharAt(i, 's');
                ++i;
                textBuilder.insert(i, 's');
                break;
            case '\'':
                textBuilder.delete(i, i + 1);
                --i;
                break;
            }
        }
    }

    private void replaceApo(StringBuilder builder) {

        for (int i = 0; i < builder.length(); ++i) {
            if (builder.charAt(i) == '\'') {
                builder.delete(i, i + 1);
                --i;
            }
        }
    }

    int getID(String select, String parameter) throws Exception {
        int id = -1;

        PreparedStatement stmt = connection.prepareStatement(select);

        stmt.setString(1, parameter);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {

            id = rs.getInt(1);
        }

        return id;
    }

    public int getPathID(String path) throws Exception {

        return getID("SELECT id FROM PFAD WHERE name = ?", path);
    }

    public int getArchiveID(String name) throws Exception {

        return getID("SELECT ID FROM ARCHIV WHERE NAME=?", name);
    }

    private int getGeneratedID(PreparedStatement stmt) throws Exception {
        ResultSet rs = stmt.getGeneratedKeys();

        int id;

        if (rs.next())
            id = rs.getInt(1);
        else
            id = -1;

        return id;
    }

    public void commit() throws Exception {
        this.connection.commit();
    }

    public void rollback() throws Exception {
        this.connection.rollback();
    }

    public void removeArchive(int archiveID) throws Exception {

        statement.addBatch("DELETE FROM DATEIPFADARCHIV WHERE archiv_id="
                + archiveID);
        statement.addBatch("DELETE FROM ARCHIV WHERE id=" + archiveID);
        statement
                .addBatch("DELETE FROM DATEIPFAD WHERE id NOT IN( SELECT DATEIPFAD_ID FROM DATEIPFADARCHIV )");
        statement
                .addBatch("DELETE FROM DATEI WHERE id NOT IN( SELECT datei_id FROM DATEIPFAD)");
        statement
                .addBatch("DELETE FROM PFAD WHERE id NOT IN( SELECT pfad_id FROM DATEIPFAD )");
        statement.executeBatch();
    }
}
