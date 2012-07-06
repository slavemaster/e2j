package veer.e2j.instrument;

import veer.e2j.collect.ClassCollector;
import veer.e2j.collect.Constraint;

import java.lang.instrument.Instrumentation;

import java.io.IOException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Entrypoint {

  public static void premain(String options, Instrumentation instr) {
    Path output;
    try {
        output = (options == null || options.isEmpty())
            ? Files.createTempFile(pwd(), "e2j-", ".dump.jar") : Paths.get(options);
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }

    ClassCollector collector = new ClassCollector(output, new Constraint() {

      private ClassLoader target;

      public boolean accept(ClassLoader loader, String name) {
        if (target == null) {
          String loader_name = loader.getClass().getName();
          if (loader_name.equals("com.regexlab.j2e.Jar2ExeClassLoader")) {
            target = loader;
          }
        }
        return loader == target;
      }
    });
    collector.prepare();
    collector.attach(instr);
  }

  private static Path pwd() {
    return Paths.get(".");
  }
}
