package org.magcruise.gaming.examples.ultimatum.actor;

public enum Response {

  YES, NO;

  @Override
  public String toString() {
    return name().toLowerCase();
  }

}
