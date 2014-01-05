package com.anygine.core.codegen;

import static com.anygine.core.codegen.CodeGenHelper.*;

import java.util.Collection;
import java.util.List;

import com.anygine.core.common.client.domain.impl.JsonWritableHelper;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.type.AnnotationType;
import com.sun.mirror.type.ArrayType;
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

public class AddWriteCallVisitor implements TypeVisitor {

  private final String methodName;
  private final FieldDeclaration fieldDecl;
  private final String fieldName;
  private final String fieldExpression;
  private final ClassWriter writer;
  private int indent;
  
  public AddWriteCallVisitor(
      String methodName, FieldDeclaration fieldDecl, MethodDeclaration getter, 
      ClassWriter writer, int indent
      ) {
    this.methodName = methodName;
    this.fieldDecl = fieldDecl;
    this.fieldName = CodeGenHelper.getFieldName(fieldDecl);
    if (getter == null) {
      fieldExpression = fieldDecl.getSimpleName();
    } else {
      fieldExpression = getter.getSimpleName() + "()";
    }
    this.writer = writer;
    this.indent = indent;
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
  public void visitTypeVariable(TypeVariable typeVariable) {
    Collection<ReferenceType> bounds = typeVariable.getDeclaration().getBounds();
    if (bounds.size() == 1) {
      bounds.iterator().next().accept(this);
    } else {
      // TODO: Handle zero or multiple bounds
      throw new UnsupportedOperationException("Not yet implemented");
    }
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
  public void visitPrimitiveType(PrimitiveType t) {
    writer.writeLine(
        indent, "writer.value(\"", fieldName, "\", ", fieldExpression, ");");
  }
  
  @Override
  public void visitInterfaceType(InterfaceType t) {
    InterfaceDeclaration ifDecl = t.getDeclaration();
    if (ifDecl.getQualifiedName().equals(List.class.getName())) {
      Collection<TypeMirror> typeArgs = t.getActualTypeArguments();
      if (typeArgs.size() != 0) {
        TypeMirror typeArg = typeArgs.iterator().next();
        typeArg.accept(new WriteListVisitor(fieldDecl, writer, indent, methodName));
      }
    } else if (CodeGenHelper.isStorable(ifDecl)) {
      if (methodName.equals("_write")) {
        writer.writeLine(
            indent, CodeGenHelper.getClassName(JsonWritableHelper.class),
            ".writeEntity((", 
            getClassName(ifDecl, GenType.PROXY, ClassNameType.QUALIFIED), 
            ") ", fieldExpression, ", entityWriter, \"", fieldName, "\");");
      } else {
        writer.writeLine(
            indent, CodeGenHelper.getClassName(JsonWritableHelper.class),
            ".write((", 
            getClassName(ifDecl, GenType.PROXY, ClassNameType.QUALIFIED), 
            ") ", fieldExpression, ", writer, \"", fieldName, "\");");
      }
    } else if (CodeGenHelper.isEmbeddable(ifDecl)) {
      writer.writeLine(
          indent, CodeGenHelper.getClassName(JsonWritableHelper.class), 
          ".write((", 
          getClassName(ifDecl, GenType.PROXY, ClassNameType.QUALIFIED), 
          ") ", fieldExpression, ", writer, \"", fieldName, "\");");
    } else {
      writer.writeLine(
          indent, "// Not adding write calls for: ", t.toString());
      System.out.println("Not adding write calls for: " + t.toString());
    }
  }
  
  @Override
  public void visitEnumType(EnumType t) {
    writer.writeLine(
        indent, CodeGenHelper.getClassName(JsonWritableHelper.class), 
        ".writeEnum(", fieldExpression, ", writer, \"", fieldName, "\");");
  }
  
  @Override
  public void visitDeclaredType(DeclaredType arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }
  
  @Override
  public void visitClassType(com.sun.mirror.type.ClassType t) {
    ClassDeclaration classDecl = t.getDeclaration();
    if (CodeGenHelper.isStorable(classDecl)) {
      if (methodName.equals("_write")) {
        writer.writeLine(
            indent, CodeGenHelper.getClassName(JsonWritableHelper.class), 
            ".writeEntity((", 
            getClassName(classDecl, GenType.PROXY, ClassNameType.QUALIFIED), 
            ") ", fieldExpression, ", entityWriter, \"", fieldName, "\");");
      } else {
        writer.writeLine(
            indent, CodeGenHelper.getClassName(JsonWritableHelper.class), 
            ".write((", 
            getClassName(classDecl, GenType.PROXY, ClassNameType.QUALIFIED), 
            ") ", fieldExpression, ", writer, \"", fieldName, "\");");
      }
    } else if (CodeGenHelper.isEmbeddable(classDecl)) {
      writer.writeLine(
          indent, CodeGenHelper.getClassName(JsonWritableHelper.class), 
          ".write((", 
          getClassName(classDecl, GenType.PROXY, ClassNameType.QUALIFIED), 
          ") ", fieldExpression, ", writer, \"", fieldName, "\");");
    } else if (CodeGenHelper.isString(classDecl)) {
      writer.writeLine(
          indent, CodeGenHelper.getClassName(JsonWritableHelper.class), 
          ".writeString(", fieldExpression, ", writer, \"", fieldName, 
          "\");");
    } else {
      writer.writeLine(
          indent, "// Not adding write calls for: ", t.toString());
      System.out.println("Not adding write calls for: " + t.toString());
    }
  }
  
  @Override
  public void visitArrayType(ArrayType t) {
    t.getComponentType().accept(new TypeVisitor() {
      
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
      public void visitClassType(com.sun.mirror.type.ClassType arg0) {
        // TODO: Implement
        throw new UnsupportedOperationException("Not yet implemented");
      }
      
      @Override
      public void visitArrayType(ArrayType t) {
        t.getComponentType().accept(new TypeVisitor() {
          
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
          public void visitInterfaceType(InterfaceType t) {
            InterfaceDeclaration ifDecl = t.getDeclaration();
            if (CodeGenHelper.isStorable(ifDecl)
                || CodeGenHelper.isEmbeddable(ifDecl)) {
              writer.writeLine(
                  indent, CodeGenHelper.getClassName(JsonWritableHelper.class), 
                  ".writeArrayOfArrays(", fieldExpression, 
                  ", writer, \"", fieldName, "\");");
            } else {
              writer.writeLine(
                  indent, "// Not adding write calls for: ", t.toString());
              System.out.println("Not adding write calls for: " + t.toString());
            }          
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
          public void visitClassType(com.sun.mirror.type.ClassType t) {
            ClassDeclaration classDecl = t.getDeclaration();
            if (CodeGenHelper.isStorable(classDecl)
                || CodeGenHelper.isEmbeddable(classDecl)) {
              writer.writeLine(
                  indent, CodeGenHelper.getClassName(JsonWritableHelper.class),
                  ".writeArrayOfArrays(", fieldExpression, 
                  ", writer, \"", fieldName, "\");");
            } else {
              writer.writeLine(
                  indent, "// Not adding write calls for: ", t.toString());
              System.out.println("Not adding write calls for: " + t.toString());
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
        });
      }
      
      @Override
      public void visitAnnotationType(AnnotationType arg0) {
        // TODO: Implement
        throw new UnsupportedOperationException("Not yet implemented");
      }
    });
  }
  
  @Override
  public void visitAnnotationType(AnnotationType arg0) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }

}
