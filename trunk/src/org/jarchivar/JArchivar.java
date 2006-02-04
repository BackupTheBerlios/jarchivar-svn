/*
 * Main.java
 *
 * Created on 1. Mai 2004, 12:24
 */

package org.jarchivar;

import java.util.Properties;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import org.jarchivar.io.SHA1Calculator;
import java.sql.*;

/**
 *
 * @author  Lars Wuckel
 */
public class JArchivar {
  
  public static boolean DEBUG = false;
  
  protected String jdbcDriver = "", jdbcUrl = "";
  protected String path = "";
  protected Statement  statement;
  protected Connection connection;
  protected String lowerCase = "lower";
  
  /** Creates a new instance of Main */
  public JArchivar (String[] args) {
    
    try{
      Properties prop = new Properties ();
      File file = new File ("jArchivar.ini");
      
      if (file.exists ()){
        
        prop.load (new FileInputStream (file));
        
        jdbcDriver = prop.getProperty ("jdbcdriver");
        jdbcUrl = prop.getProperty ("jdbcurl");
        String user = prop.getProperty ("user");
        String password = prop.getProperty ("password");
        lowerCase = prop.getProperty ("lowercase");
        
        Class.forName ( jdbcDriver );
        
        connection = DriverManager.getConnection ( jdbcUrl, user, password);
        connection.setAutoCommit (false);
        statement = connection.createStatement ();
        
        String command = "";
        
        if (args.length >0)
          command = args[0].toLowerCase ();
        
        if (command.equalsIgnoreCase ("add") && args.length == 3 && new File (args[1]).exists ()){

          path = args[1];
          
          try{
            ResultSet rs = statement.executeQuery ("SELECT id from archive where name='" + args[2].replace ("'","''") + "'");
            
            if(!rs.next ()){
              statement.execute ("INSERT INTO archive(name) VALUES ('" + args[2].replace ("'","''") +  "')");
            }
          }
          catch(java.sql.SQLException e){
            
            // Wert existiert bereits in der DB
            if (e.getErrorCode () != -9){
              System.out.println ("Fehlernumer: " + e.getErrorCode ());
              e.printStackTrace ();
            }
          }
          catch(Exception e){
          }
          finally{
            try{
              connection.commit ();
            }
            catch(Exception e){
            }
          }
          
          add (args[1],args[2]);
          
        }
        else if (command.equalsIgnoreCase ("remove") && args.length == 2 ){
          remove (args[1]);
        }
        else if (command.equalsIgnoreCase ("list") && args.length >= 1 ){
          if (args.length > 1)
            list (args[1].replace ("'","''"));
          else
            list (null);
        }
        else if (command.equalsIgnoreCase ("listarchive") ){
          listArchive ();
        }
        else if (command.equalsIgnoreCase ("same") && args.length == 2){
          same (args[1]);
        }
        else if (command.equalsIgnoreCase ("find") && args.length == 2 ){
          find (args[1]);
        }
        else if (command.equalsIgnoreCase ("finddir") && args.length == 2){
          findPath (args[1]);
        }
        else {
          System.out.println ("JArchivar Version 0.1a (c) 2004 by Lars Wuckel");
          System.out.println ();
          System.out.println ("command: JArchivar add | remove | list | listarchive | find | compare | help");
          
          System.out.println ();
          System.out.println ("add <path> <archive-name>");
          System.out.println ("   -fügt die Dateien vom angegebenen Pfad rekursiv dem Archiv hinzu.");
          System.out.println ("remove <archive-name>");
          System.out.println ("diff <path>");
          System.out.println ("   -listet alle Dateien des angegebenen Pfades (rekursiv) auf, die noch nicht erfasst wurden.");
          System.out.println ("same <path>");
          System.out.println ("   -listet alle Dateien des angegebenen Pfades (rekursiv) auf, die noch bereits erfasst wurden.");
          System.out.println ("list <archive-name>");
          System.out.println ("   -listet alle Dateien des angegebenen Archivs auf.");
          System.out.println ("find <datei-name>");
          System.out.println ("   -listet Archive auf, welche die Datei enthalten.");
          System.out.println ("finddir <pfad-name>");
          System.out.println ("   -listet alle Dateien aller Archive auf, welche in einem bestimmten Pfad enthalten sind.");
          System.out.println ("help");
          System.out.println ("   -Zeigt diesen Hilfetext an");
        }
      }
    }
    catch(Exception e){
      e.printStackTrace ();
    }
  }
  
  /**
   * @param args the command line arguments
   */
  public static void main (String[] args) {
    new JArchivar (args);
  }
  
  private void add (String path, String archName){
    
    if (DEBUG)
      System.out.println ("add " + path + " Archive: " + archName);
    
    File file = new File (path);
    
    File[] files = file.listFiles ();
    
    for(int i=0; i < files.length; ++i){
      try{
        
        if (files[i].isDirectory ())
          add (files[i].getAbsolutePath (),archName);
        
        else{
          SHA1Calculator calc = new SHA1Calculator (files[i]);
          String hash = calc.getSHA1 ();
          boolean added = false;
          String fileName = files[i].getName ().replace ("'","''");
          
          // Datei eintragen
          
          try{
            ResultSet rs = statement.executeQuery ("SELECT id from datei where hash='" + hash + "'");
            
            if(!rs.next ()){
              if(DEBUG)
                System.out.println ("datei hinzufügen" + hash);
              
              statement.execute ("INSERT INTO datei(hash,laenge,name) VALUES ('" + hash + "', " + files[i].length () +  " ,'" + fileName + "')");
              added = true;
            }
          }
          catch(java.sql.SQLException e){
            // Wert existiert noch nicht in der DB
            e.printStackTrace ();
          }
          catch(Exception e){
          }
          
          if (added)
            System.out.print ("+ ");
          else
            System.out.print ("- ");
          
          System.out.println (hash + " " + files[i].getName ());
          
          // Datei archiv Verknüpfung
          try{
            ResultSet rs = statement.executeQuery ("SELECT datei_id from dateiarchive where datei_id = (select id from datei where hash='" + hash + "') and archive_id=(SELECT id from archive where name='" + archName + "')");
            
            if(!rs.next ()){
              
              statement.execute ("INSERT INTO dateiarchive(datei_id, archive_id) VALUES ((SELECT id from datei where hash='"+hash +"') , (SELECT id from archive where name='"+archName +"') )");
            }
          }
          catch(java.sql.SQLException e){
            // Wert existiert nocht nicht in der DB
            e.printStackTrace ();
          }
          catch(Exception e){
          }
          
          // relativen Pfad hinzufügen
          String relPath = files[i].getPath ().replace (this.path,"").replace (files[i].getName (), "").replace ("'","''");
          
          try{
            
            ResultSet rs = statement.executeQuery ("SELECT id from pfad where name='" + relPath + "'");
            
            if(!rs.next ()){
              
              statement.execute ("INSERT INTO pfad(name) VALUES ('" + relPath + "')");
            }
          }
          catch(java.sql.SQLException e){
            e.printStackTrace ();
          }
          catch(Exception e){
          }
          
          try{
            
            ResultSet rs = statement.executeQuery ("SELECT datei_id from dateipfad WHERE datei_id=(SELECT id from datei where hash='" + hash + "') AND pfad_id = (SELECT id FROM pfad WHERE name='" + relPath + "')");
            
            if(!rs.next ()){
              
              if(DEBUG)
                System.out.println ("pfad mit datei verknüpfen");
              
              statement.execute ("INSERT INTO dateipfad (datei_id, pfad_id) VALUES ((SELECT id from datei where hash='"+hash +"'), (SELECT id from pfad where name ='" + relPath + "'))");
            }
          }
          catch(java.sql.SQLException e){
            e.printStackTrace ();
          }
          catch(Exception e){
          }
          
          connection.commit ();
        }
      }
      catch(Exception e){
        System.out.println ("Fehler bei der Datei: " + files[i]);
        e.printStackTrace ();
      }
    }
  }
  
  private void remove (String archName){
    try{
      statement.execute ("DELETE FROM pfad WHERE id in (SELECT pfad_id from dateipfad inner join dateiarchive on dateipfad.datei_id = dateiarchive.datei_id inner join archive on dateiarchive.archive_id = (select id from archive where name='"+archName +"'))");
    }
    catch(java.sql.SQLException e){
      e.printStackTrace ();
    }
    
    try{
      statement.execute ("DELETE FROM datei WHERE id in (SELECT datei_id from dateiarchive where archive_id = (select id from archive where name='"+archName +"'))");
    }
    catch(java.sql.SQLException e){
      e.printStackTrace ();
    }
    
    try{
      statement.execute ("DELETE FROM archive WHERE name='"+archName +"'");
      connection.commit ();
    }
    catch(java.sql.SQLException e){
      e.printStackTrace ();
    }
    
  }
  
  private void find (String dateiName){
    try{
      String dName = dateiName.replace ("'", "''").replace ("%","\\%").replace ("_","\\_").toLowerCase ();
      ResultSet rs = statement.executeQuery ("SELECT archive.name, a.name as datei, a.laenge , a.hash, pfad.name as pfad FROM (SELECT id, name, laenge, hash FROM datei WHERE  " + lowerCase + "(name) like '%" + dName + "%') a inner join dateiarchive on dateiarchive.datei_id = a.id inner join archive on archive.id = dateiarchive.archive_id inner join dateipfad on a.id = dateipfad.datei_id inner join pfad on dateipfad.pfad_id = pfad.id");
      
      while(rs.next ())
        System.out.println (rs.getString ("name") + " " + rs.getString ("datei") + " " + rs.getString ("laenge") + " " + rs.getString ("hash") + " " + rs.getString ("pfad"));
    }
    catch(java.sql.SQLException e){
      e.printStackTrace ();
    }
  }
  
  private void findPath (String pathName){
    try{
      String pName = pathName.replace ("'", "''").replace ("%","\\%").replace ("_","\\_").toLowerCase ();
      ResultSet rs = statement.executeQuery ("SELECT archive.name, datei.name as datei, datei.laenge , p.name as pfad FROM (select id, name from pfad where " + lowerCase + "(name) like '%" + pName + "%') p INNER JOIN dateipfad on dateipfad.pfad_id = p.id inner join datei on datei.id = dateipfad.datei_id inner join dateiarchive on datei.id= dateiarchive.datei_id inner join archive on archive.id = dateiarchive.archive_id" );
      
      while(rs.next ())
        System.out.println (rs.getString ("name") + " "  + rs.getString ("pfad") + " " + rs.getString ("datei") + " " + rs.getString ("laenge") );
    }
    catch(java.sql.SQLException e){
      e.printStackTrace ();
    }
  }
  
  private void listArchive (){
    try{
      ResultSet rs = statement.executeQuery ("SELECT name from archive");
      
      while(rs.next ())
        System.out.println (rs.getString ("name"));
    }
    catch(java.sql.SQLException e){
      e.printStackTrace ();
    }
  }
  
  private void list (String archiveName){
    try{
      String where ="";
      
      if (archiveName != null && archiveName.length () >0 )
        where = " AND archive.name='" + archiveName + "'";
      
      ResultSet rs = statement.executeQuery ("SELECT datei.hash,datei.laenge, datei.name as datei, archive.name as archiv, pfad.name as pfad FROM datei inner join dateiarchive on dateiarchive.datei_id = datei.id inner join archive on archive.id = dateiarchive.archive_id" + where + " inner join dateipfad on datei.id = dateipfad.datei_id inner join pfad on dateipfad.pfad_id = pfad.id");
      
      while(rs.next ())
        System.out.println (rs.getString ("archiv") + " " + rs.getString ("datei") + " " + rs.getString ("laenge") + " " +rs.getString ("hash") + " " +rs.getString ("pfad"));
    }
    catch(java.sql.SQLException e){
      e.printStackTrace ();
    }
  }
  
  private void diff (String path){
    File file = new File (path);
    
    File[] files = file.listFiles ();
    
    try{
      for(int i=0; i < files.length; ++i){
        
        if (files[i].isDirectory ())
          diff (files[i].getAbsolutePath ());
        
        else{
          SHA1Calculator calc = new SHA1Calculator (files[i]);
          String hash = calc.getSHA1 ();
          
          try{
            ResultSet rs = statement.executeQuery ("SELECT id FROM datei where hash='" + hash + "'");
            
            if(!rs.next ())
              System.out.println (files[i].getAbsolutePath ());
          }
          catch(java.sql.SQLException e){
            if (DEBUG)
              e.printStackTrace ();
          }
        }
      }
    }
    catch(Exception e){
      if (DEBUG)
        e.printStackTrace ();
    }
  }
  
  private void same (String path){
    File file = new File (path);
    
    File[] files = file.listFiles ();
    
    try{
      for(int i=0; i < files.length; ++i){
        
        if (files[i].isDirectory ())
          same (files[i].getAbsolutePath ());
        
        else{
          SHA1Calculator calc = new SHA1Calculator (files[i]);
          String hash = calc.getSHA1 ();
          
          try{
            ResultSet rs = statement.executeQuery ("SELECT id FROM datei where hash='" + hash + "'");
            
            if(rs.next ())
              System.out.println (files[i]);
          }
          catch(java.sql.SQLException e){
            if (DEBUG)
              e.printStackTrace ();
          }
        }
      }
    }
    catch(Exception e){
      if (DEBUG)
        e.printStackTrace ();
    }
  }
}
