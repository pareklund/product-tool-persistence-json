package com.anygine.core.codegen;

/**
 * A mutable index object to support easy positioning when reading from objects
 * derived from the IStorage interface.
 * 
 * @author Pär Eklund
 *
 */
public class Position {

  private int position;

  public Position(int index) {
    if (index < 0) {
      throw new RuntimeException("Negative position value not allowed: " + index);
    }
    this.position = index;
  }

  /**
   * Get the position
   * 
   * @return          the current position
   */
  public int get() {
    return position;
  }

  /**
   * Increment the position with the specified value and get the resulting
   * position.
   * 
   * @param increment          the amount to increment the position with
   * @return                   the resulting position 
   */
  public int getAndIncrement(int increment) {
    int value = position;
    position += increment;
    if (position < 0) {
      throw new RuntimeException("Negative position value not allowed: " + position);
    }
    return value;
  }

}
