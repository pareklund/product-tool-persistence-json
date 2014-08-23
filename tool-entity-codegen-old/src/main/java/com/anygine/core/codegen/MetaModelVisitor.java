package com.anygine.core.codegen;

import java.util.Collection;
import java.util.List;

import com.sun.mirror.declaration.MemberDeclaration;
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

public class MetaModelVisitor implements TypeVisitor {

  private final MemberDeclaration fieldDecl;
  private final String fieldName;
  private final ClassWriter writer;
  private int indent;
  
  public MetaModelVisitor(
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
    visitDeclaredType(t);
  }

  @Override
  public void visitDeclaredType(DeclaredType t) {
    TypeDeclaration typeDecl = t.getDeclaration();
    if (typeDecl.getQualifiedName().equals(List.class.getName())) {
      Collection<TypeMirror> typeArgs = t.getActualTypeArguments();
      if (typeArgs.size() != 0) {
        TypeMirror typeArg = typeArgs.iterator().next();
        typeArg.accept(new MetaModelListVisitor(
            fieldDecl, fieldName, writer, indent));
      }
    } else if (
        CodeGenHelper.isEmbeddable(typeDecl) 
        || CodeGenHelper.isStorable(typeDecl)) {
      String metaModelClassName = CodeGenHelper.getClassName(
          typeDecl, CodeGenHelper.GenType.META_MODEL, 
          CodeGenHelper.ClassNameType.QUALIFIED);
      writer.writeLine(
          indent, "public final ", metaModelClassName, 
          " ", fieldName, " = new ", metaModelClassName,  ".Proxy();");
    } else if (CodeGenHelper.isString(typeDecl)) {
      writer.writeLine(
          indent, "public final ", String.class.getName(), " ",
          fieldName, " = new String();");
    } else if (typeDecl.getQualifiedName().equals(Long.class.getName())){
      writer.writeLine(
          indent, "public final ", Long.class.getName(), " ", fieldName, 
          " = new Long(0);");
    } else {
      writer.writeLine(
          indent, "// Not yet implemented: ", fieldDecl.getSimpleName(), 
          " (", t.toString(), ")");
    }
  }

  @Override
  public void visitEnumType(EnumType t) {
    TypeDeclaration typeDecl = t.getDeclaration();
    writer.writeLine(
        indent, "public final ", typeDecl.getQualifiedName(), 
        " ", fieldName, " = ", typeDecl.getQualifiedName(), ".values()[0];");
  }

  @Override
  public void visitInterfaceType(InterfaceType t) {
    visitDeclaredType(t);
  }

  @Override
  public void visitPrimitiveType(PrimitiveType t) {
    switch (t.getKind()) {
      case LONG:
        writer.writeLine(
            indent, "public final ", Long.class.getName(), " ",
            fieldName, " = new Long(0);");
        break;
      case INT:
        writer.writeLine(
            indent, "public final ", Integer.class.getName(), " ",
            fieldName, " = new Integer(0);");
        break;
      case BOOLEAN:
        writer.writeLine(
            indent, "public final ", Boolean.class.getName(), " ",
            fieldName, " = new Boolean(false);");
        break;
      case FLOAT:
        writer.writeLine(
            indent, "public final ", Float.class.getName(), " ",
            fieldName, " = new Float(0.0f);");
        break;
      case DOUBLE:
        writer.writeLine(
            indent, "public final ", Double.class.getName(), " ",
            fieldName, " = new Double(0.0d);");
        break;
      case SHORT:
        writer.writeLine(
            indent, "public final ", Short.class.getName(), " ",
            fieldName, " = new Short(0);");
        break;
      case CHAR:
        writer.writeLine(
            indent, "public final ", Character.class.getName(), " ",
            fieldName, " = new Character('');");
        break;
      case BYTE:
        writer.writeLine(
            indent, "public final ", Byte.class.getName(), " ",
            fieldName, " = new Byte((byte) 0);");
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
