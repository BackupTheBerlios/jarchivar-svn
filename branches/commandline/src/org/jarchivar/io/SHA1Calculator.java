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
public class SHA1Calculator extends HashCalculator{
  
  public String getHash(File file)  throws Exception {
    return getHash ("SHA1",file);
  }
}
