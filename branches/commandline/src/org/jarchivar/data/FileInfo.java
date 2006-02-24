/*
 * FileInfo.java
 *
 * Created on 23. Februar 2006, 18:36
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jarchivar.data;

import java.io.File;
import org.jarchivar.io.HashCalculator;

/**
 *
 * @author Lars Wuckel
 */
public class FileInfo {
  
  protected String path ;
  protected String name ;
  protected String hash ;
  protected long length ;  

  protected FileInfo(){
  }
  
  public FileInfo (File file, HashCalculator hashCalc) throws Exception{
    this.name = file.getName ().replace ("'","''");
    this.path = file.getPath ().replace ("'","''");
    this.length = file.length ();
    this.hash = hashCalc.getHash (file);
  }
  
  public String getPath(){
    return this.path;
  }
  
  public String getName(){
    return this.name;
  }
  
  public String getHash(){
    return this.hash;
  }
  
  public long getLength(){
    return this.length;
  }
}
