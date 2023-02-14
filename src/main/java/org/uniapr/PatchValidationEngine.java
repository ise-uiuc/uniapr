/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr;

import org.uniapr.validator.PatchValidator;
import org.uniapr.validator.PraPRTestComparator;
import org.pitest.process.ProcessArgs;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class PatchValidationEngine {
    private final ProcessArgs defaultProcessArgs;

    private final List<String> appClassNames;

    private final PraPRTestComparator comparator;

    private final Map<String, Long> testsTiming;

    private final long timeoutBias;

    private final double timeoutCoefficient;

    private final Map<PatchId, Patch> patchMap;

    private final ValidationStatusTable statusTable;

    private final boolean resetJVM;

    private final boolean restartJVM;

    private final boolean resetInterface;
    
    private final boolean debug;

    private final File d4jAllTestsFile;

    private PatchValidationEngine(final ProcessArgs defaultProcessArgs,
                                  final List<String> appClassNames,
                                  final PraPRTestComparator comparator,
                                  final Map<String, Long> testsTiming,
                                  final long timeoutBias,
                                  final double timeoutCoefficient,
                                  final Map<PatchId, Patch> patchMap,
                                  final ValidationStatusTable statusTable,
                                  boolean resetJVM,
                                  boolean restartJVM,
                                  boolean resetInterface,
                                  boolean debug,
                                  File d4jAllTestsFile) {
        this.defaultProcessArgs = defaultProcessArgs;
        this.comparator = comparator;
        this.appClassNames = appClassNames;
        this.testsTiming = testsTiming;
        this.timeoutBias = timeoutBias;
        this.timeoutCoefficient = timeoutCoefficient;
        this.patchMap = patchMap;
        this.statusTable = statusTable;
        this.resetJVM = resetJVM;
        this.restartJVM = restartJVM;
        this.resetInterface=resetInterface;
        this.debug=debug;
        this.d4jAllTestsFile = d4jAllTestsFile;
    }

    public static PatchValidationEngine createEngine() {
        return new PatchValidationEngine(null, null, null, null, 0L, -1D, null, null, false, false, false, false, null);
    }

    public PatchValidationEngine forProcessArgs(final ProcessArgs processArgs) {
        return new PatchValidationEngine(processArgs, this.appClassNames, this.comparator, this.testsTiming, this.timeoutBias, this.timeoutCoefficient, this.patchMap, this.statusTable, this.resetJVM, this.restartJVM, this.resetInterface, this.debug, this.d4jAllTestsFile);
    }

    public PatchValidationEngine forAppClassNames(final List<String> appClassNames) {
        return new PatchValidationEngine(this.defaultProcessArgs, appClassNames, this.comparator, this.testsTiming, this.timeoutBias, this.timeoutCoefficient, this.patchMap, this.statusTable, this.resetJVM, this.restartJVM, this.resetInterface, this.debug, this.d4jAllTestsFile);
    }

    public PatchValidationEngine forComparator(final PraPRTestComparator comparator) {
        return new PatchValidationEngine(this.defaultProcessArgs, this.appClassNames, comparator, this.testsTiming, this.timeoutBias, this.timeoutCoefficient, this.patchMap, this.statusTable, this.resetJVM, this.restartJVM, this.resetInterface, this.debug, this.d4jAllTestsFile);
    }

    public PatchValidationEngine forTestsTiming(final Map<String, Long> testsTiming) {
        return new PatchValidationEngine(this.defaultProcessArgs, this.appClassNames, this.comparator, testsTiming, this.timeoutBias, this.timeoutCoefficient, this.patchMap, this.statusTable, this.resetJVM, this.restartJVM, this.resetInterface, this.debug, this.d4jAllTestsFile);
    }

    public PatchValidationEngine forTimeoutCoefficient(final double timeoutCoefficient) {
        return new PatchValidationEngine(this.defaultProcessArgs, this.appClassNames, this.comparator, this.testsTiming, this.timeoutBias, timeoutCoefficient, this.patchMap, this.statusTable, this.resetJVM, this.restartJVM, this.resetInterface, this.debug, this.d4jAllTestsFile);
    }

    public PatchValidationEngine forPatches(final List<Patch> patches) {
        final Map<PatchId, Patch> patchMap = new LinkedHashMap<>();
        final ValidationStatusTable statusTable = new ValidationStatusTable();
        for (final Patch patch : patches) {
            final PatchId id = patch.getId();
            patchMap.put(id, patch);
            statusTable.put(id, ValidationStatus.NOT_STARTED);
        }
        return new PatchValidationEngine(this.defaultProcessArgs, this.appClassNames, this.comparator, this.testsTiming, this.timeoutBias, this.timeoutCoefficient, patchMap, statusTable, this.resetJVM, this.restartJVM, this.resetInterface, this.debug, this.d4jAllTestsFile);
    }

    public PatchValidationEngine forTimeoutBias(final long timeoutBias) {
        return new PatchValidationEngine(this.defaultProcessArgs, this.appClassNames, this.comparator, this.testsTiming, timeoutBias, this.timeoutCoefficient, this.patchMap, this.statusTable, this.resetJVM, this.restartJVM, this.resetInterface, this.debug, this.d4jAllTestsFile);
    }

    public PatchValidationEngine forResetJVM(final boolean resetJVM) {
        return new PatchValidationEngine(this.defaultProcessArgs, this.appClassNames, this.comparator, this.testsTiming, this.timeoutBias, this.timeoutCoefficient, this.patchMap, this.statusTable, resetJVM, this.restartJVM, this.resetInterface, this.debug, this.d4jAllTestsFile);
    }

    public PatchValidationEngine forRestartJVM(final boolean restartJVM) {
        return new PatchValidationEngine(this.defaultProcessArgs, this.appClassNames, this.comparator, this.testsTiming, this.timeoutBias, this.timeoutCoefficient, this.patchMap, this.statusTable, resetJVM, restartJVM, this.resetInterface, this.debug, this.d4jAllTestsFile);
    }

    public PatchValidationEngine forResetInterface(final boolean resetInterface) {
        return new PatchValidationEngine(this.defaultProcessArgs, this.appClassNames, this.comparator, this.testsTiming, this.timeoutBias, this.timeoutCoefficient, this.patchMap, this.statusTable, this.resetJVM, this.restartJVM, resetInterface, this.debug, this.d4jAllTestsFile);
    }
    
    public PatchValidationEngine forDebug(final boolean debug) {
        return new PatchValidationEngine(this.defaultProcessArgs, this.appClassNames, this.comparator, this.testsTiming, this.timeoutBias, this.timeoutCoefficient, this.patchMap, this.statusTable, this.resetJVM, this.restartJVM, this.resetInterface, debug, this.d4jAllTestsFile);
    }

    public PatchValidationEngine forD4jAllTestsFile(final File d4jAllTestsFile) {
        return new PatchValidationEngine(this.defaultProcessArgs, this.appClassNames, this.comparator, this.testsTiming, this.timeoutBias, this.timeoutCoefficient, this.patchMap, this.statusTable, this.resetJVM, this.restartJVM, this.resetInterface, debug, d4jAllTestsFile);
    }

    public void test() {
        int iterationCount = 0;
        Collection<Patch> notStarted;
        while (!(notStarted = getNotStartedPatches()).isEmpty()) {
            if (this.restartJVM){
                notStarted = Collections.singletonList(notStarted.iterator().next());
            }
            System.out.println("Iteration #" + (++iterationCount));
            final ValidationStatusTable statusTable = PatchValidator.testPatches(this.defaultProcessArgs,
                    this.comparator,
                    this.appClassNames,
                    notStarted,
                    this.testsTiming,
                    this.timeoutBias,
                    this.timeoutCoefficient,
                    this.resetJVM,
                    this.resetInterface,
                    this.debug,
                    this.d4jAllTestsFile);
            for (final Map.Entry<PatchId, ValidationStatus> entry : statusTable.entrySet()) {
                if (entry.getValue() != ValidationStatus.NOT_STARTED) {
                    this.statusTable.put(entry.getKey(), entry.getValue());
                }
            }
        }
    }

    private List<Patch> getNotStartedPatches() {
        final ArrayList<Patch> notStarted = new ArrayList<>();
        for (final Map.Entry<PatchId, ValidationStatus> statusEntry : this.statusTable.entrySet()) {
            if (statusEntry.getValue() == ValidationStatus.NOT_STARTED) {
                notStarted.add(this.patchMap.get(statusEntry.getKey()));
            }
        }
        notStarted.trimToSize();
        return notStarted;
    }

    public ValidationStatusTable getStatusTable() {
        return this.statusTable;
    }
}
