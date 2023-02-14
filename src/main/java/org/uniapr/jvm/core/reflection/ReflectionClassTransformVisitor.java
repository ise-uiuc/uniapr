/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */

package org.uniapr.jvm.core.reflection;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import org.uniapr.jvm.core.JVMStatus;

/**
 * Handled reflection cases: java.lang.reflect.Field.get(Object obj)
 * java.lang.reflect.Field.set(Object obj,Object obj)
 * java.lang.reflect.Method.invoke(Object obj, Object... args)
 * java.lang.reflect.Constructor.newInstance(Object... initargs)
 */
public class ReflectionClassTransformVisitor extends ClassVisitor
		implements Opcodes
{
	String slashClazzName;
	boolean clinit = false;
	boolean isInterface = false;
	public final static Map<String, Set<String>> reflectionSites = new HashMap<String, Set<String>>();
	static {
		reflectionSites.put("java/lang/reflect/Field",
				new HashSet<>(Arrays.asList("set", "get")));
		reflectionSites.put("java/lang/reflect/Method",
				new HashSet<>(Arrays.asList("invoke")));
		reflectionSites.put("java/lang/reflect/Constructor",
				new HashSet<>(Arrays.asList("newInstance")));
	}

	public ReflectionClassTransformVisitor(String clazzName,
			final ClassVisitor cv, boolean isInterface) {
		super(ASM5, cv);
		this.slashClazzName = clazzName;
		this.isInterface = isInterface;
		// set the prapr_gen tag to avoid reinstrumentation
		cv.visitSource(JVMStatus.PRAPR_GEN, JVMStatus.PRAPR_GEN);
	}
	
	@Override
	public void visitSource(String s1, String debug){
		cv.visitSource(JVMStatus.PRAPR_GEN+"-"+s1, JVMStatus.PRAPR_GEN);
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name,
			final String desc, final String signature,
			final String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature,
				exceptions);
		if (reflectionSites.get(slashClazzName).contains(name))
			return mv == null ? null
					: new ReflectionRewriterMethodVisitor(mv,
							slashClazzName);
		else
			return mv;
	}
}
