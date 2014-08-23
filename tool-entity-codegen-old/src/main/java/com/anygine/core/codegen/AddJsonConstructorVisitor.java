package com.anygine.core.codegen;

import java.util.Collection;

import com.anygine.core.common.client.domain.impl.JsonWritableHelper;
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

public class AddJsonConstructorVisitor implements TypeVisitor {

  private final FieldDeclaration fieldDecl;
  private final String fieldName;
  private final ClassWriter writer;
  private final int indent;
  private final String prefix;
  private final String suffix;
  
  public AddJsonConstructorVisitor(
      FieldDeclaration fieldDecl, ClassWriter writer,
      int indent, String prefix, String suffix) {
    this.fieldDecl = fieldDecl;
    this.fieldName = CodeGenHelper.getFieldName(fieldDecl);
    this.writer = writer;
    this.indent = indent;
    this.prefix = prefix;
    this.suffix = suffix;
  }

  @Override
  public void visitWildcardType(WildcardType t) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }
  
  @Override
  public void visitVoidType(VoidType t) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }
  
  @Override
  public void visitTypeVariable(TypeVariable t) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }
  
  @Override
  public void visitTypeMirror(TypeMirror t) {
    // TODO: Implement
    throw new UnsupportedOperationException("Not yet implemented");
  }
  
  @Override
  public void visitReferenceType(ReferenceType t) {
    // TODO: Implement
  }
  
  @Override
  public void visitPrimitiveType(PrimitiveType t) {
    writer.write(indent, prefix);
    switch (t.getKind()) {
      case BOOLEAN:
        writer.write(0, "fields.getBoolean(\"", fieldName, "\")");
        break;
      case BYTE:
        // TODO
        break;
      case CHAR:
        // TODO
        break;
      case DOUBLE:
        writer.write(0, "fields.getDouble(\"", fieldName, "\")");
        break;
      case FLOAT:
        writer.write(0, "fields.getNumber(\"", fieldName, "\")");
        break;
      case INT:
        writer.write(0, "fields.getInt(\"", fieldName, "\")");
        break;
      case LONG:
        writer.write(0, "(long) fields.getInt(\"", fieldName, "\")");
        break;
      case SHORT:
        writer.write(0, "(short) fields.getInt(\"", fieldName, "\")");
        break;
    }
    writer.writeLine(0, suffix);
  }
  
  @Override
  public void visitInterfaceType(InterfaceType t) {
    visitDeclaredType(t);

  }
  
  @Override
  public void visitEnumType(EnumType t) {
    ClassDeclaration classDecl = t.getDeclaration();
    writer.write(indent, prefix);
    writer.write(
        0, classDecl.getQualifiedName(), ".valueOf(", 
        classDecl.getQualifiedName(), ".class, fields.getString(\"", 
        fieldName, "\"))");
    writer.writeLine(0, suffix);
  }
  
  @Override
  public void visitDeclaredType(DeclaredType type) {
    TypeDeclaration decl = type.getDeclaration();
    if (CodeGenHelper.isCollection(decl)) {
      Collection<TypeMirror> typeArgs = type.getActualTypeArguments();
      if (typeArgs.size() != 0) {
        TypeMirror typeArg = typeArgs.iterator().next();
        typeArg.accept(new TypeVisitor() {
          
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
          public void visitInterfaceType(InterfaceType interfaceType) {
            visitDeclaredType(interfaceType);
          }
          
          @Override
          public void visitEnumType(EnumType arg0) {
            // TODO: Implement
            throw new UnsupportedOperationException("Not yet implemented");
          }
          
          @Override
          public void visitDeclaredType(DeclaredType type) {
            TypeDeclaration decl = type.getDeclaration();
            if (CodeGenHelper.isStorable(decl)
                || CodeGenHelper.isEmbeddable(decl)) {
              writer.writeLine(
                  indent, JsonWritableHelper.class.getName(), ".readCollection(", 
//                  CodeGenHelper.getClassLiteralName(decl, CodeGenHelper.ClassNameType.QUALIFIED), ", ",
                      fieldName, ", fields.getObject(\"", fieldName, "\"));");
            }
          }
          
          @Override
          public void visitClassType(ClassType classType) {
            visitDeclaredType(classType);
            /*
            ClassDeclaration classDecl = t.getDeclaration();
            if (CodeGenHelper.isStorable(classDecl)) {
              writer.write(
                  0, "factory.newEntityList(", 
                  classDecl.getQualifiedName(), ".class, fields.getObject(\"", 
                  fieldName, "\"))");
            } else if (CodeGenHelper.isEmbeddable(classDecl)) {
              writer.write(
                  0, "factory.newList(", 
                  classDecl.getQualifiedName(), ".class, fields.getObject(\"",
                  fieldName, "\"))");
            }        
            */  
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
    } else {
      writer.write(indent, prefix);
      if (CodeGenHelper.isStorable(decl)) {
        writer.write(0, "entityService.getInstance(", 
            decl.getQualifiedName(),
            ".class, fields.getObject(\"", fieldName, "\")).getObject()");
      } else if (CodeGenHelper.isEmbeddable(decl)) {
        writer.write(0, "factory.newInstance(", 
            decl.getQualifiedName(), "_Embeddable.class, fields.getObject(\"", 
            fieldName, "\"))");
      } else {
        System.out.println(
            "WARN: Not adding field construction for: " + type.toString());
        writer.write(0, "null /* Missing rule */");
      }
      writer.writeLine(0, suffix);
    } 
  }
  
  @Override
  public void visitClassType(ClassType t) {
    ClassDeclaration classDecl = t.getDeclaration();
    if (CodeGenHelper.isString(classDecl)) {
      writer.write(indent, prefix);
      writer.write(0, "fields.getString(\"", fieldName, "\")");
      writer.writeLine(0, suffix);
    } else if (CodeGenHelper.isEnum(classDecl)) {
      writer.write(indent, prefix);
      writer.write(
          0, classDecl.getQualifiedName(), ".valueOf(fields.getString(\"", 
          fieldName, "\"))");
      writer.writeLine(0, suffix);
    } else {
      visitDeclaredType(t);
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
            visitDeclaredType(t);            
          }
          
          @Override
          public void visitEnumType(EnumType arg0) {
            // TODO: Implement
            throw new UnsupportedOperationException("Not yet implemented");
          }
          
          @Override
          public void visitDeclaredType(DeclaredType t) {
            TypeDeclaration classDecl = t.getDeclaration();
            if (CodeGenHelper.isStorable(classDecl)) {
              writer.write(
                  0, "factory.newEntityArrayOfArrays(", 
                  classDecl.getQualifiedName(), ".class, fields.getObject(\"",
                  fieldName, "\"))");
            } else if (CodeGenHelper.isEmbeddable(classDecl)) {
              writer.write(
                  0, "factory.newArrayOfArrays(fields.getObject(\"",
                  fieldName, "\"))");
            }    
          }
          
          @Override
          public void visitClassType(ClassType t) {
            visitDeclaredType(t);         
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
  public void visitAnnotationType(AnnotationType t) {
    // TODO: Implement
  }
}
