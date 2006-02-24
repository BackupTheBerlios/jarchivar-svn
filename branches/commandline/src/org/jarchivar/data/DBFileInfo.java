/*
 * DBFileInfo.java
 *
 * Created on 24. Februar 2006, 19:17
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
public class DBFileInfo extends FileInfo{
  
  int id = -1;
  int pathID = -1;
  String archiv ;
  int archivID ;  
  
  public DBFileInfo(java.sql.ResultSet rs) throws Exception{
    
    this.id = rs.getInt ("id");
    this.name = rs.getString ("name");
    this.hash = rs.getString ("hash");
    this.length = rs.getLong ("laenge");
    this.archiv = rs.getString ("archiv");
    this.archivID = rs.getInt ("archiv_id");
    this.path = rs.getString ("path");
    this.pathID = rs.getInt ("path_id");
  }
}
