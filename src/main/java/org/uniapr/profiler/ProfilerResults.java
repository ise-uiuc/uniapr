/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr.profiler;

import org.pitest.functional.predicate.Predicate;

import java.util.Map;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public interface ProfilerResults {
    Map<String, Long> getTestsTiming();

    Predicate<String> getIsFailingTestPredicate();
}
