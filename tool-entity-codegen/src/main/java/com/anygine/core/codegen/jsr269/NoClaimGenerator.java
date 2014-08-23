package com.anygine.core.codegen.jsr269;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;

public class NoClaimGenerator extends TypeGenerator {

  @Override
  public boolean generate(Element element, ProcessingEnvironment env) {
    return false;
  }
}
