package com.anygine.core.codegen.jsr269;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import com.anygine.core.common.client.annotation.Embeddable;

@SupportedAnnotationTypes(value = {"*"})
@SupportedSourceVersion(SourceVersion.RELEASE_7)
public class EntityAnnotationProcessor extends AbstractProcessor {

  private final GeneratorFactory factory;
  
  public EntityAnnotationProcessor() {
    factory = GeneratorFactory.instance();
  }
  
  @Override
  public boolean process(
      Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

    processingEnv.getMessager().printMessage(Diagnostic.Kind.NOTE, "In EntityAnnotationProcessor");
    
    for (Element element : roundEnv.getElementsAnnotatedWith(Embeddable.class)) {
      if (!factory.getGenerator(Embeddable.class, element).generate(element, processingEnv)) {
        return false;
      }
    }
    return true;
  }
}
