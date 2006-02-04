/*
 * SHA1Calculator.java
 *
 * Created on 1. Mai 2004, 20:40
 */

package org.jarchivar.io;

import java.io.File;

/**
 *
 * @author  Lars Wuckel
 */
public class SHA1Calculator extends FileCalculator{
  
  /** Creates a new instance of SHA1Calculator */
  public SHA1Calculator (File file) {
    super(file);
  }
  
  public String getSHA1() throws Exception {
    return getHash ("SHA1");
  }
}
