package veer.e2j.collect;

public interface Constraint {

  public boolean accept(ClassLoader loader, String name);
}