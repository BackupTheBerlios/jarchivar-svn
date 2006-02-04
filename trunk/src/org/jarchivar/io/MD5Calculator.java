/*
 * MD5File.java
 *
 * Created on 1. Mai 2004, 14:38
 */

package org.jarchivar.io;

import java.io.*;
import java.net.URI;
import java.security.MessageDigest;

/**
 *
 * @author  Lars Wuckel
 */
public class MD5Calculator extends FileCalculator{
  
  protected File file;
  
  public MD5Calculator (File file){
    super(file);
  }
  
  public String getMD5 () throws Exception {
    return getHash ("MD5");
  }
  
}