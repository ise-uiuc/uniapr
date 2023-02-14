/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr.commons.process;

import org.pitest.util.ExitCode;
import org.pitest.util.SafeDataOutputStream;

import java.io.OutputStream;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public abstract class AbstractReporter {
    protected final SafeDataOutputStream dos;

    protected AbstractReporter(OutputStream os) {
        this.dos = new SafeDataOutputStream(os);
    }

    public synchronized void done(final ExitCode exitCode) {
        this.dos.writeByte(ControlId.DONE);
        this.dos.writeInt(exitCode.getCode());
        this.dos.flush();
    }
}
