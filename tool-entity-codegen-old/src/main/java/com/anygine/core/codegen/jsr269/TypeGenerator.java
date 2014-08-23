package com.anygine.core.codegen.jsr269;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;

abstract class TypeGenerator {

  private static final int BUF_SIZE = 4096;
  
  abstract public boolean generate(Element element, ProcessingEnvironment env);
  
  protected ClassWriterJsr269 newWriter(ProcessingEnvironment env) {
    return new ClassWriterJsr269(env.getFiler(), new StringBuilder(BUF_SIZE));
  }
}
