/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr.profiler;

import org.uniapr.commons.process.ControlId;
import org.pitest.functional.SideEffect1;
import org.pitest.util.CommunicationThread;
import org.pitest.util.ReceiveStrategy;
import org.pitest.util.SafeDataInputStream;
import org.pitest.util.SafeDataOutputStream;

import java.net.ServerSocket;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
class ProfilerCommunicationThread extends CommunicationThread {
    private final DataReceiver receiver;

    public ProfilerCommunicationThread(final ServerSocket socket,
                                       final ProfilerArguments arguments) {
        this(socket, new DataSender(arguments), new DataReceiver());
    }

    public ProfilerCommunicationThread(final ServerSocket socket,
                                       final DataSender sender,
                                       final DataReceiver receiver) {
        super(socket,sender, receiver);
        this.receiver = receiver;
    }

    public  Map<String, Long> getTestsTiming() {
        return this.receiver.testsTiming;
    }

    public String[] getFailingTestNames() {
        return this.receiver.failingTestNames;
    }

    private static class DataSender implements SideEffect1<SafeDataOutputStream> {
        final ProfilerArguments arguments;

        public DataSender(final ProfilerArguments arguments) {
            this.arguments = arguments;
        }

        @Override
        public void apply(final SafeDataOutputStream dos) {
            dos.write(this.arguments);
        }
    }

    private static class DataReceiver implements ReceiveStrategy {
        final Map<String, Long> testsTiming;

        String[] failingTestNames;

        public DataReceiver() {
            this.testsTiming = new HashMap<>();
        }

        @Override
        public void apply(final byte control, final SafeDataInputStream dis) {
            switch (control) {
                case ControlId.REPORT:
                    final String testName = dis.readString();
                    final Long timeElapsed = dis.readLong();
                    this.testsTiming.put(testName, timeElapsed);
                    break;
                case ControlId.DESCRIBE:
                    this.failingTestNames = dis.read(String[].class);
            }
        }
    }
}
