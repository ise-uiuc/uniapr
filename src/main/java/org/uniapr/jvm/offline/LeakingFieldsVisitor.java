/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */

package org.uniapr.jvm.offline;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import org.uniapr.jvm.core.JVMStatus;

class LeakingFieldsVisitor extends ClassVisitor implements Opcodes
{
	String cName;
	boolean interfaze = false;
	boolean hasClinit = false;
	boolean isEnum = false;
	boolean isAbstract=false;
	boolean hasUnsafeField = false;
	String superClass;

	public LeakingFieldsVisitor() {
		super(ASM5);
	}

	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		this.cName = name;
		interfaze = isInterface(access);
		isEnum = isEnum(access);
		isAbstract=isAbstract(access);
		LeakingFieldMain.counter++;
		superClass = superName;
	}

	@Override
	public FieldVisitor visitField(int access, String name, String descriptor,
			String signature, Object value) {
		if (!isSafe(access, value)) {
			hasUnsafeField = true;// LeakingFieldMain.classes.put(cName,
									// interfaze);
		}
		return null;
	}

	@Override
	public MethodVisitor visitMethod(final int access, final String name,
			final String desc, final String signature,
			final String[] exceptions) {
		boolean isClinit = name.startsWith("<clinit>");
		if (isClinit)
			hasClinit = true;
		return null;
	}

	@Override
	public void visitEnd() {
		if (!interfaze)
			LeakingFieldMain.completeParentMap.put(cName, superClass);

		if (hasClinit && hasUnsafeField
				&& !JVMStatus.isExcluded(cName)) {
			if (!LeakingFieldMain.visitedAndSafe.contains(cName)) {
				LeakingFieldMain.classes.put(cName, interfaze);
			}
		} else {
			LeakingFieldMain.visitedAndSafe.add(cName);
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

	public static boolean isInterface(int access) {
		return ((access & Opcodes.ACC_INTERFACE) != 0);
	}
	
	public static boolean isAbstract(int access) {
		return ((access & Opcodes.ACC_ABSTRACT) != 0);
	}

	public static boolean isEnum(int access) {
		return ((access & Opcodes.ACC_ENUM) != 0);
	}
}
