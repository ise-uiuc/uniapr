/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */

package org.uniapr.jvm.agent;

import java.io.*;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;

import org.uniapr.jvm.core.CheckPraPRGenVisitor;
import org.uniapr.jvm.core.JVMStatus;
import org.uniapr.jvm.core.NormalAccessTransformVisitor;
import org.uniapr.jvm.core.reflection.ReflectionClassTransformVisitor;
import org.uniapr.jvm.offline.LeakingFieldMain;

public class JVMClinitClassTransformer implements ClassFileTransformer
{
	private boolean debugMode;
	public static Map<String, Boolean> leakingClasses;
	public static Map<String, String> classInheritance;
	public static Map<String, List<String>> leakingClassMap;
	static {
		try {
			leakingClasses = LeakingFieldMain
					.deSerializeClasses(LeakingFieldMain.CLASSLOG);
			classInheritance=LeakingFieldMain.deSerializeInheritance(LeakingFieldMain.INHERILOG);
			leakingClassMap=prepareLeakingClassMap();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public JVMClinitClassTransformer(boolean debugMode) {
		this.debugMode = debugMode;
	}

	/**
	 * prepare a map from each class to its ancestors that may contain pollution sites
	 */
	public static Map<String, List<String>> prepareLeakingClassMap() {
		Map<String, List<String>> map=new HashMap<String, List<String>>();
		for (String clazz : classInheritance.keySet()) {
			List<String> ancestors = new ArrayList<String>();
			String current=clazz;
			while(classInheritance.containsKey(current)){
				current=classInheritance.get(current);
				if (leakingClasses.containsKey(current))
					ancestors.add(0,current);
			}
			if (ancestors.size() > 0)
				map.put(clazz, ancestors);
		}
		return map;
	}


	public byte[] transform(ClassLoader loader, String slashClassName,
			Class<?> classBeingRedefined, ProtectionDomain protectionDomain,
			byte[] classfileBuffer) throws IllegalClassFormatException {
		try {
			if (slashClassName == null/*||classBeingRedefined!=null*/) {
				return classfileBuffer;
			}

			if (!ReflectionClassTransformVisitor.reflectionSites
					.containsKey(slashClassName)) {
				if (JVMStatus.isExcluded(slashClassName)) {
					return classfileBuffer;
				}
				if (loader != ClassLoader.getSystemClassLoader()) {
					return classfileBuffer;
				}
			}
			byte[] result = classfileBuffer;
			ClassReader reader = new ClassReader(classfileBuffer);
			
			// avoid retransforming files that have been transformed by PraPR
			if(isPraPRGen(result)) {
				return classfileBuffer;
			}

			ClassWriter writer = new ClassWriter(reader, 0);
			ClassVisitor cv = null;
			

			if (ReflectionClassTransformVisitor.reflectionSites
					.containsKey(slashClassName)) {
				//JVMStatus.registerClass(slashClassName);
				cv = new ReflectionClassTransformVisitor(slashClassName, writer, isInterface(reader));
			} else {
			    int id=JVMStatus.registerClass(slashClassName);
				cv = new NormalAccessTransformVisitor(slashClassName, writer,
						isInterface(reader), id);
			}
			reader.accept(cv, 0);
			result = writer.toByteArray();

			if (this.debugMode){
				write(System.getProperty("user.home") + "/javaagent/uniapr/JVMClinitClassTransformer/"
						+ slashClassName.replace('/', '.') + ".class", result);
			}

			return result;
		} catch (Throwable t) {
			t.printStackTrace();
			String message = "Exception thrown during instrumentation";
			System.err.println(message);
			System.exit(1);
		}
		throw new RuntimeException("Should not be reached");
	}
	
	protected boolean isPraPRGen(byte[] result){
		ClassReader reader = new ClassReader(result);
		CheckPraPRGenVisitor visitor=new CheckPraPRGenVisitor();
		reader.accept(visitor, 0);
		return visitor.isPraPRGen;
	}

	public boolean isInterface(ClassReader cr) {
		return ((cr.getAccess() & Opcodes.ACC_INTERFACE) != 0);
	}
	
	public static boolean shouldTransformRegardingInterface(String clazz){
		return !leakingClasses.get(clazz);
	}

	public static void write(final String path, final byte[] bytes) {
		try{
			File file = new File(path);
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}
			Files.write(Paths.get(path), bytes);
		} catch (Throwable t) {
			t.printStackTrace();
		}
	}
}
