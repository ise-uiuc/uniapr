/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */

package org.uniapr.jvm.core.reflection;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import org.uniapr.jvm.core.JVMStatus;


/**
 * Handled reflection cases:
 * java.lang.reflect.Field.get(Object obj)
 * java.lang.reflect.Field.set(Object obj,Object obj)
 * java.lang.reflect.Method.invoke(Object obj, Object... args) 
 * java.lang.reflect.Constructor.newInstance(Object... initargs)
 */
class ReflectionRewriterMethodVisitor extends MethodVisitor implements Opcodes
{

	String slashClazzName;


	public ReflectionRewriterMethodVisitor(final MethodVisitor mv, 
			String clazzName) {
		super(ASM5, mv);
		this.slashClazzName = clazzName;
	}

	// trace the method invocations
	@Override
	public void visitCode() {
		mv.visitVarInsn(ALOAD, 0);
		mv.visitFieldInsn(GETFIELD, slashClazzName, "clazz", "Ljava/lang/Class;");
		mv.visitMethodInsn(INVOKESTATIC, JVMStatus.CLASS, JVMStatus.REFLOCKCHECK,
				"(Ljava/lang/Class;)V", false);
		super.visitCode();
	}

	public void visitMaxs(int maxStack, int maxLocals) {
		mv.visitMaxs(maxStack + 1, maxLocals);
	}

	public boolean isVirtual(int access) {
		return 0 == (access & Opcodes.ACC_STATIC);
	}

}