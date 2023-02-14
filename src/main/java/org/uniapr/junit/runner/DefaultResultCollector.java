/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr.junit.runner;

import org.uniapr.commons.misc.MemberNameUtils;
import org.uniapr.jvm.agent.JVMClinitClassTransformer;
import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;

import java.util.List;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
class DefaultResultCollector implements ResultCollector {
    private final List<String> failingTestNames;
    private Throwable throwable;
    public  boolean debugMode=false;

    public DefaultResultCollector(final List<String> failingTestNames) {
        this.failingTestNames = failingTestNames;
    }
    
    public DefaultResultCollector(final List<String> failingTestNames, boolean debugMode) {
        this.failingTestNames = failingTestNames;
        this.debugMode=debugMode;
    }

    @Override
    public void notifyEnd(Description description, Throwable t) {
        if (t != null) {
            final String failingTestName =
                    MemberNameUtils.sanitizeExtendedTestName(description.getName());
            this.failingTestNames.add(failingTestName);
            throwable=t;
            // print the detailed failure message in debug mode
			if (debugMode) {
				System.out.flush();
				System.err.println();
				t.printStackTrace();
				System.err.println();
				System.err.flush();
			}
        }
    }

    @Override
    public void notifyEnd(Description description) {
        // nothing
    }

    @Override
    public void notifyStart(Description description) {
        final String testName = MemberNameUtils.sanitizeExtendedTestName(description.getName());
        System.out.println("RUNNING: " + testName + "... ");
    }

    @Override
    public void notifySkipped(Description description) {
        final String testName = MemberNameUtils.sanitizeExtendedTestName(description.getName());
        System.out.println("SKIPPED: " + testName);
    }

    @Override
    public boolean shouldExit() {
        return false;
    }

	/**
	 * Bug fix: handle the case that test execution triggers
	 * ExceptionInInitializerError, since otherwise all future patches will
	 * suffer from NoClassDefFoundError
	 * 
	 * @return
	 */
	public boolean shouldRestart() {
		if (throwable instanceof ExceptionInInitializerError) {
			return true;
		}
		return false;
	}
}