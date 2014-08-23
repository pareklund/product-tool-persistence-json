package com.anygine.core.codegen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;

import com.anygine.core.common.client.annotation.Embeddable;
import com.anygine.core.common.client.annotation.Storable;
import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

public class AnygineProcessorFactory implements AnnotationProcessorFactory {

  public AnnotationProcessor getProcessorFor(
      Set<AnnotationTypeDeclaration> types,
      AnnotationProcessorEnvironment env) {
    return new AnygineProcessor(types, env);
  }

  public Collection<String> supportedAnnotationTypes() {
    return Arrays.asList(Storable.class.getName(), Embeddable.class.getName());
  }

  public Collection<String> supportedOptions() {
    return new ArrayList<String>(0);
  }

}
