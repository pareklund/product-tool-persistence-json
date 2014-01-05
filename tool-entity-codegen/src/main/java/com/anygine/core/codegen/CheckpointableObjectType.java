package com.anygine.core.codegen;

public enum CheckpointableObjectType {

  REFDATA_ENTITY,
  POJO,
  CONTAINER;

  public static CheckpointableObjectType getObjectType(int ordinal) {
    CheckpointableObjectType objectType = null;
    switch (ordinal) {
      case 0:
        objectType = REFDATA_ENTITY;
        break;
      case 1:
        objectType = POJO;
        break;
      case 2:
        objectType = CONTAINER;
        break;
    }
    return objectType;
  }
}
