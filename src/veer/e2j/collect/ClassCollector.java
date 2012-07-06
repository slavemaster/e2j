package veer.e2j.collect;

import veer.e2j.instrument.Filter;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.instrument.Instrumentation;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import java.util.*;

import java.util.concurrent.*;

import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

public final class ClassCollector {

  private final Filter visitor;
  private final BlockingQueue<ClassDefinition> source;
  private final Path output;

  public ClassCollector(Path output, Constraint... constraints) {
    this(output, Arrays.asList(constraints));
  }

  public ClassCollector(Path output, Collection<Constraint> constraints) {
    this.output = output;
    source = new LinkedBlockingQueue<>();
    visitor = new Filter(constraints, source);
  }

  public void prepare() {
    Runtime.getRuntime().addShutdownHook(new CollectionTerminator());
  }

  public void attach(Instrumentation instr) {
    instr.addTransformer(visitor);
  }

  public void finish() {
    List<ClassDefinition> definitions = new ArrayList<>(source.size());
    source.drainTo(definitions);
    try (
      OutputStream stream = Files.newOutputStream(output, StandardOpenOption.CREATE,
          StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
      JarOutputStream sink = new JarOutputStream(stream)
    ) {
      for (ClassDefinition definition : definitions) {
        emit(sink, definition);
      }
    } catch (IOException ex) {
      ex.printStackTrace(System.err);
    }
  }

  private void emit(JarOutputStream sink, ClassDefinition definition) throws IOException {
    String filename = definition.name().replaceAll("[\\.$]", "/") + ".class";

    sink.putNextEntry(new JarEntry(filename));
    sink.write(definition.data());
    sink.closeEntry();
  }

  private final class CollectionTerminator extends Thread {

    private final ClassCollector collector = ClassCollector.this;

    public void run() {
      collector.finish();
    }
  }
}
