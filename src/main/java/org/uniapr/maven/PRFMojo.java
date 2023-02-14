/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr.maven;

import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.ResolutionScope;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
@Mojo(name = "validate", requiresDependencyResolution = ResolutionScope.TEST)
public class PRFMojo extends AbstractPRFMojo {

}
