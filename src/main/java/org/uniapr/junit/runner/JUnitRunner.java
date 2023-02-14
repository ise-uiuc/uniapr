/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr.junit.runner;

import org.uniapr.commons.misc.MemberNameUtils;
import org.uniapr.validator.PraPRTestComparator;
import org.pitest.functional.predicate.Predicate;
import org.pitest.testapi.ResultCollector;
import org.pitest.testapi.TestUnit;
import org.uniapr.junit.JUnitUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class JUnitRunner
{
	private boolean debugMode;

	private static final ExecutorService EXECUTOR_SERVICE = Executors
			.newSingleThreadExecutor();

	private final List<String> failingTestNames;

	private List<TestUnit> testUnits;

	private final ResultCollector resultCollector;

	public JUnitRunner(final Collection<String> classNames, boolean debugMode,
			final boolean earlyExit, final File allTestsFile) {
		this.debugMode = debugMode;
		if (allTestsFile != null){
			this.testUnits = JUnitUtils.discoverTestUnitsByAllTestsFile(allTestsFile);
		} else {
			this.testUnits = JUnitUtils.discoverTestUnits(classNames);
		}
		this.failingTestNames = new ArrayList<>();
		ResultCollector collector = new DefaultResultCollector(
				this.failingTestNames, debugMode);
		if (earlyExit) {
			collector = new ExitingResultCollector(collector, debugMode);
		}
		this.resultCollector = collector;
	}

	public JUnitRunner(final Collection<String> classNames,
			final PraPRTestComparator comparator, boolean debugMode, final boolean earlyExit, final File allTestsFile) {
		this.debugMode = debugMode;
		if (allTestsFile != null){
			this.testUnits = JUnitUtils.discoverTestUnitsByAllTestsFile(allTestsFile);
		} else {
			this.testUnits = JUnitUtils.discoverTestUnits(classNames);
		}
		Collections.sort(this.testUnits, comparator);
		this.failingTestNames = new ArrayList<>();
		ResultCollector collector = new DefaultResultCollector(
				this.failingTestNames, debugMode);
		if (earlyExit) {
			collector = new ExitingResultCollector(collector, debugMode);
		}
		this.resultCollector = collector;
	}

	public List<String> getFailingTestNames() {
		return this.failingTestNames;
	}

	public List<TestUnit> getTestUnits() {
		return this.testUnits;
	}

	public void setTestUnits(List<TestUnit> testUnits) {
		this.testUnits = testUnits;
	}

	public int run() {
		return run(TestUnitFilter.all());
	}

	public int run(final Map<String, Long> testsTiming, final long timeoutBias,
			final double timeoutCoefficient) {
		return run(TestUnitFilter.all(), testsTiming, timeoutBias,
				timeoutCoefficient);
	}

	public int run(final Predicate<TestUnit> shouldRun) {
		for (final TestUnit testUnit : this.testUnits) {
			if (!shouldRun.apply(testUnit)) {
				continue;
			}
			testUnit.execute(this.resultCollector);
			if (this.resultCollector.shouldExit()) {
				System.out
						.println("WARNING: Running test cases is terminated.");
				return 0;
			}
		}
		return 1;
	}

	public int run(final Predicate<TestUnit> shouldRun,
			final Map<String, Long> testsTiming, final long timeoutBias,
			final double timeoutCoefficient) {
		for (final TestUnit testUnit : this.testUnits) {
			if (!shouldRun.apply(testUnit)) {
				continue;
			}
			final Runnable task = new Runnable() {
				@Override
				public void run() {
					testUnit.execute(JUnitRunner.this.resultCollector);
				}
			};
			try {
				final String testName = MemberNameUtils
						.sanitizeExtendedTestName(
								testUnit.getDescription().getName());
				final long timeoutThreshold = timeoutBias
						+ (long) (testsTiming.get(testName)
								* (1.D + timeoutCoefficient));

				EXECUTOR_SERVICE.submit(task).get(timeoutThreshold,
						TimeUnit.MILLISECONDS);
			} catch (TimeoutException e) {
				System.out.println(
						"WARNING: Running test cases is terminated due to TIME_OUT.");
				return -1;
			} catch (Exception e) {
				if (this.debugMode){
					e.printStackTrace();
				}
				System.out.println("WARNING: Running test cases is terminated due to unknown error.");
				return 0;
			}
			if (shouldRestart(resultCollector)) {
				System.out.println(
						"WARNING: Running test cases is terminated due to INITIALIZER_ERROR.");
				return -2;
			}
			if (this.resultCollector.shouldExit()) {
				System.out
						.println("WARNING: Running test cases is terminated due to test failure.");
				return 0;
			}
		}
		return 1;
	}

	public boolean shouldRestart(ResultCollector resultCollector) {
		if (resultCollector instanceof DefaultResultCollector)
			return ((DefaultResultCollector) resultCollector).shouldRestart();
		else if (resultCollector instanceof ExitingResultCollector)
			return ((ExitingResultCollector) resultCollector).shouldRestart();
		else
			return false;
	}

}