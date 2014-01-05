package com.anygine.core.codegen;

import static com.anygine.core.codegen.Constants.NEW_LINE;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.Filer;

public class ClassWriter {

  static final String INDENTATION_UNIT = "  ";

  private final StringBuilder stringBuilder;
  private final Filer filer;
  private String qualifiedName;

  ClassWriter() {
    this.stringBuilder = new StringBuilder();
    this.filer = null;
  }
  
  ClassWriter(StringBuilder stringBuilder) {
    this.stringBuilder = stringBuilder;
    this.filer = null;
  }
  
  public ClassWriter(Filer filer, StringBuilder stringBuilder) {
    this.filer = filer;
    this.stringBuilder = stringBuilder;
  }

  ClassWriter writeLine(int indent, String...values) {
    write(indent, values);
    stringBuilder.append(NEW_LINE);
    return this;
  }
  
  ClassWriter write(int indent, String...values) {
    indent(indent);
    for (String value : values) {
      stringBuilder.append(value);
    }
    return this;
  }
  
  StringBuilder getStringBuilder() {
    return stringBuilder;
  }

  String getQualifiedName() {
    return qualifiedName;
  }
  
  void setQualifiedName(String qualifiedName) {
    this.qualifiedName = qualifiedName;
  }
  
  private ClassWriter indent(int indentation) {
    for (int i = 0; i < indentation; i++) {
      stringBuilder.append(INDENTATION_UNIT);
    }
    return this;
  }

  public void writeFile() {
    try {
      PrintWriter fileWriter = filer.createSourceFile(qualifiedName);
      fileWriter.write(stringBuilder.toString());
      fileWriter.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public String toString() {
    return stringBuilder.toString();
  }
}
