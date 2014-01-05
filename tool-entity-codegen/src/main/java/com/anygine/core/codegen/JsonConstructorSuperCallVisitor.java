package com.anygine.core.codegen;

import static com.anygine.core.codegen.CodeGenHelper.getClassName;

import java.util.Collection;

import com.anygine.core.codegen.CodeGenHelper.GenType;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.Modifier;
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


public class JsonConstructorSuperCallVisitor implements TypeVisitor {

  private final ClassDeclaration proxiedClass;
  private final String fieldName;
  private final String attribute;
  private final ClassWriter writer;
  private final boolean lastParam;
  private final int indentation;
  private TypeVariable typeVariable;

  public JsonConstructorSuperCallVisitor(
      ClassDeclaration proxiedClass, String fieldName, 
      String attribute, ClassWriter writer, 
      boolean lastParam, int indentation) {
    this.proxiedClass = proxiedClass;
    this.fieldName = fieldName;
    this.attribute = attribute;
    this.writer = writer;
    this.lastParam = lastParam;
    this.indentation = indentation;
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

  private void handleClassOrInterfaceDeclaration(TypeDeclaration typeDecl) {
    String attributeExpression = (attribute == null ? "" : "." + attribute);
    String paramSeparator = (lastParam ? "" : ",");

    if (typeDecl.getQualifiedName().equals(String.class.getName())) {
      writer.writeLine(
          indentation, "fields.getString(\"", fieldName, "\")", 
          paramSeparator);
      return;
    }

    if (attribute != null) {

      FieldDeclaration attributeDecl = CodeGenHelper.getField(
          typeDecl.getFields(), attribute);

      if (attributeDecl == null 
          || (!attributeDecl.getModifiers().contains(Modifier.PUBLIC)
              && !CodeGenHelper.isSuperClass(
                  attributeDecl.getDeclaringType(), proxiedClass))) {
        MethodDeclaration methodDecl = CodeGenHelper.getMethod(
            typeDecl.getMethods(), CodeGenHelper.getGetterName(attribute));
        if (methodDecl == null) {
          throw new RuntimeException(
              "No public attribute or getter for field "
                  + attribute + " in " + proxiedClass + "." + fieldName);
        } else {
          attributeExpression = 
              "." + CodeGenHelper.getGetterName(attribute) + "()";
        }
      }
    }

    if (CodeGenHelper.isStorable(typeDecl)) {
      writer.writeLine(
          indentation, "entityService.<",  
          (typeVariable == null ? 
              getClassName(typeDecl, GenType.PROXY) : typeVariable.toString()),
          ">getInstance(fields.getObject(\"", fieldName, "\")).getObject()", 
          attributeExpression, paramSeparator);
    } else if (CodeGenHelper.isEmbeddable(typeDecl)) {
      writer.writeLine(
          indentation, "factory.newInstance(", 
          (typeVariable == null ? "" : "(Class<" + typeVariable.toString() + ">) "),
          getClassName(typeDecl, GenType.PROXY), ".class, fields.getObject(\"",
          fieldName, "\"))", attributeExpression, paramSeparator);
    }       
  }
  
  @Override
  public void visitClassType(ClassType t) {
    handleClassOrInterfaceDeclaration(t.getDeclaration());
  }

  @Override
  public void visitDeclaredType(DeclaredType t) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void visitEnumType(EnumType t) {
    String paramSeparator = (lastParam ? "" : ",");
    ClassDeclaration classDecl = t.getDeclaration();
    writer.writeLine(
        indentation, getClassName(classDecl, GenType.PROXY), ".valueOf(", 
        getClassName(classDecl, GenType.PROXY), 
        ".class, fields.getString(\"", fieldName, "\"))", paramSeparator);
  }

  @Override
  public void visitInterfaceType(InterfaceType t) {
    handleClassOrInterfaceDeclaration(t.getDeclaration());
  }

  @Override
  public void visitPrimitiveType(PrimitiveType t) {
    String paramSeparator = (lastParam ? "" : ",");
    switch (t.getKind()) {
      case BOOLEAN:
        writer.writeLine(
            indentation, "fields.getBoolean(\"", fieldName, "\")", 
            paramSeparator);
        break;
      case BYTE:
        // TODO
        break;
      case CHAR:
        // TODO
        break;
      case DOUBLE:
        writer.writeLine(
            indentation, "fields.getDouble(\"", fieldName, "\")", 
            paramSeparator);
        break;
      case FLOAT:
        writer.writeLine(
            indentation, "fields.getNumber(\"", fieldName, "\")",
            paramSeparator);
        break;
      case INT:
        writer.writeLine(
            indentation, "fields.getInt(\"", fieldName, "\")",
            paramSeparator);
        break;
      case LONG:
        writer.writeLine(
            indentation, "fields.getInt(\"", fieldName, "\")",
            paramSeparator);
        break;
      case SHORT:
        writer.writeLine(
            indentation, "fields.getInt(\"", fieldName, "\")",
            paramSeparator);
        break;
    }
  }

  @Override
  public void visitReferenceType(ReferenceType referenceType) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void visitTypeMirror(TypeMirror arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }

  @Override
  public void visitTypeVariable(TypeVariable typeVariable) {
    Collection<ReferenceType> bounds = typeVariable.getDeclaration().getBounds();
    if (bounds.size() == 1) {
      this.typeVariable = typeVariable;
      bounds.iterator().next().accept(this);
    } else {
      // TODO: Handle zero or multiple bounds
      throw new UnsupportedOperationException("Not yet implemented");
    }
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
