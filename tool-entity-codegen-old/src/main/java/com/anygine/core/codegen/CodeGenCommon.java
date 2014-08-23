package com.anygine.core.codegen;

import com.anygine.core.codegen.CodeGenHelper.ClassNameType;
import com.anygine.core.common.client.annotation.Embeddable;
import com.anygine.core.common.client.annotation.Storable;
import com.anygine.core.common.codegen.api.EntityInternal;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.type.ArrayType;
import com.sun.mirror.type.ClassType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.type.TypeMirror;

// TODO: Replace ICheckpointable with injected class to avoid 
//       circular dependencies
public class CodeGenCommon {
  
  public static Class getClass(String className) {
    Class clazz = null;
    try {
      clazz = Class.forName(className);
    } catch (ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
    return clazz;
  }

  public static final boolean isPublic(Declaration declaration) {
    return (declaration != null && declaration.getModifiers().contains(Modifier.PUBLIC));
  }

  public static final boolean isPrivate(Declaration declaration) {
    return (declaration != null && declaration.getModifiers().contains(Modifier.PRIVATE));
  }

  public static final boolean isProtected(Declaration declaration) {
    return (declaration != null && declaration.getModifiers().contains(Modifier.PROTECTED));
  }

  public static final boolean isFinal(Declaration declaration) {
    return (declaration != null && declaration.getModifiers().contains(Modifier.FINAL));
  }

  public static final boolean isStatic(Declaration declaration) {
    return (declaration != null && declaration.getModifiers().contains(Modifier.STATIC));
  }

  public static final boolean isTransient(Declaration declaration) {
    return (declaration != null && declaration.getModifiers().contains(Modifier.TRANSIENT));
  }

  public static final boolean isNative(Declaration declaration) {
    return (declaration != null && declaration.getModifiers().contains(Modifier.NATIVE));
  }

  public static final boolean isAbstract(Declaration declaration) {
    return (declaration != null && declaration.getModifiers().contains(Modifier.ABSTRACT));
  }

  public static final boolean isInstanceOfInnerClass(FieldDeclaration field) {
    TypeMirror t = field.getType();
    ClassDeclaration classDeclaration;
    if (t instanceof ArrayType) {
      t = ((ArrayType) t).getComponentType();
    }
    if (t instanceof ClassType) {
      classDeclaration = ((ClassType) t).getDeclaration();
      TypeDeclaration parentClassTD = classDeclaration.getDeclaringType();
      return (parentClassTD != null);
    }
    return false;
  }    

  public static final String getPropertyName(MethodDeclaration method) {
    String methodName = method.getSimpleName();
    int startPos = 2;
    if (methodName.startsWith("set") || methodName.startsWith("get")) {
      startPos = 3;
    }
    else if (!methodName.startsWith("is")) {
      throw new RuntimeException("Method is not a setter or getter");
    }
    String fieldName = methodName.substring(startPos, methodName.length());
    return fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1, fieldName.length());

  }

  public static final boolean implementsInterface(
      ClassDeclaration classDecl, Class<?> clazz) {
    for (InterfaceType superInterface : classDecl.getSuperinterfaces()) {
      if (clazz.getName().equals(superInterface.toString())) {
        return true;
      }
    }
    return false;
  }
  
  public static final boolean isGenerated(Declaration decl) {
    return decl.getSimpleName().contains(Constants.CLASS_SUFFIX);    
  }

}
