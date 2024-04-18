package org.magcruise.broker.util;

import java.io.File;

public class ProcessTest {
  public static void main(String[] args) throws Exception {
    ProcessBuilder pb =
        new ProcessBuilder(
            "ls"
            /*				"/Library/Java/JavaVirtualMachines/jdk1.7.0_25.jdk/Contents/Home/bin/jrunscript",
            				"-l",
            				"js",
            				"-e",
            				"\"for(i = 0; i < 10; i++){println('hello'); java.lang.Thread.sleep(1000);}\""
            */ );
    pb.redirectOutput(new File("out"));
    pb.redirectError(new File("err"));
    //		pb.inheritIO();
    Process p = pb.start();
    System.out.println(p.waitFor());
  }
}
