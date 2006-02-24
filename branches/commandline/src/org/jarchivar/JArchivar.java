/*
 * Main.java
 *
 * Created on 1. Mai 2004, 12:24
 */

package org.jarchivar;

import java.util.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileInputStream;
import org.jarchivar.data.DBFileInfo;
import org.jarchivar.data.FileInfo;
import org.jarchivar.io.FileManager;
import org.jarchivar.io.HashCalculator;
import org.jarchivar.io.SHA1Calculator;
import org.jarchivar.sql.DBManager;

/**
 *
 * @author  Lars Wuckel
 */
public class JArchivar {
  
  public static boolean DEBUG = false;
  
  protected String path = "";
  protected String lowerCase = "lower";
  protected HashCalculator hashCalc;
  private DBManager dbManager;
  
  /** Creates a new instance of Main */
  public JArchivar (String[] args) {
    
    this.hashCalc = new SHA1Calculator ();
    
    try{
      Properties prop = new Properties ();
      File file = new File ("jArchivar.ini");
      
      if (file.exists ()){
	
	prop.load (new FileInputStream (file));
	
	String jdbcDriver = prop.getProperty ("jdbcdriver");
	String jdbcUrl = prop.getProperty ("jdbcurl");
	String user = prop.getProperty ("user");
	String password = prop.getProperty ("password");
	lowerCase = prop.getProperty ("lowercase");
	
	dbManager = new DBManager (jdbcDriver,jdbcUrl,user,password);
	
	String command = "";
	
	if (args.length >0)
	  command = args[0].toLowerCase ();
	
	if (command.equalsIgnoreCase ("add") && args.length == 3 && new File (args[1]).exists ()){
	  
	  path = args[1];
	  
	  add (args[1],args[2]);
	  
	} else if (command.equalsIgnoreCase ("remove") && args.length == 2 ){
	  remove (args[1]);
	} else if (command.equalsIgnoreCase ("list") && args.length >= 1 ){
	  if (args.length > 1)
	    list (args[1].replace ("'","''"));
	  else
	    list (null);
	} else if (command.equalsIgnoreCase ("listarchive") ){
	  listArchive ();
	} else if (command.equalsIgnoreCase ("same") && args.length == 2){
	  same (args[1]);
	} else if (command.equalsIgnoreCase ("find") && args.length == 2 ){
	  find (args[1]);
	} else if (command.equalsIgnoreCase ("finddir") && args.length == 2){
	  findPath (args[1]);
	} else {
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
    } catch(Exception e){
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
    
    try{
      int archiveID = dbManager.getArchiveID (archName);
      
      if (archiveID == -1){
	archiveID = dbManager.addArchiv (archName);
      }
      
      if (DEBUG)
	System.out.println ("add " + path + " Archive: " + archName);
      
      FileManager fileManager = new FileManager (hashCalc);
      ArrayList<FileInfo> fileInfolist = fileManager.readFiles (path);
      
      Hashtable hashTableFileID = dbManager.getHashFileIDHashtable ();
      Hashtable hashTablePathID = dbManager.getPathIDHashtable ();
      Hashtable hashTableFileIDPathID = dbManager.getFileIDPathIDHashtable ();
      
      for(int i = 0; i < fileInfolist.size (); ++i){
	
	FileInfo info = fileInfolist.get (i);
	Object o = hashTableFileID.get (info.getHash ());
	int fileID = -1;
	boolean foundFile = false;
	
	if ( o == null ){
	  
	  fileID = dbManager.addFileInfo (info);
	} else{
	  foundFile = true;
	  fileID = (Integer)o;
	}
	
	o = hashTablePathID.get (info.getPath ());
	int pathID = -1;
	
	if (o == null){
	  
	  pathID = dbManager.addPath (info.getPath ());
	} else{
	  
	  pathID = (Integer)o;
	}
	
	o = hashTableFileIDPathID.get (fileID);
	
	if (o == null || ! ((ArrayList)o).contains (pathID)){
	  dbManager.setFilePath (fileID, pathID);
	}
      }
      
      dbManager.commit ();
    } catch(Exception e){
    }
  }
  
  private void remove (String archName){
    
    try{
      int id = dbManager.getArchiveID (archName);
      dbManager.removeArchive (id);
      dbManager.commit ();
    } catch(Exception e){
    }
  }
  
  private void find (String dateiName){
    String dName = dateiName.replace ("'", "''").replace ("%","\\%").replace ("_","\\_").toLowerCase ();
  }
  
  private void findPath (String pathName){
    String pName = pathName.replace ("'", "''").replace ("%","\\%").replace ("_","\\_").toLowerCase ();
  }
  
  private void listArchive (){
    try{
      
      Hashtable hash = dbManager.getArchiveIDHashtable ();
      
      for(Enumeration enumeration = hash.keys (); enumeration.hasMoreElements ();)
	System.out.println ( enumeration.nextElement () );
    } catch(Exception e){
      e.printStackTrace ();
    }
  }
  
  private void list (String archiveName){
    try{
      
      int id = dbManager.getArchiveID (archiveName);
      
      if (id > -1){
	
	ArrayList<DBFileInfo> list = dbManager.getDBFileInfos (id);
	
	for(DBFileInfo info : list){
	  
	  System.out.println (info.getPath () + info.getName ());
	}
      }
    } catch(Exception e){
      e.printStackTrace ();
    }
  }
  
  private void diff (String path){
    
    try{
    FileManager fileManager = new FileManager (hashCalc);
    ArrayList<FileInfo> fileInfolist = fileManager.readFiles (path);
    
    Hashtable hash = dbManager.getDBFileInfos ();
    
    for(FileInfo info : fileInfolist){
      
      if (!hash.contains (info.getHash ())){
	System.out.println (info.getPath () + info.getName ());
      }
    }
    }
    catch(Exception e){      
    }
  }
  
  private void same (String path){
    
    try{
    FileManager fileManager = new FileManager (hashCalc);
    ArrayList<FileInfo> fileInfolist = fileManager.readFiles (path);
    
    Hashtable hash = dbManager.getDBFileInfos ();
    
    for(FileInfo info : fileInfolist){
      
      if (hash.contains (info.getHash ())){
	System.out.println (info.getPath () + info.getName ());
      }
    }
    }
    catch(Exception e){
      
    }
  }
}
