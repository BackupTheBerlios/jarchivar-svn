/*
 * Calculator.java
 *
 * Created on 1. Mai 2004, 20:36
 */

package org.jarchivar.io;

import java.io.*;
import java.net.URI;
import java.security.MessageDigest;

/**
 *
 * @author  Lars Wuckel
 */
public class FileCalculator {
  
  protected File file;
  
  /** Creates a new instance of Calculator */
  public FileCalculator (File file) {
    this.file = file;
  }
  
  protected String getHash (String mdName) throws Exception {
    MessageDigest md = MessageDigest.getInstance (mdName);
    byte[] buffer = new byte[4096];
    
    if ( file.isFile () ){
      FileInputStream fis = new FileInputStream (file);
      
      int count = fis.read (buffer);
      md.update (buffer);
      
      while (count == buffer.length){
        count = fis.read (buffer);
        md.update (buffer);
      }
      
      fis.close ();
      
      md.update (file.getName ().getBytes ());
      md.update (Long.toBinaryString (file.length ()).getBytes ());
    }
    
    return toHexString (md.digest ());
  }
  
  private String toHexString ( byte[] b ) {
    
    StringBuffer sb = new StringBuffer ( b.length * 2);
    
    for ( int i=0; i<b.length; i++ ) {
      
      // look up high nibble char
      sb.append ( hexChar[ ( b[i] & 0xf0 ) >>> 4 ] );
      
      // look up low nibble char
      sb.append ( hexChar [ b[i] & 0x0f ] );
    }
    
    return sb.toString ();
  }
  
  
  // table to convert a nibble to a hex char.
  static char[] hexChar = {
    '0', '1', '2', '3',
    '4', '5', '6', '7',
    '8', '9', 'a', 'b',
    'c', 'd', 'e', 'f'};
    
}
