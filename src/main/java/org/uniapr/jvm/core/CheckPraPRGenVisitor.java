/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */

package org.uniapr.jvm.core;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public class CheckPraPRGenVisitor extends ClassVisitor implements Opcodes
{
	public boolean isPraPRGen=false;

	public CheckPraPRGenVisitor() {
		super(ASM5);
	}
	@Override
	public void visitSource(String source,
            String debug){
		if(source.startsWith(JVMStatus.PRAPR_GEN))
			isPraPRGen= true;
	}
}
