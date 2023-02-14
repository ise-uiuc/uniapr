/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr.profiler;

import org.uniapr.commons.process.ChildProcessCommonArguments;

import java.io.File;
import java.util.List;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
class ProfilerArguments extends ChildProcessCommonArguments {
    private final boolean debug;
    private File d4jAllTestsFile;
    public ProfilerArguments(final List<String> appClassNames, boolean debug, File d4jAllTestsFile) {
        super(appClassNames);
        this.debug=debug;
        this.d4jAllTestsFile = d4jAllTestsFile;
    }
    public boolean getDebug() {
        return this.debug;
    }

    public File getD4jAllTestsFile() {
        return d4jAllTestsFile;
    }
}
