package com.anygine.core.codegen;

import com.anygine.core.codegen.CodeGenHelper.ClassNameType;
import com.anygine.core.codegen.CodeGenHelper.GenType;
import com.anygine.core.common.client.annotation.Storable;
import com.anygine.core.common.codegen.api.EntityInternal;
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

public class EntityEqualsVisitor implements TypeVisitor {

  private final ClassWriter writer;
  private int indent;
  private final ClassDeclaration classDecl;
  private final FieldDeclaration fieldDecl;

  public EntityEqualsVisitor(
      ClassDeclaration classDecl, FieldDeclaration fieldDecl, 
      ClassWriter writer, int indent) {
    this.classDecl = classDecl;
    this.fieldDecl = fieldDecl;
    this.writer = writer;
    this.indent = indent;
  }

  @Override
  public void visitAnnotationType(AnnotationType t) {
    // Nothing to be done
  }

  @Override
  public void visitArrayType(ArrayType t) {
    // TODO: Implement
    writer.writeLine(
        indent, "// Not yet implemented: ", fieldDecl.getSimpleName(), 
        " (", t.toString(), ")");
  }

  @Override
  public void visitClassType(ClassType t) {
    visitDeclaredType(t);
  }

  @Override
  public void visitDeclaredType(DeclaredType type) {
    // TODO: Implement collections and hash maps of entities
    if (type.getDeclaration().getAnnotation(Storable.class) != null) {
      String fieldName = CodeGenHelper.getFieldName(fieldDecl);
      if (!CodeGenHelper.isFieldAccessible(fieldDecl, classDecl)) {
        fieldName = CodeGenHelper.getGetter(
            classDecl, fieldDecl).getSimpleName() + "()";
      }
      writer.writeLine(
          indent++, "if (metaModel == ", 
          CodeGenHelper.getClassName(classDecl, GenType.META_MODEL, ClassNameType.QUALIFIED),
          ".META_MODEL.", CodeGenHelper.getFieldName(fieldDecl), ") {");
      writer.writeLine(
          indent--, "return (((", EntityInternal.class.getName(), "<", 
          CodeGenHelper.getFieldType(fieldDecl, ClassNameType.QUALIFIED),
          ">) ", fieldName, ").getId() == ((",
          EntityInternal.class.getName(), "<", 
          CodeGenHelper.getFieldType(fieldDecl, ClassNameType.QUALIFIED),
          ">) otherEntity).getId());");
      writer.writeLine(indent, "}");
    }
  }

  @Override
  public void visitEnumType(EnumType t) {
    // Nothing to be done
  }

  @Override
  public void visitInterfaceType(InterfaceType t) {
    visitDeclaredType(t);
  }

  @Override
  public void visitPrimitiveType(PrimitiveType t) {
    // Nothing to be done
  }

  @Override
  public void visitReferenceType(ReferenceType t) {
    // Nothing to be done
  }

  @Override
  public void visitTypeMirror(TypeMirror t) {
    // Nothing to be done
  }

  @Override
  public void visitTypeVariable(TypeVariable t) {
    // Nothing to be done
  }

  @Override
  public void visitVoidType(VoidType t) {
    // Nothing to be done
  }

  @Override
  public void visitWildcardType(WildcardType t) {
    // Nothing to be done
  }

}
