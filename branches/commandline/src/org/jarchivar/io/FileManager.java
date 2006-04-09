/*
 * FileManager.java
 *
 * Created on 24. Februar 2006, 19:26
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jarchivar.io;

import java.util.ArrayList;
import java.io.File;

/**
 *
 * @author Lars Wuckel
 */
public class FileManager {
   
  public ArrayList<File> readFiles(String path) throws Exception{

    ArrayList<File> fileList = new ArrayList<File>();
    
    this.readFiles (path,fileList);
    
    return fileList;
  }
  
  private void readFiles(String path, ArrayList<File> fileList) throws Exception{
    File file = new File (path);
    
    File[] files = file.listFiles ();
    
    for(int i=0; i < files.length; ++i){
       
      if (files[i].isDirectory ())
        readFiles (files[i].getAbsolutePath (),fileList);        
      else{
    	fileList.add (files[i]);
      }        
    }    
  }
}
