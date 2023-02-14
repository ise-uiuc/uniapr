/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr.validator;

import org.uniapr.Patch;
import org.uniapr.PatchId;
import org.uniapr.ValidationStatus;
import org.uniapr.ValidationStatusTable;
import org.uniapr.commons.process.ControlId;
import org.pitest.functional.SideEffect1;
import org.pitest.util.CommunicationThread;
import org.pitest.util.ReceiveStrategy;
import org.pitest.util.SafeDataInputStream;
import org.pitest.util.SafeDataOutputStream;

import java.net.ServerSocket;
import java.util.Collection;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
class ValidatorCommunicationThread extends CommunicationThread {
    private final DataReceiver receiver;

    public ValidatorCommunicationThread(final ServerSocket socket,
                                        final ValidatorArguments arguments) {
        this(socket, new DataSender(arguments), new DataReceiver());
    }

    public ValidatorCommunicationThread(final ServerSocket socket,
                                        final DataSender sender,
                                        final DataReceiver receiver) {
        super(socket,sender, receiver);
        this.receiver = receiver;
    }

    public void checkInPatches(final Collection<Patch> patches) {
        this.receiver.checkInPatches(patches);
    }

    public ValidationStatusTable getStatusTable() {
        return this.receiver.statusTable;
    }

    protected static class DataSender implements SideEffect1<SafeDataOutputStream> {
        private final ValidatorArguments arguments;

        DataSender(ValidatorArguments arguments) {
            this.arguments = arguments;
        }

        @Override
        public void apply(final SafeDataOutputStream dos) {
            dos.write(this.arguments);
        }
    }

    protected static class DataReceiver implements ReceiveStrategy {
        final ValidationStatusTable statusTable;

        public DataReceiver() {
            this.statusTable = new ValidationStatusTable();
        }

        @Override
        public void apply(final byte control, final SafeDataInputStream dis) {
            final PatchId id = dis.read(PatchId.class);
            switch (control) {
                case ControlId.DESCRIBE:
                    this.statusTable.put(id, ValidationStatus.STARTED);
                    break;
                case ControlId.REPORT:
                    final ValidationStatus status = dis.read(ValidationStatus.class);
                    this.statusTable.put(id, status);
            }
        }

        public void checkInPatches(final Collection<Patch> patches) {
            for (final Patch patch : patches) {
                this.statusTable.put(patch.getId(), ValidationStatus.NOT_STARTED);
            }
        }
    }
}
