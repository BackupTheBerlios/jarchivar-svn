/*
 * DBManager.java
 *
 * Created on 24. Februar 2006, 19:48
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jarchivar.sql;

import java.sql.*;
import java.util.ArrayList;
import java.util.Hashtable;
import org.jarchivar.data.DBFileInfo;
import org.jarchivar.data.FileInfo;

/**
 *
 * @author Lars Wuckel
 */
public class DBManager {

  private String jdbcDriver = "";
  private String jdbcUrl = "";
  private Statement  statement;
  private Connection connection;
  String insertFileInfo = "INSERT INTO datei(hash,laenge,name) VALUES (?, ? ,?)";
  String insertFileArchive = "INSERT INTO dateiarchiv VALUES(?,?)";
  String insertFilePath = "INSERT INTO dateipath VALUE(?,?)";
  String insertPath = "INSERT INTO path VALUE(?)";
  String insertArchive = "INSERT INTO archiv VALUE(?)";
  
  /** Creates a new instance of DBManager */
  public DBManager (String jdbcDriver, String jdbcUrl, String user, String password) throws Exception{
    Class.forName ( jdbcDriver );
        
    connection = DriverManager.getConnection ( jdbcUrl, user, password);
    connection.setAutoCommit (false);
    statement = connection.createStatement ();    
  }

  public ArrayList<DBFileInfo> getDBFileInfos(int archiveID) throws Exception{
    
    ArrayList<DBFileInfo> list = new ArrayList<DBFileInfo>();
    
    ResultSet rs = statement.executeQuery ("SELECT * FROM dateiinfo WHERE archive_id=" + archiveID);
            
    while(rs.next ()){
              
      DBFileInfo dbFileInfo = new DBFileInfo(rs);
      
      list.add (dbFileInfo);
    }

    return list;
  }
  
  public Hashtable getDBFileInfos() throws Exception{
    
    Hashtable<String, ArrayList<DBFileInfo>> hashTable = new Hashtable<String, ArrayList<DBFileInfo>>();
    
    ResultSet rs = statement.executeQuery ("SELECT * FROM dateiinfo");
            
    while(rs.next ()){
              
      DBFileInfo dbFileInfo = new DBFileInfo(rs);
      ArrayList<DBFileInfo> list = (ArrayList<DBFileInfo>)hashTable.get (dbFileInfo.getHash ());
      
      if ( list == null ){
        
        list = new ArrayList<DBFileInfo>();
        hashTable.put (dbFileInfo.getHash (),list);
      }
      
      list.add (dbFileInfo);
    }

    return hashTable;
  }
  
  public Hashtable getHashFileIDHashtable() throws Exception{
    
    Hashtable<String,Integer> hashTable = new Hashtable<String,Integer>();
    
    ResultSet rs = statement.executeQuery ("SELECT id,hash FROM datei");
            
    while(rs.next ()){

      hashTable.put (rs.getString (2), rs.getInt (1));
    }

    return hashTable;
  }  

  public Hashtable getFileIDPathIDHashtable() throws Exception{
    
    Hashtable<Integer,ArrayList> hashTable = new Hashtable<Integer,ArrayList>();
    
    ResultSet rs = statement.executeQuery ("SELECT datei_id, pfad_id FROM dateipfad ORDER BY datei_id");
    
    int currId = -1;
    ArrayList<Integer> pathIdList = null;
            
    while(rs.next ()){

      int fileID = rs.getInt (1);
      int pathId = rs.getInt (2);
    
      if (currId != fileID){
        pathIdList = new ArrayList<Integer>();
        hashTable.put (fileID, pathIdList);
        currId = fileID;
      }
      
      pathIdList.add(pathId);
    }

    return hashTable;
  }  
  
  public Hashtable getPathIDHashtable() throws Exception{
    
    Hashtable<String,Integer> hashTable = new Hashtable<String,Integer>();
    
    ResultSet rs = statement.executeQuery ("SELECT id,name FROM pfad");
            
    while(rs.next ()){

      hashTable.put (rs.getString (2), rs.getInt (1));
    }

    return hashTable;
  }  

  public Hashtable getArchiveIDHashtable() throws Exception{
    
    Hashtable<String,Integer> hashTable = new Hashtable<String,Integer>();
    
    ResultSet rs = statement.executeQuery ("SELECT id,name FROM archiv");
            
    while(rs.next ()){

      hashTable.put (rs.getString (2), rs.getInt (1));
    }

    return hashTable;
  }  

  int getID(String select, String parameter) throws Exception{
    int id = -1;
    
    PreparedStatement stmt = connection.prepareStatement (select);

    stmt.setString (1,parameter);
    ResultSet rs = stmt.executeQuery ();
    
    if (rs.next ()){

      id = rs.getInt (1);      
    }

    return id;
  }  

  public int getPathID(String path) throws Exception{

    return getID ("SELECT id FROM path WHERE name = ?", path);
  }  
  
  public int getArchiveID(String name) throws Exception{

    return getID ("SELECT id FROM archiv WHERE name=?", name);
  }

  public int addFileInfo(FileInfo fileInfo) throws Exception{
    String hash = fileInfo.getHash ();
    String name = fileInfo.getName ();
    long length = fileInfo.getLength ();
    
    PreparedStatement stmt = connection.prepareStatement (insertFileInfo,Statement.RETURN_GENERATED_KEYS);
    stmt.setString (1,hash);
    stmt.setString (2,name);
    stmt.setLong (3,length);
    
    stmt.execute ();
    
    ResultSet rs = stmt.getGeneratedKeys ();
    
    return rs.getInt (1);
  }
  
  public int addArchiv(String name) throws Exception{
    PreparedStatement stmt = connection.prepareStatement (insertArchive,Statement.RETURN_GENERATED_KEYS);
    stmt.setString (1,name);

    stmt.execute ();
    ResultSet rs = stmt.getGeneratedKeys ();

    return rs.getInt (1);
  }
  
  public int addPath(String path) throws Exception{
    PreparedStatement stmt = connection.prepareStatement (insertPath,Statement.RETURN_GENERATED_KEYS);
    stmt.setString (1,path);

    stmt.execute ();
    ResultSet rs = stmt.getGeneratedKeys ();

    return rs.getInt (1);
  }  
  
  public void setFilePath(int fileID, int pathID) throws Exception{
    PreparedStatement stmt = connection.prepareStatement ("INSERT INTO dateipfad VALUES(?,?)");
    stmt.setInt (1,fileID);
    stmt.setInt (2,pathID);

    stmt.execute ();
  }
  
  public void setFileArchive(int fileID, int archiveID) throws Exception{
    PreparedStatement stmt = connection.prepareStatement ("INSERT INTO dateiarchiv VALUES(?,?)");
    stmt.setInt (1,fileID);
    stmt.setInt (2,archiveID);

    stmt.execute ();
  }  
  
  public void commit() throws Exception{
    this.connection.commit ();
  }
  
  public void rollback() throws Exception{
    this.connection.rollback ();
  }

  public void removeArchive(int archiveID) throws Exception{
    
    statement.addBatch ( "DELETE FROM dateiarchiv WHERE archiv_id=" + archiveID);
    statement.addBatch ( "DELETE FROM archiv WHERE id=" + archiveID);
    statement.addBatch ( "DELETE FROM datei WHERE datei_id NOT IN( SELECT datei_id FROM dateiarchiv )");
    statement.addBatch ( "DELETE FROM dateipfad WHERE datei_id NOT IN( SELECT ID FROM datei )");
    statement.addBatch ( "DELETE FROM pfad WHERE id NOT IN( SELECT pfad_id FROM dateipfad )");
    statement.executeBatch ();    
  }
}
