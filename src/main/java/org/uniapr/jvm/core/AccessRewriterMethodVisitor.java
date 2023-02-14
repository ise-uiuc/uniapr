/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */

package org.uniapr.jvm.core;

import java.util.List;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import org.uniapr.jvm.agent.JVMClinitClassTransformer;

class AccessRewriterMethodVisitor extends MethodVisitor implements Opcodes
{

	String slashClazzName;

	public AccessRewriterMethodVisitor(final MethodVisitor mv,
			String clazzName) {
		super(ASM5, mv);
		this.slashClazzName = clazzName;
	}

	/**
	 * Add prapr_clinit invocation to possible method invocations that may
	 * trigger the original <clinit> invocation
	 */
	@Override
	public void visitMethodInsn(int opcode, String owner, String name,
			String desc, boolean itf) {
		if (opcode == Opcodes.INVOKESTATIC
				|| (opcode == Opcodes.INVOKESPECIAL && name.equals("<init>"))) {
			// reset the initializers for all the potential ancestors of owner
			// that could include pollution sites
			if (JVMClinitClassTransformer.leakingClassMap
					.containsKey(owner)) {
				List<String> ancestors = JVMClinitClassTransformer.leakingClassMap
						.get(owner);
				for (String ancestor : ancestors) {
					if (!ancestor.equals(slashClazzName)) {
						mv.visitMethodInsn(Opcodes.INVOKESTATIC, ancestor,
								JVMStatus.RECLINIT, "()V", false);
					}
				}
			}

			// reset the intializer for the owner itself
			if (JVMClinitClassTransformer.leakingClasses.keySet()
					.contains(owner) && (!owner.equals(slashClazzName)||owner.contains("Test"))// bug fix: the test classes may not have been reclinited; therefore, for here we allow tests to be reclinited by itself. TODO: better fixing via understand JUnit/testNG test class loading mechanism 
					&& JVMClinitClassTransformer.shouldTransformRegardingInterface(owner)
					&& !isSynthetic(name)) {
				//System.out.println(">>Static: "+owner+":"+name+" <from>"+slashClazzName+" "+line);
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, owner,
						JVMStatus.RECLINIT, "()V", false);
			}
		}
		mv.visitMethodInsn(opcode, owner, name, desc, itf);
	}

	/**
	 * Add prapr_clinit invocation to possible field accesses that may trigger
	 * the original <clinit> invocation
	 */
	@Override
	public void visitFieldInsn(int opcode, String owner, String name,
			String desc) {
		if (opcode == Opcodes.GETSTATIC || opcode == Opcodes.PUTSTATIC) {
			// reset the initializers for all the potential ancestors of owner
			// that could include pollution sites
			if (JVMClinitClassTransformer.leakingClassMap
					.containsKey(owner)) {
				List<String> ancestors = JVMClinitClassTransformer.leakingClassMap
						.get(owner);
				for (String ancestor : ancestors) {
					if (!ancestor.equals(slashClazzName)) {
						mv.visitMethodInsn(Opcodes.INVOKESTATIC, ancestor,
								JVMStatus.RECLINIT, "()V", false);
					}
				}
			}

			// reset the intializer for the owner itself
			if (JVMClinitClassTransformer.leakingClasses.keySet()
					.contains(owner)
					&&  JVMClinitClassTransformer.shouldTransformRegardingInterface(owner)
					&&  (!owner.equals(slashClazzName)||owner.contains("Test"))) {
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, owner,
						JVMStatus.RECLINIT, "()V", false);
			}
		}
		mv.visitFieldInsn(opcode, owner, name, desc);
	}

	public static String strip(String s) {
		return s.substring(1, s.length() - 1);
	}

	public void visitMaxs(int maxStack, int maxLocals) {
		mv.visitMaxs(maxStack + 4, maxLocals);
	}

	public boolean isVirtual(int access) {
		return 0 == (access & Opcodes.ACC_STATIC);
	}

	public boolean isSynthetic(String name) {
		return name.contains("$");
	}

}