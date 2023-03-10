/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */

package org.uniapr.jvm.agent;
import org.objectweb.asm.ClassWriter;

/**
 *  Class for compute ASM frame options
 *  @author Henry Coles
 */
public class FrameOptions {

  private static final int JAVA_7 = 51;

  /**
   * Java 7 and above require frame info for class version above 7. The ASM compute
   * frame options does not support the JSR opcode used by some pre 7 compilers
   * when generating java 5 bytecode.
   * 
   * We dodge this issue by only computing frames for classes with a version
   * above 6.
   * 
   * @param bs a class
   * @return appropriate flags
   */
  public static int pickFlags(byte[] bs) {
    if ( needsFrames(bs) ) {
      return ClassWriter.COMPUTE_FRAMES;
    }
    return ClassWriter.COMPUTE_MAXS;
  }
  
  public static boolean needsFrames(byte[] bs) {
    short majorVersion =(short)( ((bs[6]&0xFF)<<8) | (bs[7]&0xFF) );
    return majorVersion >= JAVA_7;
  }
  
}
