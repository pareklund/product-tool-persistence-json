package com.anygine.core.codegen;

import static com.anygine.core.codegen.Constants.NEW_LINE;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.Modifier;
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

public class AddCopyConstructorVisitor implements TypeVisitor {

  private final ClassDeclaration proxiedClass;
  private final StringBuilder classDefinition;
  private final String fieldName;
  private final String attribute;
  private final int indentation;
  private final boolean lastParam;
  
  public AddCopyConstructorVisitor(
      ClassDeclaration proxiedClass, StringBuilder classDefinition, 
      String fieldName, String attribute, int indentation, 
      boolean lastParam) {
    
    this.proxiedClass = proxiedClass;
    this.classDefinition = classDefinition;
    this.fieldName = fieldName;
    this.attribute = attribute;
    this.indentation = indentation;
    this.lastParam = lastParam;
  }
  
  @Override
  public void visitWildcardType(WildcardType arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }
  
  @Override
  public void visitVoidType(VoidType arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }
  
  @Override
  public void visitTypeVariable(TypeVariable arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }
  
  @Override
  public void visitTypeMirror(TypeMirror arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }
  
  @Override
  public void visitReferenceType(ReferenceType arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }
  
  @Override
  public void visitPrimitiveType(PrimitiveType arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }
  
  @Override
  public void visitInterfaceType(InterfaceType arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }
  
  @Override
  public void visitEnumType(EnumType arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }
  
  @Override
  public void visitDeclaredType(DeclaredType arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }
  
  @Override
  public void visitClassType(ClassType t) {
    ClassDeclaration classDecl = t.getDeclaration();
    FieldDeclaration fieldDecl = CodeGenHelper.getField(classDecl.getFields(), attribute);
    if (fieldDecl == null 
        || !fieldDecl.getModifiers().contains(Modifier.PUBLIC)) {
      MethodDeclaration methodDecl = CodeGenHelper.getMethod(
          classDecl.getMethods(), CodeGenHelper.getGetterName(attribute));
      if (methodDecl == null) {
        throw new RuntimeException(
            "No public attribute or getter for field "
            + attribute + " in " + proxiedClass);
      } else {
        CodeGenHelper.indent(indentation, classDefinition);    
        classDefinition.append("other.").append(fieldName).append(".")
            .append(methodDecl.getSimpleName()).append("()").append(
                lastParam ? "" : ",").append(NEW_LINE);
      }
    } else {
      CodeGenHelper.indent(indentation, classDefinition).append(
          "other.").append(fieldName).append(".").append(
              fieldDecl.getSimpleName()).append(
                  lastParam ? "" : ",").append(NEW_LINE);
    }
  }
  
  @Override
  public void visitArrayType(ArrayType arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }
  
  @Override
  public void visitAnnotationType(AnnotationType arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }

}
