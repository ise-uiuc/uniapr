/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr.profiler;

import org.pitest.process.ProcessArgs;
import org.pitest.process.WrappingProcess;
import org.pitest.util.ExitCode;
import org.pitest.util.SocketFinder;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.Map;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
class ProfilerProcess {
    private final WrappingProcess process;

    private final ProfilerCommunicationThread communicationThread;

    public ProfilerProcess(final ProcessArgs processArgs,
                           final ProfilerArguments arguments) {
        this((new SocketFinder()).getNextAvailableServerSocket(), processArgs, arguments);
    }

    private ProfilerProcess(final ServerSocket socket,
                            final ProcessArgs processArgs,
                            final ProfilerArguments arguments) {
        this.process = new WrappingProcess(socket.getLocalPort(), processArgs, ProfilerDriver.class);
        this.communicationThread = new ProfilerCommunicationThread(socket, arguments);
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

    public Map<String, Long> getTestsTiming() {
        return this.communicationThread.getTestsTiming();
    }

    public String[] getFailingTestNames() {
        return this.communicationThread.getFailingTestNames();
    }
}
