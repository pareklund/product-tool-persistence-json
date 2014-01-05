package com.anygine.core.codegen.inject;

public class CodeGenInjectorManager {

	private static CodeGenInjector injector;

	public static CodeGenInjector getInjector() {
		return injector;
	}

	public static void setInjector(CodeGenInjector injector) {
		CodeGenInjectorManager.injector = injector;
	}

}
