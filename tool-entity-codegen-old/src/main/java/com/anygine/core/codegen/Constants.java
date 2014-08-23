package com.anygine.core.codegen;


/**
 * Contains constant definitions related to checkpointing functionality
 * 
 * @author Pär Eklund
 *
 */
public class Constants {

  public static final int LONG_SIZE = 8;
  public static final int INT_SIZE = 4;
  public static final int SHORT_SIZE = 2;
  public static final int BYTE_SIZE = 1;
  public static final int CHAR_SIZE = 2;
  public static final int BOOLEAN_SIZE = 1;
  public static final int FLOAT_SIZE = 4;
  public static final int DOUBLE_SIZE = 8;
  public static final int DATETIME_SIZE = LONG_SIZE;
  public static final int TIMESPAN_SIZE = LONG_SIZE;
  public static final int DECIMALF_SIZE = LONG_SIZE;
  public static final int TXID_SIZE = 12;
  public static final int CSN_SIZE = INT_SIZE + LONG_SIZE;
  public static final int PREDICATE_SIZE = LONG_SIZE;
  public static final int ENUM_SIZE = INT_SIZE;

  public static final String NEW_LINE = System.getProperty("line.separator");

  public static final byte OPTIONAL_EMPTY_MARKER_SIZE = BYTE_SIZE;
  public static final byte OPTIONAL_EMPTY_MARKER = 0;
  public static final byte OPTIONAL_NOT_EMPTY_MARKER = 1;

  public static final InternalContainerRef[] EMPTY_CONTAINER_REF_ARRAY = new InternalContainerRef[0];
  public static final String CLASS_SUFFIX = "_Storable";


}
