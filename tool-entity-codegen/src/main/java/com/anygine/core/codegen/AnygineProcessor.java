package com.anygine.core.codegen;

import static com.anygine.core.codegen.CodeGenHelper.addCommonFieldAccessors;
import static com.anygine.core.codegen.CodeGenHelper.addEntityCopyMethod;
import static com.anygine.core.codegen.CodeGenHelper.addGetEntitiesMethod;
import static com.anygine.core.codegen.CodeGenHelper.addGetJsonTypeMethod;
import static com.anygine.core.codegen.CodeGenHelper.addGetObjectMethod;
import static com.anygine.core.codegen.CodeGenHelper.addGetKlassMethod;
import static com.anygine.core.codegen.CodeGenHelper.addUpdateMethod;
import static com.anygine.core.codegen.CodeGenHelper.addWriteJson2Params;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import com.anygine.core.codegen.CodeGenHelper.ClassNameType;
import com.anygine.core.codegen.CodeGenHelper.GenType;
import com.anygine.core.common.client.annotation.Embeddable;
import com.anygine.core.common.client.annotation.Storable;
import com.anygine.core.common.client.domain.impl.EntityHolder;
import com.anygine.core.common.codegen.api.EntityInternal;
import com.anygine.core.common.codegen.api.JsonWritableInternal;
import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.InterfaceDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;


public class AnygineProcessor implements AnnotationProcessor {

  Set<AnnotationTypeDeclaration> types;
  AnnotationProcessorEnvironment env;

  AnygineProcessor(Set<AnnotationTypeDeclaration> types,
      AnnotationProcessorEnvironment env) {
    this.types = types;
    this.env = env;
  }

  public void process() {
    System.out.println("In annotation processor!");    
    CodeGenHelper.env = this.env;

    List<String> failedClasses = new ArrayList<>();
    Collection<TypeDeclaration> types = env.getTypeDeclarations();
    for (TypeDeclaration decl : env.getTypeDeclarations()) {

      if ((decl.getAnnotation(Storable.class) == null
          && decl.getAnnotation(Embeddable.class) == null)
          || CodeGenCommon.isGenerated(decl)) {
        continue;
      }
      /*
        if (!decl.toString().equals("com.anygine.core.common.client.domain.impl.GunBase")) {
//        if (!decl.toString().contains("AmmoSupply")) {
          continue;
        }
       */      
      System.out.println("Processing class: " + decl.toString());

      try {
        if (decl.getAnnotation(Storable.class) != null) {
          generateStorable(decl);
        } else {
          generateEmbeddable(decl);
        }
      } catch (Throwable t) {
        t.printStackTrace();
        failedClasses.add(decl.toString());
        // Try to process remaining classes ...
      }
    }
    if (!failedClasses.isEmpty()) {
      throw new RuntimeException(
          "Code generation failed for the following classes: " 
              + Arrays.toString(failedClasses.toArray()));
    }
  }

  private void generateStorable(TypeDeclaration decl) throws IOException {
    StringBuilder classDefinition = new StringBuilder(4096);
    String newName = CodeGenHelper.getClassName(
        decl, GenType.PROXY, ClassNameType.QUALIFIED);
    PrintWriter newClass = env.getFiler().createSourceFile(newName);

    if (decl instanceof ClassDeclaration) {

      if (CodeGenCommon.isFinal(decl) || CodeGenCommon.isAbstract(decl)) {
        return;
      }

      ClassDeclaration classDecl = (ClassDeclaration) decl;
      // If class already implements EntityInternal, we are dealing with a custom, hand-coded
      // entity class, in which case we need and should do nothing.
      if (CodeGenCommon.implementsInterface(
          classDecl, EntityInternal.class)) {
        return;
      }

      ClassWriter writer = new ClassWriter(classDefinition);

      CodeGenHelper.addPackage(classDecl, GenType.PROXY, writer);
      CodeGenHelper.addClassHeader(classDecl, GenType.PROXY, writer);

      ClassWriter metaModelWriter = new ClassWriter(env.getFiler(), new StringBuilder(4096));
      CodeGenHelper.addMetaModel(classDecl, metaModelWriter);
      metaModelWriter.writeFile();

      CodeGenHelper.addFactoriesAndServices(classDecl, writer);
      CodeGenHelper.addCommonFields(classDecl, writer);
      CodeGenHelper.addConstructors(classDecl, writer);

      // BEGIN: JsonWritableInternal methods

      addGetJsonTypeMethod(classDecl, writer);
      addWriteJson2Params(classDecl, writer);
      addUpdateMethod(classDecl, writer);

      // END: JsonWritableInternal methods

      addCommonFieldAccessors(classDecl, writer);
      addGetEntitiesMethod(classDecl, writer);
      addEntityCopyMethod(classDecl, writer);
      addGetObjectMethod(classDecl, writer);
      addGetKlassMethod(classDecl, writer);

      //          CodeGenHelper.addWriteJson3ParamsMethod(classDeclaration, classWriter);
      CodeGenHelper.addWrite3ParamsMethod(classDecl, writer);

      CodeGenHelper.addConstraintsMethod(classDecl, classDefinition);
      CodeGenHelper.addCompareToMethod(classDecl, classDefinition);
      CodeGenHelper.addEqualsMethod(classDecl, classDefinition);

      CodeGenHelper.add_writeMethod(classDecl, writer);
      CodeGenHelper.add_writeJsonMethod(classDecl, writer);
      CodeGenHelper.add_writeJsonHeaderMethod(classDecl, writer);
      CodeGenHelper.add_writeJsonFooterMethod(classDecl, writer);

    } else {
      InterfaceDeclaration ifDecl = (InterfaceDeclaration) decl;
      CodeGenHelper.addPackage(
          ifDecl, GenType.PROXY, new ClassWriter(classDefinition));
      CodeGenHelper.addInterfaceHeader(
          ifDecl, GenType.PROXY, new ClassWriter(classDefinition));

      ClassWriter classWriter = new ClassWriter(env.getFiler(), new StringBuilder(4096));
      CodeGenHelper.addMetaModel(ifDecl, classWriter);
      classWriter.writeFile();
    }
    CodeGenHelper.addClassEnd(new ClassWriter(classDefinition));
    newClass.write(classDefinition.toString());
    newClass.flush();
  }

  private void generateEmbeddable(TypeDeclaration decl) {

    ClassWriter writer = new ClassWriter(env.getFiler(), new StringBuilder(4096));

    if (decl instanceof ClassDeclaration) {

      if (CodeGenCommon.isFinal(decl) || CodeGenCommon.isAbstract(decl)) {
        return;
      }

      ClassDeclaration classDeclaration = (ClassDeclaration) decl;
      // If class already implements EntityInternal, we are dealing with a custom, hand-coded
      // checkpointable class, in which case we need and should do nothing.
      if (CodeGenCommon.implementsInterface(
          classDeclaration, JsonWritableInternal.class)) {
        return;
      }
      ClassDeclaration classDecl = (ClassDeclaration) decl;
      CodeGenHelper.addPackage(classDecl, GenType.PROXY, writer);
      CodeGenHelper.addClassHeader(classDeclaration, GenType.PROXY, writer);

      ClassWriter metaModelWriter = new ClassWriter(env.getFiler(), new StringBuilder(4096));
      CodeGenHelper.addMetaModel(classDeclaration, metaModelWriter);
      metaModelWriter.writeFile();

      CodeGenHelper.addFactoriesAndServices(classDecl, writer);

      CodeGenHelper.addDelegatedConstructor(classDecl, writer);
      CodeGenHelper.addJsonConstructor(classDecl, writer);

      // BEGIN: JsonWritableInternal methods

      CodeGenHelper.addGetJsonTypeMethod(classDecl, writer);

      CodeGenHelper.addWriteJson2Params(classDecl, writer);

      CodeGenHelper.addUpdateMethod(classDecl, writer);

      // END: JsonWritableInternal methods

      if (CodeGenHelper.implementsInterface(classDecl, EntityHolder.class)) {
        CodeGenHelper.addWrite2ParamsMethod(classDecl, writer);
        CodeGenHelper.add_writeMethod(classDecl, writer);
      } else {
        CodeGenHelper.add_writeJsonMethod(classDecl, writer);
      }
      CodeGenHelper.add_writeJsonHeaderMethod(classDecl, writer);
      CodeGenHelper.add_writeJsonFooterMethod(classDecl, writer);
    } else {
      InterfaceDeclaration interfaceDecl = (InterfaceDeclaration) decl;
      CodeGenHelper.addPackage(interfaceDecl, GenType.PROXY, writer);
      CodeGenHelper.addInterfaceHeader(interfaceDecl, GenType.PROXY, writer);

      ClassWriter metaModelWriter = new ClassWriter(env.getFiler(), new StringBuilder(4096));
      CodeGenHelper.addMetaModel(interfaceDecl, metaModelWriter);
      metaModelWriter.writeFile();
    }
    CodeGenHelper.addClassEnd(writer);
    writer.writeFile();
  }

}
