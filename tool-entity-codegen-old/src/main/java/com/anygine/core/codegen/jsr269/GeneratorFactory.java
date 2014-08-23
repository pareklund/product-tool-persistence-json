package com.anygine.core.codegen.jsr269;

import java.lang.annotation.Annotation;

import javax.lang.model.element.Element;

import com.anygine.core.common.client.annotation.Embeddable;
import com.anygine.core.common.client.annotation.Storable;

class GeneratorFactory {

  private static final GeneratorFactory INSTANCE = new GeneratorFactory();
  
  public static GeneratorFactory instance() {
    return INSTANCE;
  }
  
  public TypeGenerator getGenerator(
      Class<? extends Annotation> annotation, Element element) {
    if (annotation.equals(Embeddable.class)) {
      if (element.getKind().isClass()) {
        return new EmbeddableClassGenerator();
      } else if (element.getKind().isInterface()) {
        return new EmbeddableInterfaceGenerator();
      }
    } else if (annotation.equals(Storable.class)) {
      if (element.getKind().isClass()) {
        return new StorableClassGenerator();
      } else if (element.getKind().isInterface()) {
        return new StorableInterfaceGenerator();
      }
    }
    return new NoClaimGenerator();
  }
}
