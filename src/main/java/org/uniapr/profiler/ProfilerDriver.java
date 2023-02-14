/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr.profiler;

import org.uniapr.commons.misc.MemberNameUtils;
import org.uniapr.junit.runner.JUnitRunner;
import org.pitest.functional.predicate.Predicate;
import org.pitest.process.ProcessArgs;
import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.TestUnit;
import org.pitest.util.ExitCode;
import org.pitest.util.SafeDataInputStream;

import java.io.File;
import java.io.Serializable;
import java.net.Socket;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class ProfilerDriver {
    public static void main(String[] args) throws Exception {
        System.out.println("Profiler is HERE!");
        final int port = Integer.parseInt(args[0]);
        try (Socket socket = new Socket("localhost", port)) {
            final SafeDataInputStream dis = new SafeDataInputStream(socket.getInputStream());

            final ProfilerArguments arguments = dis.read(ProfilerArguments.class);

            final ProfilerReporter reporter = new ProfilerReporter(socket.getOutputStream());

            final JUnitRunner runner = new JUnitRunner(arguments.getAppClassNames(), arguments.getDebug(), false, arguments.getD4jAllTestsFile());
            runner.setTestUnits(decorateTestUnits(runner.getTestUnits(), reporter));
            runner.run();

            reporter.reportFailingTestNames(runner.getFailingTestNames());

            List<String> failedTests = runner.getFailingTestNames();
            if (failedTests.size() > 0){
                for (String failedTest: runner.getFailingTestNames()) {
                    System.out.println("Profiler failed test: " + failedTest);
                }
            } else {
                System.out.println("Profiler passed all tests!");
            }

            System.out.println("Profiler is DONE!");
            reporter.done(ExitCode.OK);
        }
    }

    private static List<TestUnit> decorateTestUnits(final List<TestUnit> testUnits,
                                                    final ProfilerReporter reporter) {
        final List<TestUnit> decoratedTests = new LinkedList<>();
        for (final TestUnit testUnit : testUnits) {
            decoratedTests.add(new TestUnit() {
                @Override
                public void execute(ResultCollector rc) {
                    final long start = System.currentTimeMillis();
                    testUnit.execute(rc);
                    final long elapsed = System.currentTimeMillis() - start;
                    final String testName =
                            MemberNameUtils.sanitizeExtendedTestName(testUnit.getDescription().getName());
                    reporter.reportTestTime(testName, elapsed);
                }

                @Override
                public Description getDescription() {
                    return testUnit.getDescription();
                }
            });
        }
        return decoratedTests;
    }

    public static ProfilerResults runProfiler(final ProcessArgs defaultProcessArgs,
                                              final List<String> appClassNames, boolean debug, File d4jAllTestsFile) {
        final ProfilerArguments arguments = new ProfilerArguments(appClassNames, debug, d4jAllTestsFile);
        final ProfilerProcess process = new ProfilerProcess(defaultProcessArgs, arguments);
        try {
            process.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        process.waitToDie();
        return new ProfilerResults() {
            @Override
            public Map<String, Long> getTestsTiming() {
                return process.getTestsTiming();
            }

            @Override
            public Predicate<String> getIsFailingTestPredicate() {
                final Set<String> failingTests = new HashSet<>();
                Collections.addAll(failingTests, process.getFailingTestNames());
                return new FailingTestPredicate(failingTests);
            }
        };
    }

    private static class FailingTestPredicate implements Predicate<String>, Serializable {
        private static final long serialVersionUID = 1L;

        private final Set<String> failingTests;

        public FailingTestPredicate(Set<String> failingTests) {
            this.failingTests = failingTests;
        }

        @Override
        public Boolean apply(String testName) {
            return failingTests.contains(testName);
        }
    }
}
