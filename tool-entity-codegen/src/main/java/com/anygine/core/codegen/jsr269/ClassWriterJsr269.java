package com.anygine.core.codegen.jsr269;

import static com.anygine.core.codegen.Constants.NEW_LINE;

import java.io.IOException;
import java.io.Writer;

import javax.annotation.processing.Filer;
import javax.tools.JavaFileObject;

public class ClassWriterJsr269 {

  static final String INDENTATION_UNIT = "  ";

  private final StringBuilder stringBuilder;
  private final Filer filer;
  private String qualifiedName;

  ClassWriterJsr269() {
    this.stringBuilder = new StringBuilder();
    this.filer = null;
  }
  
  ClassWriterJsr269(StringBuilder stringBuilder) {
    this.stringBuilder = stringBuilder;
    this.filer = null;
  }
  
  public ClassWriterJsr269(Filer filer, StringBuilder stringBuilder) {
    this.filer = filer;
    this.stringBuilder = stringBuilder;
  }

  ClassWriterJsr269 writeLine(int indent, String...values) {
    write(indent, values);
    stringBuilder.append(NEW_LINE);
    return this;
  }
  
  ClassWriterJsr269 write(int indent, String...values) {
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
  
  private ClassWriterJsr269 indent(int indentation) {
    for (int i = 0; i < indentation; i++) {
      stringBuilder.append(INDENTATION_UNIT);
    }
    return this;
  }

  public void writeFile() {
    try {
      JavaFileObject javaFile = filer.createSourceFile(qualifiedName);
      Writer writer = javaFile.openWriter();
      writer.write(stringBuilder.toString());
      writer.flush();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public String toString() {
    return stringBuilder.toString();
  }
}
