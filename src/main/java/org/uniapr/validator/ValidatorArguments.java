/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr.validator;

import org.uniapr.Patch;
import org.uniapr.commons.process.ChildProcessCommonArguments;
import org.apache.commons.lang3.Validate;

import java.io.File;
import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
class ValidatorArguments extends ChildProcessCommonArguments {
    private final PraPRTestComparator testComparator;

    private final Collection<Patch> patches;

    private final Map<String, Long> testsTiming;

    private final long timeoutBias;

    private final double timeoutCoefficient;

    private final boolean resetJVM;

    private final boolean resetInterface;
    
    private final boolean debug;

    private File d4jAllTestsFile;

    public ValidatorArguments(final PraPRTestComparator testComparator,
                              final Collection<String> appClassNames,
                              final Collection<Patch> patches,
                              final Map<String, Long> testsTiming,
                              final long timeoutBias,
                              final double timeoutCoefficient,
                              boolean resetJVM,
                              boolean resetInterface,
                              boolean debug,
                              File d4jAllTestsFile) {
        super(appClassNames);
        this.testComparator = testComparator;
        Validate.isInstanceOf(Serializable.class, patches);
        Validate.isInstanceOf(Serializable.class, testsTiming);
        this.patches = patches;
        this.testsTiming = testsTiming;
        this.timeoutBias = timeoutBias;
        this.timeoutCoefficient = timeoutCoefficient;
        this.resetJVM = resetJVM;
        this.resetInterface=resetInterface;
        this.debug=debug;
        this.d4jAllTestsFile = d4jAllTestsFile;
    }

    public PraPRTestComparator getTestComparator() {
        return this.testComparator;
    }

    public Collection<Patch> getPatches() {
        return this.patches;
    }

    public Map<String, Long> getTestsTiming() {
        return this.testsTiming;
    }

    public long getTimeoutBias() {
        return timeoutBias;
    }

    public double getTimeoutCoefficient() {
        return timeoutCoefficient;
    }

    public boolean shouldResetJVM() {
        return this.resetJVM;
    }
    
    public boolean shouldResetInterface() {
        return this.resetInterface;
    }
    
    public boolean getDebug() {
        return this.debug;
    }

    public File getD4jAllTestsFile() {
        return d4jAllTestsFile;
    }
}
