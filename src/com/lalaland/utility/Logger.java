package com.lalaland.utility;

public class Logger {
  
  private static final boolean LOG_ENABLE = false;
  
  public static void log(String text) {
    if (LOG_ENABLE)
      System.out.println(text);
  }
  public static void logSameLine(String text) {
    if (LOG_ENABLE)
      System.out.print(text);
  }
  
}
