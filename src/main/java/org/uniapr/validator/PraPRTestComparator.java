/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr.validator;

import org.uniapr.commons.misc.MemberNameUtils;
import org.pitest.functional.predicate.Predicate;
import org.pitest.testapi.TestUnit;

import java.io.Serializable;
import java.util.Comparator;
import java.util.Map;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class PraPRTestComparator implements Comparator<TestUnit>, Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, Long> testsTiming;

    private final Predicate<String> isFailing;

    public PraPRTestComparator(Map<String, Long> testsTiming, Predicate<String> isFailing) {
        this.testsTiming = testsTiming;
        this.isFailing = isFailing;
    }

    @Override
    public int compare(TestUnit t1, TestUnit t2) {
        final String n1 = MemberNameUtils.sanitizeExtendedTestName(t1.getDescription().getName());
        final String n2 = MemberNameUtils.sanitizeExtendedTestName(t2.getDescription().getName());
        final boolean f1 = this.isFailing.apply(n1);
        final boolean f2 = this.isFailing.apply(n2);
        Long time2 = this.testsTiming.get(n2);
        if (time2 == null) time2 = Long.MAX_VALUE;
        Long time1 = this.testsTiming.get(n1);
        if (time1 == null) time1 = Long.MAX_VALUE;
        return (f1 ^ f2) ? (f1 ? -1 : 1) : Long.compare(time2, time1);
    }
}
