package com.anygine.core.codegen;

import static com.anygine.core.codegen.Constants.NEW_LINE;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;

import playn.core.Json;

import com.anygine.common.entity.inject.CommonCommonEntityInjector;
import com.anygine.core.common.client.annotation.Embeddable;
import com.anygine.core.common.client.annotation.Field;
import com.anygine.core.common.client.annotation.FieldRef;
import com.anygine.core.common.client.annotation.Storable;
import com.anygine.core.common.client.api.EntityFactory;
import com.anygine.core.common.client.api.EntityService;
import com.anygine.core.common.client.api.JsonWritableFactory;
import com.anygine.core.common.client.api.UniqueConstraintViolationException;
import com.anygine.core.common.client.domain.impl.EntityHolder;
import com.anygine.core.common.client.domain.impl.EntityWriter;
import com.anygine.core.common.codegen.api.EntityInternal;
import com.anygine.core.common.codegen.api.JsonWritableInternal;
import com.anygine.core.common.codegen.api.JsonWritableInternal.JsonType;
import com.anygine.core.common.codegen.api.JsonWritableInternal.TypeOfData;
import com.anygine.core.common.codegen.api.MetaModel;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.AnnotationTypeElementDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.ConstructorDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.EnumConstantDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.declaration.ExecutableDeclaration;
import com.sun.mirror.declaration.FieldDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.MemberDeclaration;
import com.sun.mirror.declaration.MethodDeclaration;
import com.sun.mirror.declaration.Modifier;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.ParameterDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.TypeParameterDeclaration;
import com.sun.mirror.type.DeclaredType;
import com.sun.mirror.type.InterfaceType;
import com.sun.mirror.util.DeclarationVisitor;
import com.sun.mirror.util.Types;


public class CodeGenHelper {

  static AnnotationProcessorEnvironment env;

  public enum ProxyStrategy {
    INHERITANCE,
    DELEGATION
  }

  static enum GenType {
    PROXY, META_MODEL;
  }

  static enum TypeExprType {
    NAME, FULL;
  }
  
  public static enum ClassNameType {
    SIMPLE,
    QUALIFIED
  }

  public enum GetterOrSetter {
    GETTER,
    SETTER;

    private String getPrefix(FieldDeclaration field) {
      if (this == SETTER) {
        return "set"; 
      }
      else if (isBoolean(field)) {
        return "is";
      }
      return "get";
    }

    public String getName(FieldDeclaration field) {
      return getName(field, null);
    }

    public String getName(FieldDeclaration field, String forcedPrefix) {
      String fieldName = field.getSimpleName();
      return (forcedPrefix == null ? getPrefix(field) : forcedPrefix)
          + fieldName.substring(0, 1).toUpperCase()
          + fieldName.substring(1, fieldName.length());
    }
  }

  static StringBuilder indent(int indentationLevel,
      StringBuilder classDefinition) {
    for (int i = 0; i < indentationLevel; i++) {
      classDefinition.append(ClassWriter.INDENTATION_UNIT);
    }
    return classDefinition;
  }

  static String getClassName(TypeDeclaration decl, GenType classType) {
    return getClassName(decl, classType, ClassNameType.QUALIFIED);
  }

  static String getClassName(
      TypeDeclaration decl, GenType classType, ClassNameType classNameType) {
    TypeDeclaration td = decl.getDeclaringType();
    if (classNameType == ClassNameType.QUALIFIED) {
      if (td != null && (isStorable(decl) || isEmbeddable(decl))) {
        return td.getQualifiedName() + "_" + decl.getSimpleName()
            + getSuffix(decl, classType);
      }
      return decl.getQualifiedName() + getSuffix(decl, classType);
    } else {
      if (td != null && (isStorable(decl) || isEmbeddable(decl))) {
        return td.getSimpleName() + "_" + decl.getSimpleName()
            + getSuffix(decl, classType);
      }
      return decl.getSimpleName() + getSuffix(decl, classType);
    }
  }

  private static String getSuffix(
      TypeDeclaration decl, GenType classType) {
    switch (classType) {
    case PROXY:
      if (decl.getAnnotation(Storable.class) != null) {
        return "_Storable";
      } else if (decl.getAnnotation(Embeddable.class) != null) {
        return "_Embeddable";
      }
      return "";
//      throw new IllegalArgumentException(
//          "No recognized annotation for type " + decl.toString());
    case META_MODEL:
      return "_MetaModel";
    }
    throw new IllegalArgumentException(
        "Unknown class type " + classType.toString());
  }  

  public static boolean isCollection(TypeDeclaration typeDecl) {
    Collection<InterfaceType> superTypes = typeDecl.getSuperinterfaces();
    for (InterfaceType superType : superTypes) {
      if (superType.toString().startsWith("java.util.Collection")) {
        return true;
      }
    }
    return false;
  }

  public static boolean isComparable(DeclaredType type) {
    Collection<InterfaceType> superTypes = type.getSuperinterfaces();
    for (InterfaceType superType : superTypes) {
      if (superType.toString().startsWith("java.lang.Comparable")) {
        return true;
      }
    }
    return false;
  }

  public static boolean isEmbeddable(TypeDeclaration typeDecl) {
    return typeDecl.getAnnotation(Embeddable.class) != null;
  }

  public static boolean isStorable(TypeDeclaration typeDecl) {
    return typeDecl.getAnnotation(Storable.class) != null;
  }

  public static boolean isStorable(FieldDeclaration fieldDecl) {
    IsTypeStorableVisitor visitor = new IsTypeStorableVisitor();
    fieldDecl.getType().accept(visitor);
    return visitor.isStorable();
  }

  public static boolean isDelegatable(MethodDeclaration method) {
    return (method.getSimpleName().startsWith("set") && !CodeGenCommon.isPrivate(method)
        && !CodeGenCommon.isFinal(method) && !CodeGenCommon.isStatic(method) 
        && !CodeGenCommon.isNative(method) && !CodeGenCommon.isAbstract(method));
  }

  public static String getProxiedObject(ClassDeclaration clazz,
      ProxyStrategy proxyStrategy) {
    if (proxyStrategy == ProxyStrategy.INHERITANCE) {
      return "super";
    } else {
      // When using delegation, object name is class name with first
      // character in lower-case
      String className = clazz.getSimpleName();
      return className.substring(0, 1).toLowerCase() + className.substring(1, className.length());
    }
  }

  public static void addPackage(
      TypeDeclaration typeDecl, GenType classType, ClassWriter writer) {
    writer.setQualifiedName(getClassName(
        typeDecl, classType, ClassNameType.QUALIFIED));
    writer.writeLine(
        0, "package ", typeDecl.getPackage().toString(), ";", NEW_LINE);
  }

  public static void addFactoriesAndServices(
      ClassDeclaration decl, ClassWriter writer) {
    String factoryName = JsonWritableFactory.class.getName();
    String factorySimpleName = JsonWritableFactory.class.getSimpleName();
    if (decl.getAnnotation(Storable.class) != null) {
      factoryName = EntityFactory.class.getName();
      factorySimpleName = EntityFactory.class.getSimpleName();
    }
    int indent = getMethodHeaderIndentation();
    // NOTE: Cannot refer to these classes since it would result in circular
    //       component dependencies
    writer.writeLine(
        indent, "private static ", factoryName, 
        " factory = com.anygine.common.inject.InjectorManager.getInstance().",
        "getInjector(", CommonCommonEntityInjector.class.getName(), ".class).get",
        factorySimpleName, "();", 
        NEW_LINE);
    writer.writeLine(
        indent, "private static ", EntityService.class.getName(), 
        " entityService = com.anygine.common.inject.InjectorManager.getInstance().",
        "getInjector(", CommonCommonEntityInjector.class.getName(), ".class).getEntityService();", 
        NEW_LINE);
  }

  public static void addClassHeader(
      ClassDeclaration decl, GenType classType, ClassWriter writer) {
    writer.setQualifiedName(getClassName(
        decl, classType, ClassNameType.QUALIFIED));
    writer.writeLine(
        0, "public ", 
        CodeGenCommon.isAbstract(decl) ? "abstract " : "", "class ", 
            getClassName(decl, classType, ClassNameType.SIMPLE),
            getAnyTypeParameters(decl, TypeExprType.FULL),
            " extends ", decl.getQualifiedName(),
            getAnyTypeParameters(decl, TypeExprType.NAME),
            " implements ", 
            getImplementedInterfaces(decl), " { ", NEW_LINE);
  }

  private static String getAnyTypeParameters(TypeDeclaration decl, TypeExprType typeExprType) {
    StringBuffer typeParamsStrBuf = new StringBuffer();
    Collection<TypeParameterDeclaration> typeParams = decl.getFormalTypeParameters();
    if (typeParams.size() > 0) {
      String comma = "";
      typeParamsStrBuf.append("<");
      for (TypeParameterDeclaration typeParam : typeParams) {
        typeParamsStrBuf.append(comma).append(
            typeExprType == TypeExprType.FULL ? typeParam : typeParam.getSimpleName());
        comma = ", ";
      }
      typeParamsStrBuf.append(">");
    }
    return typeParamsStrBuf.toString();
  }
  
  // TODO: Write out type params 
  private static String getImplementedInterfaces(TypeDeclaration decl) {
    if (decl.getAnnotation(Embeddable.class) != null) {
      String iface = JsonWritableInternal.class.getName();
      if (decl instanceof ClassDeclaration) {
        if (holdsStorable((ClassDeclaration) decl)) {
          return iface + ", " + EntityHolder.class.getName() 
              + "<" + decl.getQualifiedName() + getAnyTypeParameters(decl, TypeExprType.NAME) + ">";
        }
      }
      return iface;
    } else if (decl.getAnnotation(Storable.class) != null) {
      return EntityInternal.class.getName() + "<" + decl.getQualifiedName() + 
          getAnyTypeParameters(decl, TypeExprType.NAME) + ">";
    } else {
      throw new IllegalArgumentException("Unsupported type: " + decl.toString());
    }
  }

  private static boolean holdsStorable(ClassDeclaration classDecl) {
    Collection<FieldDeclaration> fields = classDecl.getFields();
    for (FieldDeclaration field : fields) {
      // TODO: Implement
      if (isStorable(field) 
          //          || isCollectionOfStorables(field)
          //          || isArrayOfStorables(field)
          ) {
        return true;
      }
    }
    return false;
  }

  public static void addInterfaceHeader(
      InterfaceDeclaration decl, GenType classType, ClassWriter writer) {
    writer.setQualifiedName(getClassName(
        decl, classType, ClassNameType.QUALIFIED));
    writer.writeLine(
        0, "public interface ", getClassName(
            decl, classType, ClassNameType.SIMPLE), 
            getAnyTypeParameters(decl, TypeExprType.FULL), " extends ",
            decl.getQualifiedName(), getAnyTypeParameters(decl, TypeExprType.NAME), 
            ", ", getImplementedInterfaces(decl), " {", NEW_LINE);
  }

  public static Set<FieldDeclaration> getStorableFields(ClassDeclaration proxiedClass) {
    return getStorableFields(proxiedClass, null);
  }

  public static Set<FieldDeclaration> getStorableFields(
      ClassDeclaration proxiedClass, Modifier modifier) {
    Set<FieldDeclaration> storableFields = new HashSet<FieldDeclaration>();
    for (FieldDeclaration field : getAllFields(proxiedClass, true)) {
      if (isStorableField(proxiedClass, field) || isCheckpointable(proxiedClass, field)) {
        if ((field.getDeclaringType().getPackage().equals(proxiedClass.getPackage()) 
            && !(field.getModifiers().contains(Modifier.PRIVATE) 
                && !hasGetter(proxiedClass, field)))
                || field.getModifiers().contains(Modifier.PUBLIC)
                || field.getModifiers().contains(Modifier.PROTECTED)) {
          //            || modifier == null || field.getModifiers().contains(modifier)) {
          storableFields.add(field);
        }
      }
    }
    return storableFields;
  }

  public static void addStorableFields(
      ClassDeclaration proxiedClass, StringBuilder proxyClassDef) {
    for (FieldDeclaration field : getStorableFields(proxiedClass)) {
      if (isStorableField(proxiedClass, field) || isCheckpointable(field)) {
        addFieldIndentation(proxyClassDef);
        proxyClassDef.append("private ").append(field.getType()).append(" ")
        .append(field.getSimpleName()).append(";").append(Constants.NEW_LINE);
      }
    }
    proxyClassDef.append(Constants.NEW_LINE);
  }

  static Collection<MethodDeclaration> getAllMethods(ClassDeclaration classDeclaration) {
    Collection<MethodDeclaration> methods = classDeclaration.getMethods();
    ClassDeclaration superClass = classDeclaration.getSuperclass().getDeclaration();
    if (!"Object".equals(superClass.getSimpleName())) {
      methods.addAll(getAllMethods(superClass));
    }
    return methods;
  }

  // TODO: Possibly replace with a type visitor for added type safety / robustness
  private static boolean isBoolean(FieldDeclaration field) {
    return "boolean".equals(field.getType().toString());
  }

  private static MethodDeclaration findMethod(ClassDeclaration clazz, String name) {
    for (MethodDeclaration method : getAllMethods(clazz)) {
      if (method.getSimpleName().equals(name)) {
        return method;
      }
    }
    return null;
  }

  private static MethodDeclaration getGetterOrSetter(
      ClassDeclaration clazz, FieldDeclaration field,
      GetterOrSetter getterOrSetter) {
    String methodName = getterOrSetter.getName(field);
    MethodDeclaration method = findMethod(clazz, methodName);
    if (method == null) {
      if (isBoolean(field) && getterOrSetter == GetterOrSetter.GETTER) {
        method = findMethod(clazz, getterOrSetter.getName(field, "get"));
      }
    }
    return method;
  }

  private static String getGetExpression(
      ClassDeclaration clazz,
      FieldDeclaration field) {
    if (hasGetter(clazz, field)) {
      return getGetterOrSetter(clazz, field, GetterOrSetter.GETTER).getSimpleName() + "()";
    }
    return field.getSimpleName();
  }

  static MethodDeclaration getGetter(
      ClassDeclaration clazz, FieldDeclaration field) {
    return getGetterOrSetter(clazz, field, GetterOrSetter.GETTER);
  }

  static MethodDeclaration getSetter(
      ClassDeclaration clazz, FieldDeclaration field) {
    return getGetterOrSetter(clazz, field, GetterOrSetter.SETTER);
  }

  private static boolean isProperty(ClassDeclaration proxiedClass,
      FieldDeclaration field) {
    return ((CodeGenCommon.isPrivate(field) && getSetter(proxiedClass, field) != null && getGetter(proxiedClass, field) != null)
        || CodeGenCommon.isProtected(field));

  }

  private static boolean isCheckpointable(ClassDeclaration proxiedClass,
      FieldDeclaration field) {
    for (Modifier modifier : field.getModifiers()) {
      if (!checkpointableFieldModifiers.contains(modifier)) {
        return false;
      }
    }
    return isProperty(proxiedClass, field);
  }

  static void add_writeJsonMethod(
      ClassDeclaration classDecl, ClassWriter writer) {
    Set<FieldDeclaration> storableFields = 
        CodeGenHelper.getStorableFields(classDecl);
    int indent = getMethodHeaderIndentation();
    writer.writeLine(
        indent, "protected void _writeJson(", 
        Json.Writer.class.getName().replace("$", "."), " writer) {");
    indent++;
    if (CodeGenHelper.isStorable(classDecl)) {
      writer.writeLine(indent, "writer.value(\"id\", id);");
      writer.writeLine(indent, "writer.value(\"version\", version);");
    }
    addWriteCalls(classDecl, "_writeJson", writer, storableFields, indent);
    writer.writeLine(--indent, "}", NEW_LINE);
  }

  static String getFieldName(FieldDeclaration fieldDecl) {
    if (fieldDecl.getAnnotation(Field.class) != null) {
      return fieldDecl.getAnnotation(Field.class).name();
    } else {
      return fieldDecl.getSimpleName();
    }
  }

  private static void addWriteCalls(
      ClassDeclaration classDecl, String methodName, ClassWriter writer,
      Set<FieldDeclaration> storableFields, int indent) {
    for (final FieldDeclaration fieldDecl : storableFields) {
      MethodDeclaration getter = null;
      if (!isFieldAccessible(fieldDecl, classDecl)) {
        getter = getGetter(classDecl, fieldDecl);
        if (getter == null) {
          throw new IllegalArgumentException(
              "Field " + fieldDecl.toString() + " must either be non-private or "
                  + "have a corresponding getter method");
        }
      }
      fieldDecl.getType().accept(new AddWriteCallVisitor(
          methodName, fieldDecl, getter, writer, indent));
    }
  }

  static String getClassName(Class<?> klass) {
    return klass.getName().replace("$", ".");    
  }

  static void addWriteJson3ParamsMethod(
      ClassDeclaration classDecl, ClassWriter writer) {
    int indent = getMethodHeaderIndentation();
    writer.writeLine(indent, "@Override");
    writer.writeLine(
        indent, "public void writeJson(", getClassName(Json.Writer.class), 
        " writer, ", getClassName(String.class), " key, ",
        getClassName(TypeOfData.class), " typeOfData) {");
    writer.writeLine(++indent, "switch (typeOfData) {");
    writer.writeLine(++indent, "case Object:");
    writer.writeLine(++indent, "_writeJson(writer);");
    writer.writeLine(indent, "break;");
    writer.writeLine(--indent, "case Id:");
    writer.writeLine(++indent, "writer.value(\"id\", id);");
    writer.writeLine(indent, "break;");
    writer.writeLine(--indent, "case ChangedFields:");
    writer.writeLine(++indent, "// TODO: Implement");
    writer.writeLine(indent, "break;");
    indent -= 2;
    writer.writeLine(indent, "}");
    writer.writeLine(--indent, "}", NEW_LINE);
  }

  static void addWrite2ParamsMethod(
      ClassDeclaration classDecl, ClassWriter writer) {
    int indent = getMethodHeaderIndentation();
    writer.writeLine(indent, "@Override");
    writer.writeLine(
        indent, "public void write(", EntityWriter.class.getName(), 
        " entityWriter, ", getClassName(String.class), " key) {");
    writer.writeLine(
        ++indent, getClassName(Json.Writer.class), 
        " writer = entityWriter.getWriter();");
    writer.writeLine(
        indent, "_writeJsonHeader(writer, key, " +
            "", getClassName(TypeOfData.class), ".", TypeOfData.Object.name(), 
        ");");
    writer.writeLine(indent, "_write(entityWriter);");
    writer.writeLine(indent, "_writeJsonFooter(writer);");
    writer.writeLine(--indent, "}", NEW_LINE);
  }

  static void addWrite3ParamsMethod(
      ClassDeclaration classDecl, ClassWriter writer) {
    int indent = getMethodHeaderIndentation();
    writer.writeLine(indent, "@Override");
    writer.writeLine(
        indent, "public void write(", getClassName(EntityWriter.class), 
        " entityWriter, ", getClassName(String.class), " key, ",
        getClassName(TypeOfData.class), " typeOfData) {");
    writer.writeLine(
        ++indent, getClassName(Json.Writer.class), 
        " writer = entityWriter.getWriter();");
    writer.writeLine(
        indent, "_writeJsonHeader(writer, key, " +
            "", getClassName(TypeOfData.class), ".", TypeOfData.Object.name(), 
        ");");
    writer.writeLine(indent, "if (typeOfData == TypeOfData.Id) {");
    writer.writeLine(++indent, "writer.value(\"id\", id);");
    writer.writeLine(indent, "writer.value(\"version\", version);");
    writer.writeLine(--indent, "} else {");
    writer.writeLine(++indent, "_write(entityWriter);");
    writer.writeLine(--indent, "}");
    writer.writeLine(
        indent, "_writeJsonFooter(writer);");
    writer.writeLine(--indent, "}", NEW_LINE);
  }

  // JsonWritableInternal.addWriteJson(String key, Writer writer)
  static void addWriteJson2Params(
      ClassDeclaration classDecl, ClassWriter writer) {
    int indent = getMethodHeaderIndentation();
    writer.writeLine(indent, "@Override");
    writer.writeLine(
        indent, "public void writeJson(", getClassName(String.class), 
        " key, ", getClassName(Json.Writer.class), " writer) {");
    writer.writeLine(
        ++indent, "_writeJsonHeader(writer, key, ", 
        getClassName(TypeOfData.class), ".Object);");
    writer.writeLine(indent, "_writeJson(writer);");
    writer.writeLine(indent, "_writeJsonFooter(writer);");
    writer.writeLine(--indent, "}", NEW_LINE);
  }

  static void add_writeMethod(
      ClassDeclaration classDecl, ClassWriter writer) {
    Set<FieldDeclaration> storableFields = 
        CodeGenHelper.getStorableFields(classDecl);
    int indent = getMethodHeaderIndentation();
    writer.writeLine(
        indent, "protected void _write(", EntityWriter.class.getName(),
        " entityWriter) {");
    //    writer.writeLine(++indent, "super._write(entityWriter);");
    writer.writeLine(
        ++indent, "playn.core.Json.Writer writer = entityWriter.getWriter();");
    writer.writeLine(indent, "writer.value(\"id\", id);");
    writer.writeLine(indent, "writer.value(\"version\", version);");
    addWriteCalls(classDecl, "_write", writer, storableFields, indent);
    writer.writeLine(--indent, "}", NEW_LINE);
  }

  // Common helper method _writeJsonHeader(Writer writer, String key, TypeOfData typeOfData)
  static void add_writeJsonHeaderMethod(
      ClassDeclaration classDecl, ClassWriter writer) {
    int indent = getMethodHeaderIndentation();
    writer.writeLine(
        indent, "protected void _writeJsonHeader(", 
        getClassName(Json.Writer.class), " writer, ", 
        getClassName(String.class), " key, ", getClassName(TypeOfData.class),
        " typeOfData) {");
    writer.writeLine(++indent, "if (key != null) {");
    writer.writeLine(++indent, "writer.object(key);");
    writer.writeLine(--indent, "} else {");
    writer.writeLine(++indent, "writer.object();");
    writer.writeLine(--indent, "}");
    writer.writeLine(indent, "writer.value(\"type\", getJsonType().name());");
    writer.writeLine(indent, "writer.value(\"typeOfData\", typeOfData.name());");
    writer.writeLine(indent, "writer.object(typeOfData.name());");
    writer.writeLine(--indent, "}", NEW_LINE);
  }

  // Common helper method _writeJsonFooter(Writer writer)
  static void add_writeJsonFooterMethod(
      ClassDeclaration classDecl, ClassWriter writer) {
    int indent = getMethodHeaderIndentation();
    writer.writeLine(
        indent, "protected void _writeJsonFooter(", 
        getClassName(Json.Writer.class), " writer) {");
    writer.writeLine(++indent, "writer.end();");
    writer.writeLine(indent, "writer.end();");
    writer.writeLine(--indent, "}", NEW_LINE);
  }

  // EntityInternal.compareTo(T attribute, T value)
  public static void addCompareToMethod(
      ClassDeclaration classDecl, StringBuilder builder) {
    ClassWriter writer = new ClassWriter(builder);
    int indent = getMethodHeaderIndentation();
    writer.writeLine(indent, "@Override");
    writer.writeLine(
        indent, "public <T extends ", Comparable.class.getName(), 
        "<? extends T>> int compareTo(T attribute, T value) {");
    Set<FieldDeclaration> storableFields = 
        CodeGenHelper.getStorableFields(classDecl);
    writer.writeLine(++indent, "// TODO: Implement for all fields");
    for (FieldDeclaration fieldDecl : storableFields) {
      fieldDecl.getType().accept(new CompareToVisitor(
          classDecl, fieldDecl, writer, indent));
    }
    writer.writeLine(indent, "return -1;");
    writer.writeLine(--indent, "}", NEW_LINE);
  }

  public static void addEqualsMethod(
      ClassDeclaration classDecl, StringBuilder builder) {
    ClassWriter writer = new ClassWriter(builder);
    int indent = getMethodHeaderIndentation();
    writer.writeLine(indent, "@Override");
    writer.writeLine(indent, "@SuppressWarnings(\"unchecked\")");
    writer.writeLine(
        indent++, "public <E> boolean equals(", 
        MetaModel.class.getName(), "<E> metaModel, E otherEntity) {");
    Set<FieldDeclaration> storableFields = 
        CodeGenHelper.getStorableFields(classDecl);
    for (FieldDeclaration fieldDecl : storableFields) {
      fieldDecl.getType().accept(new EntityEqualsVisitor(
          classDecl, fieldDecl, writer, indent));
    }
    writer.writeLine(indent, "return false;");
    writer.writeLine(--indent, "}", NEW_LINE);
  }

  public static void addConstraintsMethod(
      ClassDeclaration classDecl, StringBuilder builder) {
    ClassWriter writer = new ClassWriter(builder);
    int indent = getMethodHeaderIndentation();
    writer.writeLine(indent, "@Override");
    writer.writeLine(
        indent, "public void checkUniqueConstraints(", SortedMap.class.getName(), 
        "<Long, ", EntityInternal.class.getName(), "<",  
        classDecl.getQualifiedName(), getAnyTypeParameters(classDecl, TypeExprType.NAME), 
        ">> typedEntities) throws ",
        UniqueConstraintViolationException.class.getName(), " {");
    writer.writeLine(
        ++indent, "for (", EntityInternal.class.getName(), "<", 
        classDecl.getQualifiedName(), getAnyTypeParameters(classDecl, TypeExprType.NAME), 
        "> entity : typedEntities.values()) {");
    indent++;
    Set<FieldDeclaration> storableFields = 
        CodeGenHelper.getStorableFields(classDecl);
    boolean anyUniqueConstraint = false;
    for (FieldDeclaration fieldDecl : storableFields) {
      if (hasUniqueConstraint(fieldDecl)) {
        anyUniqueConstraint = true;
        writer.writeLine(indent, "if (", fieldDecl.getSimpleName(), 
            ".equals(entity.getObject().",
            getGetter(classDecl, fieldDecl).getSimpleName(), "())) {");
        writer.writeLine(
            ++indent, "throw new ", 
            UniqueConstraintViolationException.class.getName(), "(");
        writer.writeLine(
            ++indent, "\"", fieldDecl.getSimpleName(), " \" + ", 
            fieldDecl.getSimpleName(), " + \" ", "already exists\");");
        indent--;
        writer.writeLine(--indent, "}");
      }
    }
    // Ensure that we are not looping if there are no constraints
    if (!anyUniqueConstraint) {
      writer.writeLine(indent, "break;");
    }
    writer.writeLine(--indent, "}");
    writer.writeLine(--indent, "}", NEW_LINE);

  }

  private static boolean hasUniqueConstraint(FieldDeclaration fieldDecl) {
    Field fieldAnnotation = fieldDecl.getAnnotation(Field.class);
    if (fieldAnnotation == null) {
      return false;
    }
    return fieldAnnotation.unique();
  }

  public static void addUpdateMethod(
      ClassDeclaration classDecl, ClassWriter writer) {
    int indent = getMethodHeaderIndentation();
    writer.writeLine(indent, "@Override");
    writer.writeLine(
        indent, "public void update(", 
        getClassName(Json.Object.class), " jsonObj) {");
    indent++;
    if (isStorable(classDecl)) {
      writer.writeLine(indent, "if (jsonObj.containsKey(\"version\")) {");
      writer.writeLine(++indent, "version = jsonObj.getInt(\"version\");");
      writer.writeLine(--indent, "}");
    }
    //    writer.writeLine(++indent, "super.update(jsonObj);");
    Set<FieldDeclaration> storableFields = 
        CodeGenHelper.getStorableFields(classDecl);
    for (FieldDeclaration fieldDecl : storableFields) {
      if (fieldDecl.getModifiers().contains(Modifier.FINAL)) {
        continue;
      }
      MethodDeclaration getter = null;
      if (!isFieldAccessible(fieldDecl, classDecl)) {
        getter = getGetter(classDecl, fieldDecl);
        if (getter == null) {
          throw new IllegalArgumentException(
              "Field " + fieldDecl.toString() + " must either be non-private or "
                  + "have a corresponding getter method");
        }
      }
      fieldDecl.getType().accept(new AddUpdateCallVisitor(
          fieldDecl, getter, writer, indent));
    }
    writer.writeLine(--indent, "}", NEW_LINE);
  }

  // T EntityInternal.getObject()
  static void addGetObjectMethod(
      ClassDeclaration classDecl, ClassWriter writer) {
    int indent = getMethodHeaderIndentation();
    writer.writeLine(indent, "@Override");
    writer.writeLine(
        indent, "public ", classDecl.getQualifiedName(), 
        getAnyTypeParameters(classDecl, TypeExprType.NAME), " getObject() {");
    writer.writeLine(++indent, "return this;");
    writer.writeLine(--indent, "}", NEW_LINE);
  }

  // Class<T> EntityInternal.getType()
  static void addGetKlassMethod(
      ClassDeclaration classDecl, ClassWriter writer) {
    int indent = getMethodHeaderIndentation();
    writer.writeLine(indent, "@SuppressWarnings(\"unchecked\")");
    writer.writeLine(indent, "@Override");
    writer.writeLine(
        indent, "public Class<", classDecl.getQualifiedName(),
        getAnyTypeParameters(classDecl, TypeExprType.NAME), 
        "> getKlass() {");
    writer.writeLine(
        ++indent, "return (Class<", classDecl.getQualifiedName() + 
        getAnyTypeParameters(classDecl, TypeExprType.NAME), 
        ">) super.getClass();");
    writer.writeLine(--indent, "}", NEW_LINE);
  }

  static void addEntityCopyMethod(
      ClassDeclaration proxiedClass, ClassWriter writer) {
    int indent = getMethodHeaderIndentation();
    writer.writeLine(indent, "@Override");
    writer.writeLine(
        indent, "public ", EntityInternal.class.getName(), "<", 
        proxiedClass.getQualifiedName(), getAnyTypeParameters(proxiedClass, TypeExprType.NAME), 
        "> entityCopy(", Json.Object.class.getName().replace("$", "."), " updateSpec) {");
    writer.writeLine(
        ++indent, "return new " + getClassName(
            proxiedClass, GenType.PROXY, ClassNameType.QUALIFIED), 
            getAnyTypeParameters(proxiedClass, TypeExprType.NAME),
        "(updateSpec);");
    writer.writeLine(--indent, "}", NEW_LINE);
  }

  static void addGetJsonTypeMethod(
      ClassDeclaration classDecl, ClassWriter writer) {
    int indent = getMethodHeaderIndentation();
    writer.writeLine(indent, "@Override");
    writer.writeLine(
        indent, "public ", getClassName(JsonType.class), " getJsonType() {");
    writer.writeLine(
        ++indent, "return ", getClassName(JsonType.class), ".", 
        classDecl.getSimpleName(), ";");
    writer.writeLine(--indent, "}", NEW_LINE);
  }

  // TODO: Possibly use addClassHeader method instead
  //       and remove this
  private static void addMetaModelHeader(
      TypeDeclaration decl, ClassWriter writer, int indent) {
    String classSimpleName = getClassName(
        decl, GenType.META_MODEL, ClassNameType.SIMPLE);
    String classQualifiedName = getClassName(
        decl, GenType.META_MODEL, ClassNameType.QUALIFIED);
    writer.setQualifiedName(classQualifiedName);
    writer.writeLine(
        indent, "package ", decl.getPackage().getQualifiedName(), 
        ";", NEW_LINE);
    writer.writeLine(
        indent, "public class ", classSimpleName, 
        " implements ", MetaModel.class.getName(), "<", 
        decl.getQualifiedName(), "> {", 
        NEW_LINE);
    indent++;
    writer.writeLine(
        indent, "public static final ", classSimpleName, " META_MODEL = new ",
        classQualifiedName, "();", NEW_LINE);
  }

  private static String getFieldName(MethodDeclaration method) {
    String fieldName = null; 
    Field field = method.getAnnotation(Field.class);
    if (field != null) {
      fieldName = field.name();
    } else {
      String methodName = method.getSimpleName();
      if (methodName.startsWith("get")) {
        fieldName = 
            Character.toLowerCase(methodName.charAt(3)) 
            + methodName.substring(4, methodName.length()); 
        // TODO: Add check that method returns boolean 
      } else if (methodName.startsWith("is")) {
        fieldName = 
            Character.toLowerCase(methodName.charAt(2)) 
            + methodName.substring(3, methodName.length()); 
      }
    }
    return fieldName;
  }
  
  public static void addMetaModel(
      InterfaceDeclaration ifDecl, ClassWriter writer) {
    int indent = 0;
    addMetaModelHeader(ifDecl, writer, indent++);
    Collection<? extends MethodDeclaration> methods = ifDecl.getMethods();
    addMetaModelProxyClass(ifDecl, writer, methods, indent);
    for (MethodDeclaration method : methods) {
      String fieldName = getFieldName(method); 
      if (fieldName == null) {
        continue;
      }
      method.getReturnType().accept(
          new MetaModelVisitor(method, fieldName, writer, indent));
    }
    writer.writeLine(--indent, "}", NEW_LINE);
  }

  private static void addMetaModelProxyClassPrologue(
      TypeDeclaration typeDecl, ClassWriter writer, int indent) {
    writer.writeLine(
        indent++, "public static class Proxy extends ", 
        getClassName(typeDecl, GenType.META_MODEL, ClassNameType.QUALIFIED),
        " {", NEW_LINE);
  }
  
  private static void addMetaModelProxyClassEpilogue(
      ClassWriter writer, int indent) {
    writer.writeLine(indent);
    writer.writeLine(indent, "public Proxy() {}");
    writer.writeLine(--indent, "}", NEW_LINE);
  }
  
  private static void addMetaModelProxyClass(
      ClassDeclaration classDecl, ClassWriter writer, 
      Set<FieldDeclaration> storableFields, int indent) {
    addMetaModelProxyClassPrologue(classDecl, writer, indent);
    for (FieldDeclaration fieldDecl : storableFields) {
      fieldDecl.getType().accept(new MetaModelProxyVisitor(
          fieldDecl, getFieldName(fieldDecl), writer, indent));
    }
    addMetaModelProxyClassEpilogue(writer, indent);
  }

  private static void addMetaModelProxyClass(
      InterfaceDeclaration interfaceDecl, ClassWriter writer, 
      Collection<? extends MethodDeclaration> methods, int indent) {
    addMetaModelProxyClassPrologue(interfaceDecl, writer, indent);
    for (MethodDeclaration method : methods) {
      String fieldName = getFieldName(method); 
      if (fieldName == null) {
        continue;
      }
      method.getReturnType().accept(
          new MetaModelProxyVisitor(method, fieldName, writer, indent));
    }
    addMetaModelProxyClassEpilogue(writer, indent);
  }

  public static void addMetaModel(
      ClassDeclaration classDecl, ClassWriter writer) {
    int indent = 0;
    addMetaModelHeader(classDecl, writer, indent++);
    Set<FieldDeclaration> storableFields = CodeGenHelper.getStorableFields(classDecl);
    addMetaModelProxyClass(classDecl, writer, storableFields, indent);
    for (FieldDeclaration fieldDecl : storableFields) {
      fieldDecl.getType().accept(new MetaModelVisitor(
          fieldDecl, getFieldName(fieldDecl), writer, indent));
    }
    writer.writeLine(--indent, "}", NEW_LINE);
  }

  public static void addCommonFields(
      ClassDeclaration proxiedClass, ClassWriter writer) {
    int indent = getMethodHeaderIndentation();
    writer.writeLine(indent, "protected long id;");
    writer.writeLine(indent, "protected int version;");
    writer.writeLine(
        indent, Set.class.getName(), "<? extends ", 
        getClassName(EntityInternal.class), "<?>> referers;", NEW_LINE);
  }

  static void addCommonFieldAccessors(
      ClassDeclaration proxiedClass, ClassWriter writer) {
    int indent = getMethodHeaderIndentation();
    writer.writeLine(indent, "@Override");
    writer.writeLine(indent, "public long getId() { return id; }", NEW_LINE);
    writer.writeLine(indent, "@Override");
    writer.writeLine(indent, "public void setId(long id) { this.id = id; }", NEW_LINE);
    writer.writeLine(indent, "@Override");
    writer.writeLine(indent, "public int getVersion() { return version; }", NEW_LINE);
    writer.writeLine(indent, "@Override");
    writer.writeLine(indent, "public void setVersion(int version) { this.version = version; }", NEW_LINE);
    writer.writeLine(indent, "@Override");
    writer.writeLine(
        indent, "public ", getClassName(Set.class), "<? extends ", 
        getClassName(EntityInternal.class), 
        "<?>> getReferers() { return referers; }", NEW_LINE);
  }

  static void addGetEntitiesMethod(
      ClassDeclaration classDecl, ClassWriter writer) {
    int indent = getMethodHeaderIndentation();
    writer.writeLine(indent, "@Override");
    writer.writeLine(
        indent, "public ", getClassName(Set.class), "<? extends ", 
        getClassName(EntityInternal.class), "<?>> getEntities() {");
    writer.writeLine(++indent, "// TODO: Collections of EntityInternals");
    writer.writeLine(
        indent, getClassName(Set.class), "<", 
        getClassName(EntityInternal.class), "<?>> entities = new ",
        getClassName(HashSet.class), "<", getClassName(EntityInternal.class),
        "<?>>();");
    Set<FieldDeclaration> storableFields = CodeGenHelper.getStorableFields(classDecl);
    for (FieldDeclaration field : storableFields) {
      // TODO: Use a visitor instead to get the field class
      if (isStorable(field)) {
        writer.writeLine(
            indent, "if (", getGetExpression(classDecl, field), 
            " != null && ", getGetExpression(classDecl, field), 
            " instanceof ", getClassName(EntityInternal.class), ") {");
        writer.writeLine(
            ++indent, "entities.add((", getClassName(EntityInternal.class), 
            "<?>) ", getGetExpression(classDecl, field), ");");
        writer.writeLine(--indent, "}");
      }
    }
    writer.writeLine(indent, "return entities;");
    writer.writeLine(--indent, "}", NEW_LINE);
  }

  static void addConstructors(
      ClassDeclaration proxiedClass, ClassWriter writer) {
    addDelegatedConstructor(proxiedClass, writer);
    Set<FieldDeclaration> storableFields = CodeGenHelper.getStorableFields(proxiedClass);
    addCopyConstructorIdAndVersion(proxiedClass, writer, storableFields);
    addCopyConstructorJson(proxiedClass, writer, storableFields);
    addJsonConstructor(proxiedClass, writer);
  }

  static void addJsonConstructor(
      ClassDeclaration proxiedClass, ClassWriter writer) {
    Set<FieldDeclaration> storableFields = getStorableFields(proxiedClass);
    int indent = getMethodHeaderIndentation();
    writer.writeLine(indent, "public ", getClassName(
        proxiedClass, GenType.PROXY, ClassNameType.SIMPLE), "(", 
        Json.Object.class.getName().replace("$", "."), " fields) {");

    ConstructorDeclaration constructor = getMainConstructor(proxiedClass);

    addCallToSuperConstructorFromJsonConstructor(
        proxiedClass, writer, indent, constructor, 
        storableFields);

    indent++;

    if (isStorable(proxiedClass)) {
      writer.writeLine(indent, "id = fields.getInt(\"id\");");
      writer.writeLine(indent, "version = fields.getInt(\"version\");");
      writer.writeLine(
          indent, "referers = factory.newEntitySet(", 
          getClassName(EntityInternal.class), ".class, ",
          "fields.getObject(\"referers\"));");
    }

    Collection<ParameterDeclaration> constructorParams = 
        constructor.getParameters();
    for (final FieldDeclaration fieldDecl : storableFields) {
      if (!inParameters(constructorParams, fieldDecl.getSimpleName())) {
        if (fieldDecl.getModifiers().contains(Modifier.FINAL)) {
          System.out.println("WARNING: Final field " + fieldDecl.getSimpleName()
              + " neither used in delegated constructor call nor in field assignment");
        } else {
          ClassWriter prefixWriter = new ClassWriter();
          if (!isFieldAccessible(fieldDecl, proxiedClass)) {
            prefixWriter.write(
                0, getSetter(proxiedClass, fieldDecl).getSimpleName(), 
                "(");
            fieldDecl.getType().accept(
                new AddJsonConstructorVisitor(
                    fieldDecl, writer, indent, prefixWriter.toString(), ");"));
          } else {
            prefixWriter.write(
                0, "this.", fieldDecl.getSimpleName(), " = ");
            fieldDecl.getType().accept(
                new AddJsonConstructorVisitor(
                    fieldDecl, writer, indent, prefixWriter.toString(), ";"));
          }
        }
      }
    }
    writer.writeLine(--indent, "}", NEW_LINE);
  }

  private static void addCallToSuperConstructorFromJsonConstructor(
      ClassDeclaration proxiedClass, ClassWriter writer, 
      int indentation, ConstructorDeclaration constructor,
      Set<FieldDeclaration> storableFields) {
    writer.writeLine(++indentation, "super(");
    indentation++;
    int numParams = constructor.getParameters().size();
    int i = 0;
    boolean lastParam = false;
    Collection<ParameterDeclaration> constructorParams = 
        constructor.getParameters();
    for (ParameterDeclaration pd : constructorParams) {
      i++;
      String fieldName = pd.getSimpleName();
      String attribute = null;
      if (pd.getAnnotation(FieldRef.class) != null) {
        fieldName = pd.getAnnotation(FieldRef.class).field();
        attribute = pd.getAnnotation(FieldRef.class).attribute();
      } 
      FieldDeclaration fieldDeclaration = getField(storableFields, fieldName);
      if (fieldDeclaration == null) {
        throw new RuntimeException(
            "No field named " + fieldName + " in " 
                + proxiedClass.getQualifiedName());
      }
      if (i == numParams) {
        lastParam = true;
      }
      fieldDeclaration.getType().accept(
          new JsonConstructorSuperCallVisitor(
              proxiedClass, fieldDeclaration.getSimpleName(), attribute, 
              writer, lastParam, indentation));
    }
    writer.writeLine(--indentation, ");");
  }

  static FieldDeclaration getField(
      Collection<FieldDeclaration> fields, String name) {
    for (FieldDeclaration field : fields) {
      if (field.getSimpleName().equals(name)) {
        return field;
      }
    }
    return null;
  }

  static MethodDeclaration getMethod(
      Collection<? extends MethodDeclaration> methods, String name) {
    for (MethodDeclaration method : methods) {
      if (method.getSimpleName().equals(name)) {
        return method;
      }
    }
    return null;
  }

  static String getGetterName(String attribute) {
    return "get" + Character.toUpperCase(attribute.charAt(0))
        + attribute.substring(1, attribute.length());
  }

  private static void addCopyConstructorIdAndVersion(
      final ClassDeclaration proxiedClass, ClassWriter writer, 
      Set<FieldDeclaration> storableFields) {
    int indent = getMethodHeaderIndentation();
    writer.writeLine(
        indent, "public ", 
        getClassName(proxiedClass, GenType.PROXY, ClassNameType.SIMPLE), "(", 
        proxiedClass.getQualifiedName(), getAnyTypeParameters(proxiedClass, TypeExprType.NAME),
        " other, long id, int version) {");
    addCopyConstructorCommon(proxiedClass, writer, storableFields, indent);
    writer.writeLine(indent, "this.id = id;");
    writer.writeLine(indent, "this.version = version;");
    writer.writeLine(
        indent, "referers = new ", HashSet.class.getName(), "<",
        EntityInternal.class.getName(), "<?>>();");
    writer.writeLine(--indent, "}", NEW_LINE);
  }

  private static void addCopyConstructorJson(
      final ClassDeclaration proxiedClass, ClassWriter writer, 
      Set<FieldDeclaration> storableFields) {
    int indent = getMethodHeaderIndentation();
    writer.writeLine(indent, "public ", getClassName(
        proxiedClass, GenType.PROXY, ClassNameType.SIMPLE), "(", 
        getClassName(proxiedClass, GenType.PROXY, ClassNameType.QUALIFIED),
        getAnyTypeParameters(proxiedClass, TypeExprType.NAME),
        " other, ", Json.Object.class.getName().replace("$", "."), 
        " updateSpec) {");
    addCopyConstructorCommon(proxiedClass, writer, storableFields, indent);
    writer.writeLine(indent, "id = other.getId();");
    writer.writeLine(indent, "version = other.getVersion();");
    writer.writeLine(indent, "referers = other.getReferers();");
    writer.writeLine(--indent, "}", NEW_LINE);
  }

  private static void addCopyConstructorCommon(
      final ClassDeclaration proxiedClass, ClassWriter writer, 
      Set<FieldDeclaration> storableFields, int indent) {      
    ConstructorDeclaration constructor = getMainConstructor(proxiedClass);
    addCallToSuperConstructorFromCopyConstructor(
        proxiedClass, writer, indent, constructor, 
        storableFields);

    indent++;
    Collection<ParameterDeclaration> constructorParams = 
        constructor.getParameters();
    // Add (non-final) storable fields not set through constructor
    for (FieldDeclaration fieldDecl : storableFields) {
      if (!inParameters(constructorParams, fieldDecl.getSimpleName())) {
        if (fieldDecl.getModifiers().contains(Modifier.FINAL)) {
          System.out.println("WARNING: Final field " + fieldDecl.getSimpleName()
              + " neither used in delegated constructor call nor in field assignment");
        } else {
          if (!isFieldAccessible(fieldDecl, proxiedClass, false)
              && isFieldAccessible(fieldDecl, proxiedClass)) {
            writer.writeLine(
                indent, "this.", fieldDecl.getSimpleName(), " = other.",
                getGetter(proxiedClass, fieldDecl).getSimpleName(), "();");
          } else if (isFieldAccessible(fieldDecl, proxiedClass)) {
            writer.writeLine(
                indent, "this.", fieldDecl.getSimpleName(), " = other.",
                fieldDecl.getSimpleName(), ";");
          } else {
            System.err.println(
                "Could not assign field " + fieldDecl.getSimpleName()
                + " in copy constructor.");
          }
        }
      }
    }
  }
  
  private static void addCallToSuperConstructorFromCopyConstructor(
      ClassDeclaration proxiedClass, ClassWriter writer, 
      int indent, ConstructorDeclaration constructor,
      Set<FieldDeclaration> storableFields) {
    writer.writeLine(++indent, "super(");
    indent++;
    int numParams = constructor.getParameters().size();
    int i = 0;
    boolean lastParam = false;
    Collection<ParameterDeclaration> constructorParams = 
        constructor.getParameters();
    for (ParameterDeclaration pd : constructorParams) {
      i++;
      if (i == numParams) {
        lastParam = true;
      }
      String fieldName = pd.getSimpleName();
      if (pd.getAnnotation(FieldRef.class) != null) {
        fieldName = pd.getAnnotation(FieldRef.class).field();
        final String attribute = pd.getAnnotation(FieldRef.class).attribute();
        FieldDeclaration fieldDecl = getField(storableFields, fieldName);
        if (fieldDecl == null) {
          throw new RuntimeException(
              "No field named " + fieldName + " in " 
                  + proxiedClass.getQualifiedName());
        }
        if (!isFieldAccessible(fieldDecl, proxiedClass, false)) {
          fieldName = getGetter(proxiedClass, fieldDecl).getSimpleName() + "()";
        }
        fieldDecl.getType().accept(
            new AddCopyConstructorVisitor(
                proxiedClass, writer.getStringBuilder(), fieldName, attribute, 
                indent, lastParam));
      } else {
        FieldDeclaration fieldDecl = getField(storableFields, fieldName);
        if (!isFieldAccessible(fieldDecl, proxiedClass, false)) {
          fieldName = getGetter(proxiedClass, fieldDecl).getSimpleName() + "()";
        }
        writer.writeLine(indent, "other.", fieldName, lastParam ? "" : ",");
      }
    }
    writer.writeLine(--indent, ");");
  }

  static boolean isFieldAccessible(
      FieldDeclaration fieldDecl, TypeDeclaration fromTypeDecl) {
    return isFieldAccessible(fieldDecl, fromTypeDecl, true);
  }
  
  static boolean isFieldAccessible(
      FieldDeclaration fieldDecl, TypeDeclaration fromTypeDecl,
      boolean includeProtected) {
    return (
        fieldDecl.getModifiers().contains(Modifier.PUBLIC)
        || (includeProtected 
            && fieldDecl.getModifiers().contains(Modifier.PROTECTED))
        || (!fieldDecl.getModifiers().contains(Modifier.PRIVATE)
            && (fieldDecl.getDeclaringType().getPackage()
                == fromTypeDecl.getPackage())));
  }

  private static ConstructorDeclaration getMainConstructor(
      ClassDeclaration proxiedClass) {
    Collection<ConstructorDeclaration> constructors = proxiedClass.getConstructors();
    ConstructorDeclaration constructor = constructors.iterator().next();
    for (ConstructorDeclaration c : constructors) {
      if (c.getParameters().size() > constructor.getParameters().size()) {
        constructor = c;
      }
    }
    return constructor;
  }

  private static boolean inParameters(
      Collection<ParameterDeclaration> params, String name) {
    for (ParameterDeclaration param : params) {
      if (param.getSimpleName().equals(name) 
          || ((param.getAnnotation(FieldRef.class) != null)
              && param.getAnnotation(FieldRef.class).field().equals(name))) {
        return true;
      }
    }
    return false;
  }

  static void addDelegatedConstructor(
      ClassDeclaration decl, ClassWriter writer) {
    int indent = getMethodHeaderIndentation();
    for (ConstructorDeclaration constructor : decl.getConstructors()) {
      writer.write(
          indent, "public ", getClassName(
              decl, GenType.PROXY, ClassNameType.SIMPLE), 
          "(");
      String comma = "";
      for (ParameterDeclaration param : constructor.getParameters()) {
        writer.write(
            0, comma, param.getType().toString(), " ", param.getSimpleName());
        comma = ", ";
      }
      if (isStorable(decl)) {
        writer.write(0, comma, "long id, int version");
      }
      writer.writeLine(0, ") {");
      writer.write(++indent, "super(");
      comma = "";
      for (ParameterDeclaration param : constructor.getParameters()) {
        writer.write(0, comma, param.getSimpleName());
        comma = ", ";
      }
      writer.writeLine(0, ");");
      if (isStorable(decl)) {
        writer.writeLine(indent, "this.id = id;");
        writer.writeLine(indent, "this.version = version;");
        writer.writeLine(
            indent, "referers = new ",
            getClassName(HashSet.class), "<", getClassName(EntityInternal.class),
            "<?>>();");

      }
      writer.writeLine(--indent, "}", NEW_LINE);
    }
  }

  public static void addMethodModifiers(Collection<Modifier> methodModifiers,
      StringBuilder classDefinition) {
    for (Modifier methodModifier : methodModifiers) {
      classDefinition.append(methodModifier.toString()).append(" ");
    }
  }

  public static void addReturnType(MethodDeclaration method,
      StringBuilder classDefinition) {
    classDefinition.append(method.getReturnType().toString()).append(" ");
  }

  public static void addMethodName(MethodDeclaration method,
      StringBuilder classDefinition) {
    classDefinition.append(method.getSimpleName()).append("(");
  }

  public static void addMethodArgumentSection(MethodDeclaration method,
      StringBuilder classDefinition) {
    CodeGenHelper.addMethodParameters(method, classDefinition);
    classDefinition.append(") {").append(Constants.NEW_LINE);
  }

  public static void addMethodParameters(MethodDeclaration method,
      StringBuilder classDefinition) {
    addMethodParameters(method, false, classDefinition);
  }

  public static void addMethodParameters(MethodDeclaration method,
      boolean onlyName,
      StringBuilder classDefinition) {
    String separator = "";
    for (ParameterDeclaration parameter : method.getParameters()) {
      classDefinition.append(separator).append(onlyName ? parameter.getSimpleName() : parameter.toString());
      separator = ", ";
    }
  }

  private static Collection<FieldDeclaration> getAllFields(
      ClassDeclaration classDeclaration, boolean includeInherited) {
    Collection<FieldDeclaration> fields = classDeclaration.getFields();
    /*
    Collection<FieldDeclaration> annotatedFields = new ArrayList<FieldDeclaration>(fields.size());
    for (FieldDeclaration fieldDecl : fields) {
      if (fieldDecl.getAnnotation(Field.class) != null) {
        annotatedFields.add(fieldDecl);
      }
    }
     */
    if (includeInherited) {
      ClassDeclaration superClass = classDeclaration.getSuperclass().getDeclaration();
      if (!superClass.toString().equals(Object.class.getName())) {
        fields.addAll(getAllFields(superClass, true));
      }
    }
    return fields;
  }

  private static FieldDeclaration getProperty(ClassDeclaration clazz,
      MethodDeclaration method) {
    String methodName = method.getSimpleName();
    if (methodName.startsWith("set") || methodName.startsWith("get")) {
      String fieldName = methodName.substring(3, methodName.length());
      fieldName = fieldName.substring(0, 1).toLowerCase() + fieldName.substring(1, fieldName.length());
      for (FieldDeclaration field : getAllFields(clazz, true)) {
        if (field.getSimpleName().equals(fieldName)) {
          return field;
        }
      }
    }
    return null;
  }

  public static void addSaveFieldsMethod(ClassDeclaration classDeclaration,
      StringBuilder classDefinition,
      Set<String> saveOfCheckpointedFields) {
    int indentationLevel = getMethodHeaderIndentation();
    indent(indentationLevel, classDefinition);
    classDefinition.append("private void saveFields() {").append(Constants.NEW_LINE);
    indent(++indentationLevel, classDefinition);
    classDefinition.append("synchronized (syncObj) {").append(Constants.NEW_LINE);
    indent(++indentationLevel, classDefinition);
    classDefinition.append("if (!saved) {").append(Constants.NEW_LINE);
    indentationLevel++;
    // Use previously generated section where all fields
    // are saved (indentical between methods)
    for (String saveLine : saveOfCheckpointedFields) {
      indent(indentationLevel, classDefinition);
      classDefinition.append(saveLine);
    }
    indent(--indentationLevel, classDefinition);
    classDefinition.append("}").append(Constants.NEW_LINE);
    indent(--indentationLevel, classDefinition);
    classDefinition.append("}").append(Constants.NEW_LINE);
    indent(--indentationLevel, classDefinition);
    classDefinition.append("}").append(Constants.NEW_LINE).append(Constants.NEW_LINE);
  }

  private static void addSaveFieldsCallSection(
      StringBuilder classDefinition,
      int indentation) {
    classDefinition.append("if (").append("checkpointingHandler.isCheckpointInProgress()) {")
    .append(Constants.NEW_LINE);
    indent(++indentation, classDefinition);
    classDefinition.append("saveFields();").append(Constants.NEW_LINE);
    indent(--indentation, classDefinition);
    classDefinition.append("}").append(Constants.NEW_LINE);

  }

  public static void addDelegatedMethodCall(ClassDeclaration clazz,
      MethodDeclaration method,
      CodeGenHelper.ProxyStrategy proxyStrategy,
      StringBuilder classDefinition,
      Set<String> saveOfCheckpointedFields) {
    int indentationLevel = getMethodHeaderIndentation();
    indent(++indentationLevel, classDefinition);
    FieldDeclaration property = getProperty(clazz, method);
    if (property != null) {
      // TODO: Make check for setter method less ugly (more correct)
      if (!CodeGenCommon.isTransient(property) && method.getSimpleName().startsWith("set")) {
        addSaveFieldsCallSection(classDefinition, indentationLevel);
        indent(indentationLevel, classDefinition);
      }
    }
    if (!method.getReturnType().toString().equals("void")) {
      classDefinition.append("return ");
    }
    classDefinition.append(CodeGenHelper.getProxiedObject(clazz, proxyStrategy)).append(".")
    .append(method.getSimpleName()).append("(");
    CodeGenHelper.addMethodParameters(method, true, classDefinition);
    classDefinition.append(");").append(Constants.NEW_LINE);
  }

  public static void addFieldIndentation(StringBuilder classDefinition) {
    classDefinition.append("   ");
  }

  public static void addMethodHeaderIndentation(StringBuilder classDefinition) {
    classDefinition.append("   ");
  }

  public static void addMethodBodyIndentation(StringBuilder classDefinition) {
    classDefinition.append("      ");
  }

  public static int getMethodHeaderIndentation() {
    return 1;
  }

  public static int getMethodBodyIndentation() {
    return 2;
  }

  public static void addMethodEnd(StringBuilder classDefinition) {
    addMethodHeaderIndentation(classDefinition);
    classDefinition.append("}").append(Constants.NEW_LINE).append(Constants.NEW_LINE);
  }

  public static void addClassEnd(ClassWriter writer) {
    writer.writeLine(0, "}");
  }

  public static void addToString(ClassDeclaration proxiedClass,
      StringBuilder classDefinition) {
    addMethodHeaderIndentation(classDefinition);
    classDefinition.append("public String toString() {").append(Constants.NEW_LINE);
    addMethodBodyIndentation(classDefinition);
    classDefinition.append("StringBuilder contents = new StringBuilder();").append(Constants.NEW_LINE);
    addMethodBodyIndentation(classDefinition);
    classDefinition.append("contents.append(this.getClass().getName())").append(".append(\" {\").append(")
    .append(Constants.class.getName()).append(".newLine);").append(Constants.NEW_LINE);
    addMethodBodyIndentation(classDefinition);
    classDefinition.append("contents.append(\"   Internal id: \")").append(".append(this.__internalId).append(")
    .append(Constants.class.getName()).append(".newLine);").append(Constants.NEW_LINE);
    for (FieldDeclaration field : getAllFields(proxiedClass, true)) {
      if (isProperty(proxiedClass, field)) {
        addMethodBodyIndentation(classDefinition);
        // Just output internal id if field is another checkpointable
        if (isCheckpointable(field)) {
          classDefinition.append("if (super.").append(getGetExpression(proxiedClass, field)).append(" == null) {")
          .append(Constants.NEW_LINE);
          indent(3, classDefinition);
          classDefinition.append("contents.append(\"").append(ClassWriter.INDENTATION_UNIT).append(field.getSimpleName())
          .append(": null\").append(").append(Constants.class.getName()).append(".newLine);")
          .append(Constants.NEW_LINE);
          indent(2, classDefinition);
          classDefinition.append("}").append(Constants.NEW_LINE);
          indent(2, classDefinition);
          classDefinition.append("else {").append(Constants.NEW_LINE);
          indent(3, classDefinition);
          classDefinition.append("contents.append(\"").append(ClassWriter.INDENTATION_UNIT).append(field.getSimpleName())
          .append(": [Checkpointable internal id = \").append(((")
          .append(EntityInternal.class.getName()).append(") super.")
          .append(getGetExpression(proxiedClass, field));
          classDefinition.append(").get__InternalId()").append(").append(\"]\"");
          classDefinition.append(").append(").append(Constants.class.getName()).append(".newLine);")
          .append(Constants.NEW_LINE);
          indent(2, classDefinition);
          classDefinition.append("}").append(Constants.NEW_LINE);
        } else {
          classDefinition.append("contents.append(\"");
          indent(1, classDefinition);
          classDefinition.append(field.getSimpleName()).append(": \").append(super.")
          .append(getGetExpression(proxiedClass, field));
          classDefinition.append(").append(").append(Constants.class.getName()).append(".newLine);")
          .append(Constants.NEW_LINE);
        }
      }
    }
    addMethodBodyIndentation(classDefinition);
    classDefinition.append("contents.append(\"}\").append(").append(Constants.class.getName()).append(".newLine);")
    .append(Constants.NEW_LINE);
    addMethodBodyIndentation(classDefinition);
    classDefinition.append("return contents.toString();").append(Constants.NEW_LINE);
    addMethodHeaderIndentation(classDefinition);
    classDefinition.append("}").append(Constants.NEW_LINE).append(Constants.NEW_LINE);
  }

  private static boolean isPrimitiveType(String type) {
    return primitiveTypes.contains(type);
  }

  private static boolean isWrapperType(String type) {
    return wrapperTypes.contains(type);
  }

  private static boolean isStringType(String type) {
    return "String".equals(type);
  }

  static String getFieldType(FieldDeclaration field,
      ClassNameType classNameType) {
    Types types = env.getTypeUtils();
    String fieldQualifiedType = types.getErasure(field.getType()).toString();
    if (isPrimitiveType(fieldQualifiedType)) {
      return fieldQualifiedType;
    }
    if (classNameType.equals(ClassNameType.QUALIFIED)) {
      if (CodeGenCommon.isInstanceOfInnerClass(field)) {
        int lastDot = fieldQualifiedType.lastIndexOf(".");
        String simpleTypeName = fieldQualifiedType.substring(lastDot + 1, fieldQualifiedType.length());
        String pakkage = fieldQualifiedType.substring(0, lastDot);
        return pakkage + "$" + simpleTypeName;
      }
      return fieldQualifiedType;
    }
    int lastDot = fieldQualifiedType.lastIndexOf(".");
    return fieldQualifiedType.substring(lastDot + 1, fieldQualifiedType.length());
  }

  private static boolean hasCheckpointableAnnotation(String className) {
    Class fieldClass = CodeGenCommon.getClass(className);
    for (Annotation annotation : fieldClass.getAnnotations()) {
      if (annotation.annotationType().equals(Storable.class)) {
        return true;
      }
    }
    return false;
  }

  // TODO: Implement using method below
  private static boolean implementsEntityInternal(String className) {
    Class clazz = CodeGenCommon.getClass(className);
    return EntityInternal.class.isAssignableFrom(clazz);
  }

  static boolean implementsInterface(
      TypeDeclaration typeDecl, Class<?> interfejs) {
    Collection<InterfaceType> interfaces = typeDecl.getSuperinterfaces();
    for (InterfaceType type : interfaces) {
      if (type.toString().equals(interfejs.getName())) {
        return true;
      } 
    }
    for (InterfaceType type : interfaces) {
      if (implementsInterface(type.getDeclaration(), interfejs)) {
        return true;
      }
    }
    return false;    
  }

  static boolean isSuperClass(
      TypeDeclaration maybeSuper, ClassDeclaration type) {
    if (maybeSuper.equals(type)) {
      return true;
    }
    if (type.getSuperclass() == null) {
      return false;
    }
    if (type.getSuperclass().getDeclaration().equals(maybeSuper)) {
      return true;
    } else {
      return isSuperClass(maybeSuper, type.getSuperclass().getDeclaration());
    }
  }
  // TODO: Ugly - Use class comparison when ClassNotFound problems fixed
  static boolean isString(TypeDeclaration typeDecl) {
    return (typeDecl.getQualifiedName().equals("java.lang.String"));
  }

  static boolean isEnum(ClassDeclaration classDecl) {
    if (classDecl.getQualifiedName().equals("java.lang.Enum")) {
      return true;
    }
    com.sun.mirror.type.ClassType superType = classDecl.getSuperclass();
    while (!superType.toString().equals("java.lang.Object")) {
      if (superType.toString().startsWith("java.lang.Enum")) {
        return true;
      }
      superType = superType.getSuperclass();
    }
    return false;
  }

  private static boolean isCheckpointable(FieldDeclaration field) {
    if (isArray(getFieldType(field, ClassNameType.QUALIFIED))) {
      return false;
    }
    if (!isReferenceObject(field)) {
      return false;
    }
    String fieldType = getFieldType(field, ClassNameType.QUALIFIED);
    return hasCheckpointableAnnotation(fieldType) 
        || implementsEntityInternal(fieldType);
  }

  private static final boolean hasGetter(ClassDeclaration clazz, FieldDeclaration field) {
    return (getGetter(clazz, field) != null);
  }

  private static final boolean hasSetter(ClassDeclaration clazz, FieldDeclaration field) {
    return (getSetter(clazz, field) != null);
  }

  private static boolean isStorableField(ClassDeclaration clazz,
      FieldDeclaration field) {
    if (CodeGenCommon.isStatic(field) 
        || CodeGenCommon.isTransient(field) 
        || CodeGenCommon.isNative(field)) {
      return false;
    }
    return true;
  }

  private static boolean isArray(String className) {
    return className.endsWith("[]");
  }

  private static String getArrayType(FieldDeclaration field) {
    String fieldType = getFieldType(field, ClassNameType.QUALIFIED);
    int fieldTypeLength = fieldType.length();
    return fieldType.substring(0, fieldTypeLength - 2);
  }

  private static boolean isReferenceObject(FieldDeclaration field) {
    String fieldType = getFieldType(field, ClassNameType.SIMPLE);
    return (!isStringType(fieldType) && !isPrimitiveType(fieldType) && !isWrapperType(fieldType));
  }

  private static final Set<String> primitiveTypes = initializePrimitiveTypes();

  private static final Set<String> initializePrimitiveTypes() {
    Set<String> primitiveTypes = new HashSet<String>();
    primitiveTypes.add("byte");
    primitiveTypes.add("short");
    primitiveTypes.add("int");
    primitiveTypes.add("long");
    primitiveTypes.add("float");
    primitiveTypes.add("double");
    primitiveTypes.add("boolean");
    primitiveTypes.add("char");
    return Collections.unmodifiableSet(primitiveTypes);
  }

  private static final Set<String> wrapperTypes = initializeWrapperTypes();

  private static final Set<String> initializeWrapperTypes() {
    Set<String> wrapperTypes = new HashSet<String>();
    wrapperTypes.add("Byte");
    wrapperTypes.add("Short");
    wrapperTypes.add("Integer");
    wrapperTypes.add("Long");
    wrapperTypes.add("Float");
    wrapperTypes.add("Double");
    wrapperTypes.add("Boolean");
    wrapperTypes.add("Character");
    return Collections.unmodifiableSet(wrapperTypes);
  }

  private static final Set<Modifier> checkpointableFieldModifiers = initializeCheckpointableFieldModifiers();

  private static final Set<Modifier> initializeCheckpointableFieldModifiers() {
    Set<Modifier> modifiers = new HashSet<Modifier>();
    modifiers.add(Modifier.PRIVATE);
    modifiers.add(Modifier.PROTECTED);
    modifiers.add(Modifier.VOLATILE);
    return Collections.unmodifiableSet(modifiers);
  }
}
