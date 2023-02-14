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
import org.uniapr.agent.HotSwapAgent;
import org.uniapr.junit.runner.JUnitRunner;
import org.uniapr.jvm.agent.JVMClinitClassTransformer;
import org.uniapr.jvm.core.JVMStatus;

import org.pitest.classinfo.CachingByteArraySource;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classpath.ClassloaderByteArraySource;
import org.pitest.mutationtest.execute.MemoryWatchdog;
import org.pitest.process.ProcessArgs;
import org.pitest.util.ExitCode;
import org.pitest.util.IsolationUtils;
import org.pitest.util.SafeDataInputStream;

import javax.management.Notification;
import javax.management.NotificationListener;
import java.io.File;
import java.io.IOException;
import java.lang.management.MemoryNotificationInfo;
import java.net.Socket;
import java.util.Collection;
import java.util.Map;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class PatchValidator {
    private static final int CACHE_SIZE = 50;

    public static void main(String[] args) {
        System.out.println("Patch Validator is HERE!");
        final int port = Integer.parseInt(args[0]);
        try (Socket socket = new Socket("localhost", port)) {
            final ClassLoader loader = IsolationUtils.getContextClassLoader();

            ClassByteArraySource byteArraySource = new ClassloaderByteArraySource(loader);
            byteArraySource = new CachingByteArraySource(byteArraySource, CACHE_SIZE);

            final SafeDataInputStream dis = new SafeDataInputStream(socket.getInputStream());

            final ValidatorArguments arguments = dis.read(ValidatorArguments.class);

            final ValidatorReporter reporter = new ValidatorReporter(socket.getOutputStream());

            // add dynamic bytecode transformation to reset JVM status
            // provided that we need to reset JVM
            if (arguments.shouldResetJVM()) {
                System.out.println(">>Installing JVM ClInit Class Transformer...");
                HotSwapAgent.addTransformer(new JVMClinitClassTransformer(arguments.getDebug()));
            }

            addMemoryWatchDog(reporter);

            for (final Patch patch : arguments.getPatches()) {
            	System.out.println(">>Validating patchID: " + patch.getId().getId() +" (patch directory name: "+ patch.patchName+")");
                final JUnitRunner runner = new JUnitRunner(arguments.getAppClassNames(), arguments.getTestComparator(), arguments.getDebug(), true, arguments.getD4jAllTestsFile());
                final PatchId id = patch.getId();
                reporter.notifyStarted(id);
                // TODO: add test selection
                Long startTime = System.currentTimeMillis();
                try {
                    final int res = patch.test(byteArraySource,
                            loader,
                            runner,
                            arguments.getTestsTiming(),
                            arguments.getTimeoutBias(),
                            arguments.getTimeoutCoefficient());
                    final ValidationStatus status;
                    if (res == 1) {
                        status = ValidationStatus.PLAUSIBLE;
                    } else if (res == 0) {
                        status = ValidationStatus.NON_PLAUSIBLE;
                    } else if(res == -1){
                        status = ValidationStatus.TIMED_OUT;
                    } else {
                        status = ValidationStatus.INITIALIZER_ERROR;
                    }
                    reporter.reportStatus(id, status);
                    if (status == ValidationStatus.TIMED_OUT ||status == ValidationStatus.INITIALIZER_ERROR) {
                        break;
                    }
                } catch (Throwable t) {
                    t.printStackTrace();
                    reporter.reportStatus(id, ValidationStatus.RUN_ERROR);
                    break;
                } finally {
                    Long timeCost = System.currentTimeMillis() - startTime;
                    System.out.println(String.format("Time cost to validate patch %d (patch directory name: %s): %d ms",
                            patch.getId().getId(), patch.patchName, timeCost));
                }
                // reset the JVM status after each patch execution
                // provided that we need to reset JVM
                if (arguments.shouldResetJVM()) {
                    if (arguments.getDebug()){
                        System.out.println("Resetting Clinit...");
                    }
                    JVMStatus.resetClinit();
                }
            }
            System.out.println("Patch Validator is DONE!");
            reporter.done(ExitCode.OK);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Patch Validator is unable to establish connection!");
        }
    }

    // credit: adopted from PIT source code
    private static void addMemoryWatchDog(final ValidatorReporter reporter) {
        final NotificationListener listener = new NotificationListener() {
            @Override
            public void handleNotification(final Notification notification,
                                           final Object handback) {
                final String type = notification.getType();
                if (type.equals(MemoryNotificationInfo.MEMORY_THRESHOLD_EXCEEDED)) {
                    reporter.done(ExitCode.OUT_OF_MEMORY);
                }
            }
        };
        MemoryWatchdog.addWatchDogToAllPools(90, listener);
    }

    public static ValidationStatusTable testPatches(final ProcessArgs defaultProcessArgs,
                                                    final PraPRTestComparator testComparator,
                                                    final Collection<String> appClassNames,
                                                    final Collection<Patch> patches,
                                                    final Map<String, Long> methodsTiming,
                                                    final long timeoutBias,
                                                    final double timeoutCoefficient,
                                                    final boolean resetJVM,
                                                    final boolean resetInterface,
                                                    final boolean debug,
                                                    final File d4jAllTestsFile) {
        final ValidatorArguments arguments = new ValidatorArguments(testComparator, appClassNames,
                patches, methodsTiming, timeoutBias, timeoutCoefficient, resetJVM, resetInterface, debug, d4jAllTestsFile);
        final ValidatorProcess process = new ValidatorProcess(defaultProcessArgs, arguments);
        process.checkInPatches(patches);
        try {
            process.start();
        } catch (Exception e) {
            e.printStackTrace();
            throw new IllegalStateException();
        }
        final ExitCode exitCode = process.waitToDie();
        final ValidationStatusTable statusTable = process.getStatusTable();
        if (exitCode != ExitCode.OK) {
            final ValidationStatus status = ValidationStatus.forExitCode(exitCode);
            for (final Map.Entry<PatchId, ValidationStatus> entry : statusTable.entrySet()) {
                if (entry.getValue() == ValidationStatus.STARTED) {
                    entry.setValue(status);
                    break;
                }
            }
        }
        return statusTable;
    }
}
