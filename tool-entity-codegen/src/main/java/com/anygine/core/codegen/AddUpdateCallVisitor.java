package com.anygine.core.codegen;

import java.util.Collection;
import java.util.List;

import com.anygine.core.common.client.domain.impl.Entity;
import com.anygine.core.common.client.domain.impl.JsonWritable;
import com.anygine.core.common.client.domain.impl.JsonWritableHelper;
import com.anygine.core.common.codegen.api.EntityInternal;
import com.anygine.core.common.codegen.api.JsonWritableInternal;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
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

public class AddUpdateCallVisitor implements TypeVisitor {

  private final FieldDeclaration fieldDecl;
  private final String fieldName;
  private final String fieldExpression;
  private final ClassWriter writer;
  private final int indent;

  public AddUpdateCallVisitor(
      FieldDeclaration fieldDecl, MethodDeclaration getter, ClassWriter writer, 
      int indent) {
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
  public void visitAnnotationType(AnnotationType arg0) {
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
      public void visitClassType(ClassType arg0) {
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
            updateArrayOfArrays(t);
          }
          
          @Override
          public void visitEnumType(EnumType t) {
            updateArrayOfArrays(t);
          }
          
          @Override
          public void visitDeclaredType(DeclaredType t) {
            updateArrayOfArrays(t);
          }
          
          @Override
          public void visitClassType(ClassType t) {
            updateArrayOfArrays(t);
          }
          
          private void updateArrayOfArrays(DeclaredType t) {
            TypeDeclaration typeDecl = t.getDeclaration();
            if (CodeGenHelper.isStorable(typeDecl)
                || CodeGenHelper.isEmbeddable(typeDecl)) {
              writer.writeLine(
                  indent, CodeGenHelper.getClassName(JsonWritableHelper.class), 
                  ".updateArrayOfArrays(", typeDecl.getQualifiedName(), 
                  ".class, ", fieldExpression, ", jsonObj, \"", fieldName, 
                  "\");");
            } else {
              writer.writeLine(
                  indent, "// Not adding write calls for: ", t.toString());
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
        typeArg.accept(new UpdateListVisitor(
            fieldDecl, fieldExpression, writer, indent));
      }
    } else if (CodeGenHelper.isStorable(typeDecl)) {
      writer.writeLine(
          indent, CodeGenHelper.getClassName(JsonWritableHelper.class), 
          ".updateEntity(", typeDecl.getQualifiedName(), ".class, (", 
          typeDecl.getQualifiedName(), "_Storable) ", 
          fieldExpression, ", jsonObj, \"", fieldName, "\");");
    } else if (CodeGenHelper.isEmbeddable(typeDecl)) {
      writer.writeLine(
          indent, CodeGenHelper.getClassName(JsonWritableHelper.class), 
          ".update(", typeDecl.getQualifiedName(), "_Embeddable.class, (",
          typeDecl.getQualifiedName(), "_Embeddable) ",
          fieldExpression, ", jsonObj, \"", fieldName, "\");");
    } else if (
        typeDecl.getQualifiedName().equals(String.class.getName())
        || typeDecl.getQualifiedName().equals(Long.class.getName())
        || typeDecl.getQualifiedName().equals(Integer.class.getName())
        || typeDecl.getQualifiedName().equals(Short.class.getName())
        || typeDecl.getQualifiedName().equals(Byte.class.getName())
        || typeDecl.getQualifiedName().equals(Float.class.getName())
        || typeDecl.getQualifiedName().equals(Double.class.getName())
        || typeDecl.getQualifiedName().equals(Character.class.getName())
        || typeDecl.getQualifiedName().equals(Boolean.class.getName())
        ) {
      // TODO: CONTINUE HERE - add other primitive wrappers + ...
      writer.writeLine(
          indent, CodeGenHelper.getClassName(JsonWritableHelper.class), 
          ".update(", fieldExpression, ", jsonObj, \"", fieldName, "\");");
    } else { 
      writer.writeLine(
          indent, "// Not adding update calls for: ", t.toString());
    }
  }

  @Override
  public void visitEnumType(EnumType t) {
    writer.writeLine(
        indent, CodeGenHelper.getClassName(JsonWritableHelper.class), 
        ".updateEnum(", fieldExpression, ", jsonObj, \"", fieldName, "\");");
  }

  @Override
  public void visitInterfaceType(InterfaceType t) {
    visitDeclaredType(t);
  }

  @Override
  public void visitPrimitiveType(PrimitiveType t) {
    writer.writeLine(
        indent, CodeGenHelper.getClassName(JsonWritableHelper.class), 
        ".update(", fieldExpression, ", jsonObj, \"", fieldName, "\");");
  }

  @Override
  public void visitReferenceType(ReferenceType arg0) {
  }

  @Override
  public void visitTypeMirror(TypeMirror arg0) {
  }

  @Override
  public void visitTypeVariable(TypeVariable arg0) {
  }

  @Override
  public void visitVoidType(VoidType arg0) {
  }

  @Override
  public void visitWildcardType(WildcardType arg0) {
  }

}
