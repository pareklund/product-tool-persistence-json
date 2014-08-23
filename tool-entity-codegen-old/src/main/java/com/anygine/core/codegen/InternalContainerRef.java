package com.anygine.core.codegen;

public class InternalContainerRef {

  private long id;
  private int position;

  public InternalContainerRef(long id, int position) {
    this.id = id;
    this.position = position;
  }

  public long getId() {
    return id;
  }

  public int getPosition() {
    return position;
  }


}
