/*
 * Debug.java
 *
 * Created on 9. März 2006, 20:18
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.jarchivar.diagnostic;

import java.util.GregorianCalendar;

/**
 *
 * @author Lars Wuckel
 */
public class Debug {
  
  public static boolean DEBUG = true;
  static StringBuilder stringBuilder = new StringBuilder();
  
  /** Creates a new instance of Debug */
  public Debug () {
  }
  
  public static void println(Object out){
    if (DEBUG){

        stringBuilder.setLength(0);
        stringBuilder.append(new GregorianCalendar().getTime());
        stringBuilder.append(" ");
        stringBuilder.append(out);
        
        System.out.println (  stringBuilder.toString());
    }
  }
  
  public static void printHeap(){
    // Get current size of heap in bytes
    long heapSize = Runtime.getRuntime().totalMemory();
    
    // Get maximum size of heap in bytes. The heap cannot grow beyond this size.
    // Any attempt will result in an OutOfMemoryException.
    long heapMaxSize = Runtime.getRuntime().maxMemory();
    
    // Get amount of free memory within the heap in bytes. This size will increase
    // after garbage collection and decrease as new objects are created.
    long heapFreeSize = Runtime.getRuntime().freeMemory();

    Debug.println ("Heap size:" + heapSize);
    Debug.println ("HeapMaxSize:" + heapMaxSize);
    Debug.println ("HeapFreeSize:" + heapFreeSize);
  }  
}
