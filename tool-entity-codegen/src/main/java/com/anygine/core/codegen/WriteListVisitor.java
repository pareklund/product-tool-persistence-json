package com.anygine.core.codegen;

import com.anygine.core.common.client.domain.impl.JsonWritableHelper;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.AnnotationType;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.EnumType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.PrimitiveType;
import com.sun.mirror.type.ReferenceType;
import com.sun.mirror.type.TypeMirror;
import com.sun.mirror.type.TypeVariable;
import com.sun.mirror.type.VoidType;
import com.sun.mirror.type.WildcardType;
import com.sun.mirror.util.TypeVisitor;

public class WriteListVisitor implements TypeVisitor {

  private final FieldDeclaration fieldDecl;
  private final String fieldName;
  private final ClassWriter writer;
  private final String methodName;
  private int indent;
  
  public WriteListVisitor(
      FieldDeclaration fieldDecl, ClassWriter writer, int indent, String methodName) {
    this.fieldDecl = fieldDecl;
    this.fieldName = CodeGenHelper.getFieldName(fieldDecl);
    this.writer = writer;
    this.methodName = methodName;
    this.indent = indent;
  }

  @Override
  public void visitAnnotationType(AnnotationType arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visitArrayType(ArrayType arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visitClassType(ClassType t) {
    visitDeclaredType(t);
  }

  @Override
  public void visitDeclaredType(DeclaredType t) {
    TypeDeclaration classDecl = t.getDeclaration();
    if (CodeGenHelper.isStorable(classDecl) && methodName.equals("_write")) {
      writer.writeLine(
          indent, CodeGenHelper.getClassName(JsonWritableHelper.class), 
          ".writeEntityList(", fieldDecl.getSimpleName(), 
          ", entityWriter, \"", fieldName, "\");");
    } else if (CodeGenHelper.isEmbeddable(classDecl) || methodName.equals("_writeJson")) {
      writer.writeLine(
          indent, CodeGenHelper.getClassName(JsonWritableHelper.class), 
          ".writeList(", fieldDecl.getSimpleName(), ", writer, \"",
          fieldName, "\");");
    } else {
      writer.writeLine(
          indent, "// Not adding write list call for: ", t.toString());
    }
    // TODO: Implement writing lists of other classes
  }

  @Override
  public void visitEnumType(EnumType arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visitInterfaceType(InterfaceType t) {
    visitDeclaredType(t);
  }

  @Override
  public void visitPrimitiveType(PrimitiveType arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visitReferenceType(ReferenceType arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visitTypeMirror(TypeMirror arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visitTypeVariable(TypeVariable arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visitVoidType(VoidType arg0) {
    // TODO Auto-generated method stub

  }

  @Override
  public void visitWildcardType(WildcardType arg0) {
    // TODO Auto-generated method stub

  }

}
