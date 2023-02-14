/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr;

import java.io.File;
import java.util.Map;

/**
 * Classes implementing this interface must have an accessible default constructor
 *
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public interface PatchGenerationPlugin {
    /**
     * Program repair algorithms are patch generation engines that can be viewed as plugins
     *
     * @param compatibleJDKHome Sometimes some APR tools, like CapGen, require higher versions of JDK
     * @param patchesPool The directory under which the generated, compiled patches are to be stored
     * @param params All the param(s) that the plugin might need. E.g., CapGen needs the location of JDK,
     *               as well as, the bug id to be fixed.
     * @throws Exception An exception is expected to be thrown in case of failure
     */
    void generatePatches(File compatibleJDKHome,
                         File patchesPool,
                         Map<String, String> params) throws Exception;

    String name();

    String description();
}
