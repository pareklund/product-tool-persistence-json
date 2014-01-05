package com.anygine.core.codegen;

import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MemberDeclaration;
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

public class MetaModelListVisitor implements TypeVisitor {

  private final MemberDeclaration fieldDecl;
  private final String fieldName;
  private final ClassWriter writer;
  private int indent;
  
  public MetaModelListVisitor(
      MemberDeclaration fieldDecl, String fieldName, ClassWriter writer, 
      int indent) {
    this.fieldDecl = fieldDecl;
    this.fieldName = fieldName;
    this.writer = writer;
    this.indent = indent;
  }

  @Override
  public void visitAnnotationType(AnnotationType t) {
    writer.writeLine(
        indent, "// Not yet implemented: ", fieldDecl.getSimpleName(), 
        " (", t.toString(), ")");
  }

  @Override
  public void visitArrayType(ArrayType t) {
    writer.writeLine(
        indent, "// Not yet implemented: ", fieldDecl.getSimpleName(), 
        " (", t.toString(), ")");
  }

  @Override
  public void visitClassType(ClassType t) {
    writer.writeLine(
        indent, "// Not yet implemented: ", fieldDecl.getSimpleName(), 
        " (", t.toString(), ")");
  }

  @Override
  public void visitDeclaredType(DeclaredType t) {
    writer.writeLine(
        indent, "// Not yet implemented: ", fieldDecl.getSimpleName(), 
        " (", t.toString(), ")");
  }

  @Override
  public void visitEnumType(EnumType t) {
    writer.writeLine(
        indent, "// Not yet implemented: ", fieldDecl.getSimpleName(), 
        " (", t.toString(), ")");
  }

  @Override
  public void visitInterfaceType(InterfaceType t) {
    writer.writeLine(
        indent, "// Not yet implemented: ", fieldDecl.getSimpleName(), 
        " (", t.toString(), ")");
  }

  @Override
  public void visitPrimitiveType(PrimitiveType t) {
    writer.writeLine(
        indent, "// Not yet implemented: ", fieldDecl.getSimpleName(), 
        " (", t.toString(), ")");
  }

  @Override
  public void visitReferenceType(ReferenceType t) {
    writer.writeLine(
        indent, "// Not yet implemented: ", fieldDecl.getSimpleName(), 
        " (", t.toString(), ")");
  }

  @Override
  public void visitTypeMirror(TypeMirror t) {
    writer.writeLine(
        indent, "// Not yet implemented: ", fieldDecl.getSimpleName(), 
        " (", t.toString(), ")");
  }

  @Override
  public void visitTypeVariable(TypeVariable t) {
    writer.writeLine(
        indent, "// Not yet implemented: ", fieldDecl.getSimpleName(), 
        " (", t.toString(), ")");
  }

  @Override
  public void visitVoidType(VoidType t) {
    writer.writeLine(
        indent, "// Not yet implemented: ", fieldDecl.getSimpleName(), 
        " (", t.toString(), ")");
  }

  @Override
  public void visitWildcardType(WildcardType t) {
    writer.writeLine(
        indent, "// Not yet implemented: ", fieldDecl.getSimpleName(), 
        " (", t.toString(), ")");
  }

}
