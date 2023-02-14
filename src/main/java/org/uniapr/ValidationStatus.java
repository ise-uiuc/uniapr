/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr;

import org.pitest.util.ExitCode;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public enum ValidationStatus {
    NOT_STARTED,
    STARTED,
    PLAUSIBLE,
    NON_PLAUSIBLE,
    TIMED_OUT,
    RUN_ERROR,
    INITIALIZER_ERROR,
    MEMORY_ERROR;

    public static ValidationStatus forExitCode(final ExitCode exitCode) {
        if (exitCode.equals(ExitCode.OUT_OF_MEMORY)) {
            return MEMORY_ERROR;
        } else if (exitCode.equals(ExitCode.TIMEOUT)) {
            return TIMED_OUT;
        } else {
            return RUN_ERROR;
        }
    }
}
