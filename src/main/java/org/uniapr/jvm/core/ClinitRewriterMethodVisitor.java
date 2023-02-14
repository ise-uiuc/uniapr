/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */

package org.uniapr.jvm.core;

import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

class ClinitRewriterMethodVisitor extends MethodVisitor implements Opcodes
{

	String slashClazzName;
	int id;

	public ClinitRewriterMethodVisitor(final MethodVisitor mv,
			String clazzName, int id) {
		super(ASM5, mv);
		this.slashClazzName = clazzName;
		this.id=id;
	}

	/**
	 * Implement the checked prapr_clinit: if prapr_clinit has been invoked, skip it
	 */
	@Override
	public void visitCode() {
		//mv.visitLdcInsn(slashClazzName);
		mv.visitLdcInsn(id);
		mv.visitMethodInsn(INVOKESTATIC, JVMStatus.CLASS, JVMStatus.LOCKCHECK,
				"(I)Z", false);
		Label l0 = new Label();
		mv.visitJumpInsn(IFEQ, l0);
		mv.visitInsn(RETURN);
		mv.visitLabel(l0);
		mv.visitFrame(F_FULL, 0, null, 0, null);
//		// for debugging
//		mv.visitFieldInsn(GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
//		mv.visitLdcInsn("Invoking " + slashClazzName + ".prapr_reclinit()");
//		mv.visitMethodInsn(INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V", false);
		super.visitCode();
	}

	public void visitMaxs(int maxStack, int maxLocals) {
		mv.visitMaxs(maxStack + 1, maxLocals);
	}

	public boolean isVirtual(int access) {
		return 0 == (access & Opcodes.ACC_STATIC);
	}

}