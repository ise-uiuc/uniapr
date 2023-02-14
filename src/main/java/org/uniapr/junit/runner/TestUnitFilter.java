/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr.junit.runner;

import org.uniapr.commons.misc.MemberNameUtils;
import org.pitest.functional.predicate.Predicate;
import org.pitest.testapi.TestUnit;

import java.util.Collection;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class TestUnitFilter {
    public static Predicate<TestUnit> all() {
        return new Predicate<TestUnit>() {
            @Override
            public Boolean apply(TestUnit testUnit) {
                return Boolean.TRUE;
            }
        };
    }

    public static Predicate<TestUnit> some(final Collection<String> testUnitNames) {
        return new Predicate<TestUnit>() {
            @Override
            public Boolean apply(TestUnit testUnit) {
                final String testName = MemberNameUtils.sanitizeExtendedTestName(testUnit.getDescription().getName());
                return testUnitNames.contains(testName);
            }
        };
    }
}