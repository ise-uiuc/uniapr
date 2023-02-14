/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */

package org.uniapr.junit.runner;

import org.pitest.testapi.Description;
import org.pitest.testapi.ResultCollector;

public class ExitingResultCollector implements ResultCollector
{

	private final ResultCollector child;
	private boolean hadFailure = false;
	private boolean debugMode = false;

	public ExitingResultCollector(final ResultCollector child, boolean debugMode) {
		this.child = child;
		this.debugMode = debugMode;
	}

	public void notifySkipped(final Description description) {
		this.child.notifySkipped(description);
	}

	public void notifyStart(final Description description) {
		this.child.notifyStart(description);
	}

	public boolean shouldExit() {
		return this.hadFailure;
	}

	public void notifyEnd(final Description description, final Throwable t) {
		this.child.notifyEnd(description, t);
		if (t != null) {
			this.hadFailure = true;
			if (debugMode) {
				System.out.flush();
				System.err.println();
				t.printStackTrace();
				System.err.println();
				System.err.flush();
			}
		}

	}

	public void notifyEnd(final Description description) {
		this.child.notifyEnd(description);
	}

	public boolean shouldRestart() {
		if (child instanceof DefaultResultCollector)
			return ((DefaultResultCollector) child).shouldRestart();
		return false;
	}

}