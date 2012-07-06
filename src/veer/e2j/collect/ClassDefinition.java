package veer.e2j.collect;

public final class ClassDefinition {

  private final String name;
  private final byte[] data;

  public ClassDefinition(String name, byte[] data) {
    this.name = name;
    this.data = data;
  }

  public final String name() {
    return name;
  }

  public final byte[] data() {
    return data;
  }
}
