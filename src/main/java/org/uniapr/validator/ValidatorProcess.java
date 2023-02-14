/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr.validator;

import org.uniapr.Patch;
import org.uniapr.ValidationStatusTable;
import org.pitest.process.ProcessArgs;
import org.pitest.process.WrappingProcess;
import org.pitest.util.ExitCode;
import org.pitest.util.SocketFinder;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Collection;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
class ValidatorProcess {
    private final WrappingProcess process;

    private final ValidatorCommunicationThread communicationThread;

    public ValidatorProcess(final ProcessArgs processArgs,
                            final ValidatorArguments arguments) {
        this((new SocketFinder()).getNextAvailableServerSocket(), processArgs, arguments);
    }

    private ValidatorProcess(final ServerSocket socket,
                             final ProcessArgs processArgs,
                             final ValidatorArguments arguments) {
        this.process = new WrappingProcess(socket.getLocalPort(), processArgs, PatchValidator.class);
        this.communicationThread = new ValidatorCommunicationThread(socket, arguments);
    }

    public void start() throws IOException, InterruptedException {
        this.communicationThread.start();
        this.process.start();
    }

    public ExitCode waitToDie() {
        try {
            return this.communicationThread.waitToFinish();
        } finally {
            this.process.destroy();
        }
    }

    public void checkInPatches(final Collection<Patch> patches) {
        this.communicationThread.checkInPatches(patches);
    }

    public ValidationStatusTable getStatusTable() {
        return this.communicationThread.getStatusTable();
    }
}
