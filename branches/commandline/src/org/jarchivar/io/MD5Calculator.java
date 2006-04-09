/*
 * MD5File.java
 *
 * Created on 1. Mai 2004, 14:38
 */

package org.jarchivar.io;

import java.io.*;

/**
 *
 * @author  Lars Wuckel
 */
public class MD5Calculator extends HashCalculator{
  
  public String getHash (File file) throws Exception {
    return getHash ("MD5",file);
  }
  
}