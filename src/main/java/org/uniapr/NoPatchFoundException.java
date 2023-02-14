/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public final class NoPatchFoundException extends Exception {
    public NoPatchFoundException() {
    }

    public NoPatchFoundException(String message) {
        super(message);
    }
}
