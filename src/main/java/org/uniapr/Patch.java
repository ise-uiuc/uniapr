/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr;

import org.uniapr.agent.HotSwapAgent;
import org.uniapr.junit.runner.JUnitRunner;
import org.pitest.classinfo.ClassByteArraySource;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class Patch implements Serializable {
    private static final long serialVersionUID = 1L;

    private final PatchId id;
    
    public final String patchName;

    private final List<LoadedClass> patchedClasses;

    private final List<String> coveringTestNames;

    public Patch(final Collection<LoadedClass> patchedClasses,
                 final Collection<String> coveringTestNames, String patchName) {
        this.id = PatchId.alloc();
        this.patchName=patchName;
        this.patchedClasses = new ArrayList<>(patchedClasses);
        this.coveringTestNames = new ArrayList<>(coveringTestNames);
    }

    /**
     * Redefines the classes to be patches while taking backups from the patches classes.
     * It then runs the given test cases.
     * Regardless of the result of validation, the redefined classes shall be recovered
     * immediately after running test cases.
     * Normal termination means success.
     *
     * @param byteArraySource Class byte array source, used for taking backups
     * @param loader Class loader for the current JVM session
     * @return <code>true</code> iff the patch is a plausible one
     * @throws ClassNotFoundException Any exception while installing
     */
    public int test(final ClassByteArraySource byteArraySource,
                        final ClassLoader loader,
                        final JUnitRunner runner,
                        final Map<String, Long> testsTiming,
                        final long timeoutBias,
                        final double timeoutCoefficient) throws ClassNotFoundException {
        final List<LoadedClass> originalClasses = new LinkedList<>();
        // take backup and install
        try {
            for (final LoadedClass patchedClass : this.patchedClasses) {
                final String className = patchedClass.getJavaName();
                final Class<?> clazz = Class.forName(className, false, loader);
                originalClasses.add(new LoadedClass(className,
                        byteArraySource.getBytes(className).value()));
                if (!HotSwapAgent.hotSwap(clazz, patchedClass.getBytes())) {
                    throw new IllegalStateException("Unable to install the class " + className);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
        // test
        // TODO: run only covering tests
        final int res = runner.run(testsTiming, timeoutBias, timeoutCoefficient);//(TestUnitFilter.some(this.coveringTestNames));
        // restore
        for (final LoadedClass original : originalClasses) {
            final String className = original.getJavaName();
            final Class<?> clazz = Class.forName(className, false, loader);
            if (!HotSwapAgent.hotSwap(clazz, original.getBytes())) {
                throw new IllegalStateException("Unable to restore the class " + className);
            }
        }
        return res;
    }

    public PatchId getId() {
        return this.id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Patch that = (Patch) o;
        return this.id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }
}
