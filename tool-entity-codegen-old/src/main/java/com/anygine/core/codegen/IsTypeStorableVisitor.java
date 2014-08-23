package com.anygine.core.codegen;

import com.anygine.core.common.client.annotation.Storable;
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

public class IsTypeStorableVisitor implements TypeVisitor {

  private boolean storable;
  
  public boolean isStorable() {
    return storable;
  }
  
  @Override
  public void visitAnnotationType(AnnotationType type) {
  }

  @Override
  public void visitArrayType(ArrayType type) {
  }

  @Override
  public void visitClassType(ClassType type) {
    storable = (type.getDeclaration().getAnnotation(Storable.class) != null);
  }

  @Override
  public void visitDeclaredType(DeclaredType type) {
    storable = (type.getDeclaration().getAnnotation(Storable.class) != null);
  }

  @Override
  public void visitEnumType(EnumType type) {
    storable = (type.getDeclaration().getAnnotation(Storable.class) != null);
  }

  @Override
  public void visitInterfaceType(InterfaceType type) {
    storable = (type.getDeclaration().getAnnotation(Storable.class) != null);
  }

  @Override
  public void visitPrimitiveType(PrimitiveType type) {
  }

  @Override
  public void visitReferenceType(ReferenceType type) {
  }

  @Override
  public void visitTypeMirror(TypeMirror type) {
  }

  @Override
  public void visitTypeVariable(TypeVariable type) {
  }

  @Override
  public void visitVoidType(VoidType type) {
  }

  @Override
  public void visitWildcardType(WildcardType type) {
  }
}
