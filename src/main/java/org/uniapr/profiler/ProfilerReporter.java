/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr.profiler;

import org.uniapr.commons.process.AbstractReporter;
import org.uniapr.commons.process.ControlId;

import java.io.OutputStream;
import java.util.Collection;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
class ProfilerReporter extends AbstractReporter {
    public ProfilerReporter(OutputStream os) {
        super(os);
    }

    public synchronized void reportTestTime(final String testName, final long timeElapsed) {
        this.dos.writeByte(ControlId.REPORT);
        this.dos.writeString(testName);
        this.dos.writeLong(timeElapsed);
        this.dos.flush();
    }

    public synchronized void reportFailingTestNames(final Collection<String> failingTestNames) {
        this.dos.writeByte(ControlId.DESCRIBE);
        this.dos.write(failingTestNames.toArray(new String[0]));
    }
}
