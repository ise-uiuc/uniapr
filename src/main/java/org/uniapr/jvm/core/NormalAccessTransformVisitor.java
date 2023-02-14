/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */

package org.uniapr.jvm.core;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import org.uniapr.jvm.agent.JVMClinitClassTransformer;

public class NormalAccessTransformVisitor extends ClassVisitor
		implements Opcodes
{
	String slashClazzName;
	boolean clinit = false;
	boolean isInterface = false;
	int id;

	public NormalAccessTransformVisitor(String clazzName, final ClassVisitor cv,
			boolean isInterface, int id) {
		super(ASM5, cv);
		this.slashClazzName = clazzName;
		this.isInterface = isInterface;
		this.id = id;
		// set the prapr_gen tag to avoid reinstrumentation
		cv.visitSource(JVMStatus.PRAPR_GEN, JVMStatus.PRAPR_GEN);
	}

	@Override
	public void visitSource(String s1, String debug) {
		cv.visitSource(JVMStatus.PRAPR_GEN + "-" + s1, JVMStatus.PRAPR_GEN);
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name,
			final String desc, final String signature,
			final String[] exceptions) {
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature,
				exceptions);
		boolean isClinit = name.startsWith("<clinit>");

		if (isClinit) {
			clinit = true;
			if (!isInterface) {
				if (JVMClinitClassTransformer.leakingClasses.keySet()
						.contains(slashClazzName)) {
					addNewClinit(mv, access, name, desc, signature, exceptions);
					mv = cv.visitMethod(access + Opcodes.ACC_PUBLIC,
							JVMStatus.RECLINIT, desc, signature, exceptions);
					mv = mv == null ? null
							: new ClinitRewriterMethodVisitor(mv,
									slashClazzName, id);
				}
			}
		}
		return mv == null ? null
				: new AccessRewriterMethodVisitor(mv, slashClazzName);
	}

	@Override
	public FieldVisitor visitField(int access, String name, String descriptor,
			String signature, Object value) {
		if (isSafe(access, value) || !JVMClinitClassTransformer.leakingClasses
				.keySet().contains(slashClazzName)) {
			return cv.visitField(access, name, descriptor, signature, value);
		} else if (!isInterface) {
			if ((access & ACC_FINAL) != 0) {
				return cv.visitField(access - ACC_FINAL, name, descriptor,
						signature, value);
			} else {
				return cv.visitField(access, name, descriptor, signature,
						value);
			}
		} else {
			return cv.visitField(access, name, descriptor, signature, value);
		}
	}

	public boolean isSafe(int access, Object value) {
		if ((access & ACC_STATIC) == 0)
			return true;
		if ((access & Opcodes.ACC_FINAL) != 0 && value != null) {
			return true;
		}
		return false;
	}

	public void addNewClinit(MethodVisitor mv, int access, String name,
			String desc, String signature, String[] exceptions) {
		mv.visitCode();
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, slashClazzName,
				JVMStatus.RECLINIT, desc, false);
		mv.visitInsn(RETURN);
		mv.visitEnd();
	}

}
