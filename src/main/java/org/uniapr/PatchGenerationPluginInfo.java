/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr;

import org.apache.commons.lang3.Validate;

import java.io.File;
import java.util.Map;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class PatchGenerationPluginInfo {
    private String name;

    private File compatibleJDKHome;

    private Map<String, String> params;

    public void setCompatibleJDKHome(final File compatibleJDKHome) {
        Validate.isTrue(compatibleJDKHome == null || compatibleJDKHome.isDirectory());
        this.compatibleJDKHome = compatibleJDKHome;
    }

    public void setName(final String name) {
        Validate.notNull(name);
        Validate.notEmpty(name);
        this.name = name;
    }

    public void setParams(final Map<String, String> params) {
        this.params = params;
    }

    public File getCompatibleJDKHome() {
        return this.compatibleJDKHome;
    }

    public String getName() {
        return this.name;
    }

    public Map<String, String> getParams() {
        return this.params;
    }

    public boolean matches(PatchGenerationPlugin plugin) {
        return this.name.equalsIgnoreCase(plugin.name());
    }
}
