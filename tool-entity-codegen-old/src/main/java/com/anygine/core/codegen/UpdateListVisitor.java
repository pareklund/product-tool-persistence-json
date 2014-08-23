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

public class UpdateListVisitor implements TypeVisitor {

  private final FieldDeclaration fieldDecl;
  private final String fieldName;
  private final String fieldExpression;
  private final ClassWriter writer;
  private final int indent;
  
  public UpdateListVisitor(
      FieldDeclaration fieldDecl, String fieldExpression, ClassWriter writer, 
      int indent) {
    this.fieldDecl = fieldDecl;
    this.fieldName = CodeGenHelper.getFieldName(fieldDecl);
    this.fieldExpression = fieldExpression;
    this.writer = writer;
    this.indent = indent;
  }

  @Override
  public void visitAnnotationType(AnnotationType arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void visitArrayType(ArrayType arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void visitClassType(ClassType t) {
    visitDeclaredType(t);
  }

  @Override
  public void visitDeclaredType(DeclaredType t) {
    TypeDeclaration classDecl = t.getDeclaration();
    if (CodeGenHelper.isEmbeddable(classDecl)) {
      writer.writeLine(
          indent, CodeGenHelper.getClassName(JsonWritableHelper.class),
          ".updateList(", classDecl.getQualifiedName(), 
          "_Embeddable.class, ", fieldExpression, ", jsonObj, \"", fieldName, "\");");
    } else {
      writer.writeLine(
          indent, "// Not adding write calls for: ", t.toString());
    }  
  }

  @Override
  public void visitEnumType(EnumType arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void visitInterfaceType(InterfaceType t) {
    visitDeclaredType(t);
  }

  @Override
  public void visitPrimitiveType(PrimitiveType arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void visitReferenceType(ReferenceType arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void visitTypeMirror(TypeMirror arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void visitTypeVariable(TypeVariable arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void visitVoidType(VoidType arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void visitWildcardType(WildcardType arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }

}
