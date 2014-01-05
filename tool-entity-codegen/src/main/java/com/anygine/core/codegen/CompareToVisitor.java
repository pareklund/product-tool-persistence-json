package com.anygine.core.codegen;

import com.anygine.core.codegen.CodeGenHelper.ClassNameType;
import com.anygine.core.codegen.CodeGenHelper.GenType;
import com.sun.mirror.declaration.ClassDeclaration;
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

public class CompareToVisitor implements TypeVisitor {

  private final ClassWriter writer;
  private int indent;
  private final TypeDeclaration typeDecl;
  private final FieldDeclaration fieldDecl;

  public CompareToVisitor(
      TypeDeclaration typeDecl, FieldDeclaration fieldDecl, 
      ClassWriter writer, int indent) {
    this.typeDecl = typeDecl;
    this.fieldDecl = fieldDecl;
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
    if (CodeGenHelper.isComparable(t)) {
      writer.writeLine(
          indent++, "if (", t.toString(), ".class.cast(attribute) == ",
          CodeGenHelper.getClassName(
              typeDecl, GenType.META_MODEL, ClassNameType.QUALIFIED),
              ".META_MODEL.", fieldDecl.getSimpleName(), ") {");
      writer.writeLine(
          indent--, "return ", fieldDecl.getSimpleName(), ".compareTo(",
          t.toString(), ".class.cast(value));");
      writer.writeLine(indent--, "}");
    } else {
      writer.writeLine(
          indent, "// Not yet implemented: ", fieldDecl.getSimpleName(), 
          " (", t.toString(), ")");
    }
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
    switch (t.getKind()) {
    case LONG:
      writer.writeLine(
          indent, "if (attribute.getClass().isAssignableFrom(", 
          Long.class.getName(), ".class)");
      indent++;
      writer.writeLine(
          ++indent, "&& ", Long.class.getName(), 
          ".class.cast(attribute) == ", 
          fieldDecl.getDeclaringType().getQualifiedName(), 
          "_MetaModel.META_MODEL.", fieldDecl.getSimpleName(), ") {");
      writer.writeLine(
          --indent, "return (int) (", fieldDecl.getSimpleName(), " - (",
          Long.class.getName(), ".class.cast(value)));");
      writer.writeLine(--indent, "}");
    }
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
