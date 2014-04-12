package com.anygine.core.codegen.jsr269;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.TypeMirror;

public class EmbeddableClassGenerator extends TypeGenerator {

  @Override
  public boolean generate(Element element, ProcessingEnvironment env) {
    if (element.getModifiers().contains(Modifier.FINAL)
        || element.getModifiers().contains(Modifier.ABSTRACT)) {
      return false;
    }
    ClassWriterJsr269 classWriter = newWriter(env);
    TypeMirror type = element.asType();
    // And probably use TypeVisitors to do futher work
    throw new UnsupportedOperationException("Not yet implemented");
  }
}
