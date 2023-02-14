/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr.commons.process;

import org.apache.commons.lang3.Validate;

import java.io.Serializable;
import java.util.Collection;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public abstract class ChildProcessCommonArguments implements Serializable {
    protected static final long serialVersionUID = 1L;

    protected final Collection<String> appClassNames;

    protected ChildProcessCommonArguments(final Collection<String> appClassNames) {
        Validate.isInstanceOf(Serializable.class, appClassNames);
        this.appClassNames = appClassNames;
    }

    public Collection<String> getAppClassNames() {
        return appClassNames;
    }
}
