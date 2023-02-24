/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */

package org.uniapr.agent;

import java.lang.instrument.ClassDefinition;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.jar.JarFile;
import java.util.logging.Logger;

public class HotSwapAgent
{

	private static Instrumentation instrumentation;

	public static void premain(final String agentArguments,
			final Instrumentation inst) {
		System.out.println("Installing UniAPR agent");
		instrumentation = inst;
		try {
			instrumentation.appendToBootstrapClassLoaderSearch(new JarFile(System.getProperty("user.home") + "/.m2/repository/org/uniapr/uniapr-plugin/1.1/uniapr-plugin-1.1.jar"));
		} catch (Throwable t){
			t.printStackTrace();
		}
	}

	/**
	 * Add the transformer (with canRetransform to be true), and also
	 * retransform the already loaded classes to handle reflections
	 * 
	 * @param transformer
	 */
	public static void addTransformer(final ClassFileTransformer transformer) {
		instrumentation.addTransformer(transformer, true);
		try {
			for (Class loadedClass : instrumentation.getAllLoadedClasses()) {
				String str = loadedClass.getName();
				if ((str.equals("java.lang.reflect.Field"))
						|| (str.equals("java.lang.reflect.Method"))
						|| (str.equals("java.lang.reflect.Constructor"))) {
					instrumentation.retransformClasses(loadedClass);
				}
			}
		} catch (UnmodifiableClassException localUnmodifiableClassException) {
		}
	}

	public static void agentmain(final String agentArguments,
			final Instrumentation inst) throws Exception {
		instrumentation = inst;
	}

	public static boolean hotSwap(final Class<?> mutateMe, final byte[] bytes) {

		final ClassDefinition[] definitions = {
				new ClassDefinition(mutateMe, bytes) };

		try {
			instrumentation.redefineClasses(definitions);

			return true;
		} catch (final ClassNotFoundException e) {
			// swallow
		} catch (final UnmodifiableClassException e) {
			// swallow
		} catch (final java.lang.VerifyError e) {
			// swallow
		} catch (final java.lang.InternalError e) {
			// swallow
		}
		return false;
	}

}
