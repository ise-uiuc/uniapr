/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr.validator;

import org.uniapr.PatchId;
import org.uniapr.ValidationStatus;
import org.uniapr.commons.process.AbstractReporter;
import org.uniapr.commons.process.ControlId;

import java.io.OutputStream;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
class ValidatorReporter extends AbstractReporter {
    public ValidatorReporter(OutputStream os) {
        super(os);
    }

    public synchronized void reportStatus(final PatchId id, final ValidationStatus status) {
        this.dos.writeByte(ControlId.REPORT);
        this.dos.write(id);
        this.dos.write(status);
        this.dos.flush();
    }

    public synchronized void notifyStarted(final PatchId id) {
        this.dos.writeByte(ControlId.DESCRIBE);
        this.dos.write(id);
        this.dos.flush();
    }
}
