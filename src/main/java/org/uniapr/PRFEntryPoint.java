/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.pitest.classinfo.ClassByteArraySource;
import org.pitest.classinfo.ClassInfo;
import org.pitest.classpath.ClassFilter;
import org.pitest.classpath.ClassPath;
import org.pitest.classpath.CodeSource;
import org.pitest.classpath.PathFilter;
import org.pitest.classpath.ProjectClassPaths;
import org.pitest.functional.predicate.Predicate;
import org.pitest.functional.prelude.Prelude;
import org.pitest.mutationtest.config.DefaultCodePathPredicate;
import org.pitest.mutationtest.config.DefaultDependencyPathPredicate;
import org.pitest.mutationtest.tooling.KnownLocationJavaAgentFinder;
import org.pitest.process.JavaAgent;
import org.pitest.process.JavaExecutableLocator;
import org.pitest.process.KnownLocationJavaExecutableLocator;
import org.pitest.process.LaunchOptions;
import org.pitest.process.ProcessArgs;
import org.uniapr.agent.JarCreatingJarFinder;
import org.uniapr.commons.process.LoggerUtils;
import org.uniapr.profiler.ProfilerDriver;
import org.uniapr.profiler.ProfilerResults;
import org.uniapr.validator.PraPRTestComparator;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class PRFEntryPoint {
    private final ClassPath classPath;

    private final ClassByteArraySource byteArraySource;

    private final String whiteListPrefix;

    private final List<String> failingTests;

    private final File compatibleJREHome;

    private final long timeoutBias;

    private final double timeoutCoefficient;

    private final File patchesPool;

    private final PatchGenerationPlugin patchGenerationPluginImpl;

    private final boolean resetJVM;

    private final boolean restartJVM;

    private final boolean resetInterface;
    
    private final boolean debug;

    private final File d4jAllTestsFile;

    private final String argLine;

    private final boolean profilerOnly;

    private PRFEntryPoint(final ClassPath classPath,
                          final ClassByteArraySource byteArraySource,
                          final String whiteListPrefix,
                          final List<String> failingTests,
                          final File compatibleJREHome,
                          final long timeoutBias,
                          final double timeoutCoefficient,
                          final File patchesPool,
                          final PatchGenerationPlugin patchGenerationPluginImpl,
                          boolean resetJVM, 
                          boolean restartJVM,
                          boolean resetInterface,
                          boolean debug,
                          final File d4jAllTestsFile,
                          String argLine,
                          boolean profilerOnly) {
        this.classPath = classPath;
        this.byteArraySource = byteArraySource;
        this.whiteListPrefix = whiteListPrefix;
        this.failingTests = failingTests;
        this.compatibleJREHome = compatibleJREHome;
        this.timeoutBias = timeoutBias;
        this.timeoutCoefficient = timeoutCoefficient;
        this.patchesPool = patchesPool;
        this.patchGenerationPluginImpl = patchGenerationPluginImpl;
        this.resetJVM = resetJVM;
        this.restartJVM = restartJVM;
        this.resetInterface=resetInterface;
        this.debug=debug;
        this.d4jAllTestsFile = d4jAllTestsFile;
        this.argLine = argLine;
        this.profilerOnly = profilerOnly;
    }

    public static PRFEntryPoint createEntryPoint() {
        return new PRFEntryPoint(null, null, null, null, null, 0L, -1D, null, null, false, false, false, false, null, null, false);
    }

    public PRFEntryPoint withClassPath(final ClassPath classPath) {
        return new PRFEntryPoint(classPath, this.byteArraySource, this.whiteListPrefix, this.failingTests, this.compatibleJREHome, this.timeoutBias, this.timeoutCoefficient, this.patchesPool, this.patchGenerationPluginImpl, this.resetJVM, this.restartJVM, this.resetInterface, this.debug, this.d4jAllTestsFile, this.argLine, this.profilerOnly);
    }

    public PRFEntryPoint withWhiteListPrefix(final String whiteListPrefix) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, whiteListPrefix, this.failingTests, this.compatibleJREHome, this.timeoutBias, this.timeoutCoefficient, this.patchesPool, this.patchGenerationPluginImpl, this.resetJVM, this.restartJVM, this.resetInterface, this.debug, this.d4jAllTestsFile, this.argLine, this.profilerOnly);
    }

    public PRFEntryPoint withFailingTests(final List<String> failingTests) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.whiteListPrefix, failingTests, this.compatibleJREHome, this.timeoutBias, this.timeoutCoefficient, this.patchesPool, this.patchGenerationPluginImpl, this.resetJVM, this.restartJVM, this.resetInterface, this.debug, this.d4jAllTestsFile, this.argLine, this.profilerOnly);
    }

    public PRFEntryPoint withCompatibleJREHome(final File compatibleJREHome) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.whiteListPrefix, this.failingTests, compatibleJREHome, this.timeoutBias, this.timeoutCoefficient, this.patchesPool, this.patchGenerationPluginImpl, this.resetJVM, this.restartJVM, this.resetInterface, this.debug, this.d4jAllTestsFile, this.argLine, this.profilerOnly);
    }

    public PRFEntryPoint withByteArraySource(final ClassByteArraySource byteArraySource) {
        return new PRFEntryPoint(this.classPath, byteArraySource, this.whiteListPrefix, this.failingTests, this.compatibleJREHome, this.timeoutBias, this.timeoutCoefficient, this.patchesPool, this.patchGenerationPluginImpl, this.resetJVM, this.restartJVM, this.resetInterface, this.debug, this.d4jAllTestsFile, this.argLine, this.profilerOnly);
    }

    public PRFEntryPoint withTimeoutCoefficient(final double timeoutCoefficient) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.whiteListPrefix, this.failingTests, this.compatibleJREHome, this.timeoutBias, timeoutCoefficient, this.patchesPool, this.patchGenerationPluginImpl, this.resetJVM, this.restartJVM, this.resetInterface, this.debug, this.d4jAllTestsFile, this.argLine, this.profilerOnly);
    }

    public PRFEntryPoint withPatchesPool(final File patchesPool) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.whiteListPrefix, this.failingTests, this.compatibleJREHome, this.timeoutBias, this.timeoutCoefficient, patchesPool, this.patchGenerationPluginImpl, this.resetJVM, this.restartJVM, this.resetInterface, this.debug, this.d4jAllTestsFile, this.argLine, this.profilerOnly);
    }

    public PRFEntryPoint withPatchGenerationPlugin(final PatchGenerationPlugin patchGenerationPluginImpl) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.whiteListPrefix, this.failingTests, this.compatibleJREHome, this.timeoutBias, this.timeoutCoefficient, this.patchesPool, patchGenerationPluginImpl, this.resetJVM, this.restartJVM, this.resetInterface, this.debug, this.d4jAllTestsFile, this.argLine, this.profilerOnly);
    }

    public PRFEntryPoint withTimeoutBias(final long timeoutBias) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.whiteListPrefix, this.failingTests, this.compatibleJREHome, timeoutBias, this.timeoutCoefficient, this.patchesPool, this.patchGenerationPluginImpl, this.resetJVM, this.restartJVM, this.resetInterface, this.debug, this.d4jAllTestsFile, this.argLine, this.profilerOnly);
    }

    public PRFEntryPoint withResetJVM(final boolean resetJVM) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.whiteListPrefix, this.failingTests, this.compatibleJREHome, this.timeoutBias, this.timeoutCoefficient, this.patchesPool, this.patchGenerationPluginImpl, resetJVM, this.restartJVM, this.resetInterface, this.debug, this.d4jAllTestsFile, this.argLine, this.profilerOnly);
    }

    public PRFEntryPoint withRestartJVM(final boolean restartJVM) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.whiteListPrefix, this.failingTests, this.compatibleJREHome, this.timeoutBias, this.timeoutCoefficient, this.patchesPool, this.patchGenerationPluginImpl, this.resetJVM, restartJVM, this.resetInterface, this.debug, this.d4jAllTestsFile, this.argLine, this.profilerOnly);
    }
    
    public PRFEntryPoint withResetInterface(final boolean resetInterface) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.whiteListPrefix, this.failingTests, this.compatibleJREHome, this.timeoutBias, this.timeoutCoefficient, this.patchesPool, this.patchGenerationPluginImpl, this.resetJVM, this.restartJVM, resetInterface, this.debug, this.d4jAllTestsFile, this.argLine, this.profilerOnly);
    }
    
    public PRFEntryPoint withDebug(final boolean debug) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.whiteListPrefix, this.failingTests, this.compatibleJREHome, this.timeoutBias, this.timeoutCoefficient, this.patchesPool, this.patchGenerationPluginImpl, this.resetJVM, this.restartJVM, this.resetInterface, debug, this.d4jAllTestsFile, this.argLine, this.profilerOnly);
    }

    public PRFEntryPoint withD4jAllTestsFile(final File d4jAllTestsFile) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.whiteListPrefix, this.failingTests, this.compatibleJREHome, this.timeoutBias, this.timeoutCoefficient, this.patchesPool, this.patchGenerationPluginImpl, this.resetJVM, this.restartJVM, this.resetInterface, debug, d4jAllTestsFile, this.argLine, this.profilerOnly);
    }

    public PRFEntryPoint withArgLine(final String argLine) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.whiteListPrefix, this.failingTests, this.compatibleJREHome, this.timeoutBias, this.timeoutCoefficient, this.patchesPool, this.patchGenerationPluginImpl, this.resetJVM, this.restartJVM, this.resetInterface, debug, d4jAllTestsFile, argLine, this.profilerOnly);
    }

    public PRFEntryPoint withProfilerOnly(final boolean profilerOnly) {
        return new PRFEntryPoint(this.classPath, this.byteArraySource, this.whiteListPrefix, this.failingTests, this.compatibleJREHome, this.timeoutBias, this.timeoutCoefficient, this.patchesPool, this.patchGenerationPluginImpl, this.resetJVM, this.restartJVM, this.resetInterface, debug, d4jAllTestsFile, argLine, profilerOnly);
    }

    public void run(final PatchGenerationPluginInfo info) throws Exception {
        // generate patches:
        if (this.patchGenerationPluginImpl != null) {
            this.patchGenerationPluginImpl.generatePatches(info.getCompatibleJDKHome(),
                this.patchesPool, info.getParams());
        }
        // run profiler:
        final ProcessArgs defaultProcessArgs = getDefaultProcessArgs();
        final List<String> appClassNames = retrieveApplicationClassNames();
        ProfilerResults profilerResults =
                ProfilerDriver.runProfiler(defaultProcessArgs, appClassNames, this.debug, this.d4jAllTestsFile);
        if (profilerOnly){
            System.exit(0);
        }
        final Map<String, Long> testsTiming = profilerResults.getTestsTiming();
        final PraPRTestComparator comparator =
                new PraPRTestComparator(testsTiming, profilerResults.getIsFailingTestPredicate());
        // load the patches:
        // we except a separate folder be devoted to each patch wherein all the classes reside
        // some of them might be mutated, some not
        final PatchLoader patchLoader = new PatchLoader(this.patchesPool);
        final List<Patch> patches = patchLoader.loadPatches();
        // run validator through invoking the engine
        final PatchValidationEngine engine = PatchValidationEngine.createEngine()
                .forAppClassNames(appClassNames)
                .forComparator(comparator)
                .forPatches(patches)
                .forProcessArgs(defaultProcessArgs)
                .forTestsTiming(testsTiming)
                .forTimeoutBias(this.timeoutBias)
                .forTimeoutCoefficient(this.timeoutCoefficient)
                .forResetJVM(this.resetJVM)
                .forRestartJVM(this.restartJVM)
                .forResetInterface(this.resetInterface)
                .forDebug(this.debug)
                .forD4jAllTestsFile(d4jAllTestsFile);
        final long start = System.currentTimeMillis();
        engine.test();
        System.out.println("***VALIDATION-TOOK: " + (System.currentTimeMillis() - start));
        Set<Integer> plausiblePatchIds=getPlausiblePatches(engine.getStatusTable());
        System.out.println("# of plausible patches: " + plausiblePatchIds.size());
        System.out.println("Detailed set of plausible patchIDs: " + plausiblePatchIds);
        System.out.println("Directory of plausible patches: " + getPlausiblePatchesDirNum(patches, plausiblePatchIds));
    }

    private Set<String> getPlausiblePatchesDirNum(List<Patch> patches, Set<Integer> plausiblePatchIds) {
        Set<String> res = new HashSet<>();
        Map<Integer, String> idDirMap = new HashMap<>();
        for (Patch patch: patches){
            idDirMap.put(patch.getId().getId(), patch.patchName);
        }
        for (Integer id: plausiblePatchIds){
            res.add(idDirMap.get(id));
        }
        return res;
    }

    private Set<Integer> getPlausiblePatches(ValidationStatusTable statusTable) {
        Set<Integer> plausiblePatchIds=new HashSet<Integer>();
        for (final Map.Entry<PatchId, ValidationStatus> vs : statusTable.entrySet()) {
            if (vs.getValue() == ValidationStatus.PLAUSIBLE) {
            	plausiblePatchIds.add(vs.getKey().getId());
            }
        }
        return plausiblePatchIds;
    }

    private List<String> retrieveApplicationClassNames() {
        final ProjectClassPaths pcp = new ProjectClassPaths(this.classPath,
                defaultClassFilter(this.whiteListPrefix),
                defaultPathFilter());
        final CodeSource codeSource = new CodeSource(pcp);
        final ArrayList<String> classNames = new ArrayList<>();
        for (final ClassInfo classInfo : codeSource.getTests()) {
            classNames.add(classInfo.getName().asJavaName());
        }
        Collections.sort(classNames);
        classNames.trimToSize();
        return classNames;
    }

    private static PathFilter defaultPathFilter() {
        return new PathFilter(new DefaultCodePathPredicate(),
                Prelude.not(new DefaultDependencyPathPredicate()));
    }

    private static ClassFilter defaultClassFilter(final String whiteListPrefix) {
        final Predicate<String> filter = new Predicate<String>() {
            @Override
            public Boolean apply(String cn) {
                return cn.startsWith(whiteListPrefix);
            }
        };
        return new ClassFilter(filter, filter);
    }

    private ProcessArgs getDefaultProcessArgs() {
        List<String> jvmArgs = new ArrayList<>();
        jvmArgs.add("-Xmx40g");
        jvmArgs.addAll(getArgLines());
        final LaunchOptions defaultLaunchOptions = new LaunchOptions(getJavaAgent(),
                getDefaultJavaExecutableLocator(),
                jvmArgs,
                Collections.<String, String>emptyMap());
        return ProcessArgs.withClassPath(this.classPath)
                .andLaunchOptions(defaultLaunchOptions)
                .andStderr(LoggerUtils.err())
                .andStdout(LoggerUtils.out());
    }

    private JavaExecutableLocator getDefaultJavaExecutableLocator() {
        final File javaFile = FileUtils.getFile(this.compatibleJREHome, "bin", "java");
        return new KnownLocationJavaExecutableLocator(javaFile.getAbsolutePath());
    }

    private JavaAgent getJavaAgent() {
        final String jarLocation = (new JarCreatingJarFinder(this.byteArraySource))
                .getJarLocation()
                .value();
        return new KnownLocationJavaAgentFinder(jarLocation);
    }

    private List<String> getArgLines() {
        List<String> res = new ArrayList<>();
        if (argLine != null && !argLine.isEmpty()){
            String[] args = argLine.split("[;]");
            for (String arg: args){
                res.add(arg);
            }
        }
        return res;
    }
}
