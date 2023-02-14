/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */
package org.uniapr.junit;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;
import org.pitest.classinfo.ClassName;
import org.pitest.functional.F;
import org.pitest.functional.FCollection;
import org.pitest.functional.Option;
import org.pitest.junit.adapter.AdaptedJUnitTestUnit;
import org.pitest.testapi.TestUnit;

import org.uniapr.commons.misc.MemberNameUtils;
import org.uniapr.jvm.core.JVMStatus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author Ali Ghanbari (ali.ghanbari@utdallas.edu)
 */
public class JUnitUtils {
    private static final Map<Class<?>, Set<Method>> VISITED;

    static {
        VISITED = new HashMap<>();
    }

    public static List<TestUnit> discoverTestUnits(final Collection<String> classNames) {
//        System.out.println("===== discoverTestUnits ===");
        final List<TestUnit> testUnits = new LinkedList<>();
        final Collection<Class<?>> classes =
                FCollection.flatMap(FCollection.map(classNames, ClassName.stringToClassName()),
                        ClassName.nameToClass());
        // find JUnit 4.XX tests
        testUnits.addAll(findJUnit4YYTestUnits(classes));
        testUnits.addAll(findJUnit3XXTestUnits(classes));
        
        // for bad test exclusion
        excludeTests(testUnits);
        	
        	
        for (final Map.Entry<Class<?>, Set<Method>> entry : VISITED.entrySet()) {
            entry.getValue().clear();
        }
        VISITED.clear();
        System.out.println("Found " + testUnits.size() + " test units");
        return testUnits;
    }

    public static List<TestUnit> discoverTestUnitsByAllTestsFile(File allTestsFile){
//        System.out.println("===== discoverTestUnitsByAllTestsFile ===");
        // Parse the allTestsFile
        F<String, ClassName> stringToClassName = ClassName.stringToClassName();
        F<ClassName, Option<Class<?>>> nameToClass = ClassName.nameToClass();
        final List<TestUnit> testUnits = new LinkedList<>();
        try(BufferedReader br = new BufferedReader(new FileReader(allTestsFile))){
            String line;
            while((line = br.readLine()) != null){
                int splitIdx = line.indexOf('(');
                int endIdx = line.indexOf(')');
                String testMethod = line.substring(0, splitIdx);
                if (testMethod.contains("[") && testMethod.contains("]")) {
                    testMethod = testMethod.substring(0, testMethod.indexOf('['));
                }
                String testClassName = line.substring(splitIdx + 1, endIdx);
                Option<Class<?>> testClassOption = nameToClass.apply(stringToClassName.apply(testClassName));
                if (testClassOption.hasSome()){
                    Class<?> testClass = testClassOption.value();
                    for (final Method method : testClass.getMethods()) {
                        if (method.getName().equals(testMethod) && shouldAdd(testClass, method)){
                            testUnits.add(createTestUnit(testClass, method));
                        }
                    }
                }

            }
        } catch (Throwable t){
            t.printStackTrace();
        }
        VISITED.clear();
        System.out.println("Found " + testUnits.size() + " test units");
        return testUnits;
    }

	protected static void excludeTests(final List<TestUnit> testUnits) {
		Set<TestUnit> set=new HashSet<>();
        for(TestUnit test: testUnits){
            final String testName =
                    MemberNameUtils.sanitizeExtendedTestName(test.getDescription().getName());
            //System.out.println("TESTNAME: "+testName+" "+JVMStatus.excludedTestPrefixes);
            
            for(String prefix: JVMStatus.excludedTestPrefixes){
        		if(testName.startsWith(prefix))
        			set.add(test);
        	}
        }
        testUnits.removeAll(set);
	}


    private static boolean shouldAdd(final Class<?> testSuite, final Method testCase) {
        Set<Method> methods = VISITED.get(testSuite);
        if (methods == null) {
            methods = new HashSet<>();
            VISITED.put(testSuite, methods);
        }
        return methods.add(testCase);
    }

    private static Collection<? extends TestUnit> findJUnit3XXTestUnits(Collection<Class<?>> classes) {
        final List<TestUnit> testUnits = new LinkedList<>();
        for (final Class<?> clazz : classes) {
            if (isAbstract(clazz) || isInnerClass(clazz)) {
                continue;
            }
            if (isJUnit3XXTestSuite(clazz)) {
                testUnits.addAll(findJUnit3XXTestUnits(clazz));
            }
        }
        return testUnits;
    }

    private static boolean isJUnit3XXTestSuite(Class<?> clazz) {
        do {
            clazz = clazz.getSuperclass();
            if (clazz == TestCase.class) {
                return true;
            }
        } while (clazz != null);
        return false;
    }

    private static Collection<? extends TestUnit> findJUnit3XXTestUnits(final Class<?> testSuite) {
        final List<TestUnit> testUnits = new LinkedList<>();
        for (final Method method : testSuite.getMethods()) {
            final int mod = method.getModifiers();
            if (Modifier.isAbstract(mod) || Modifier.isNative(mod) || !Modifier.isPublic(mod)) {
                continue;
            }
            if (method.getReturnType() == Void.TYPE
                    && method.getName().startsWith("test")
                    && shouldAdd(testSuite, method)) {
//                System.out.println("Found test: " + testSuite.getName() + "#" + method.getName());
                testUnits.add(createTestUnit(testSuite, method));
            }
        }
        return testUnits;
    }

    private static Collection<? extends TestUnit> findJUnit4YYTestUnits(Collection<Class<?>> classes) {
        final List<TestUnit> testUnits = new LinkedList<>();
        for (final Class<?> clazz : classes) {
            if (isAbstract(clazz) || isInnerClass(clazz)) {
                continue;
            }
            for (final Method method : clazz.getMethods()) {
                final int mod = method.getModifiers();
                if (Modifier.isAbstract(mod) || Modifier.isNative(mod) || !Modifier.isPublic(mod)) {
                    continue;
                }
                final Test annotation = method.getAnnotation(Test.class);
                if (annotation != null && shouldAdd(clazz, method)) {
//                    System.out.println("Found test: " + clazz.getName() + "#" + method.getName());
                    testUnits.add(createTestUnit(clazz, method));
                }
            }
        }
        return testUnits;
    }

    private static TestUnit createTestUnit(final Class<?> testSuite, final Method testCase) {
        final Description testDescription = Description.createTestDescription(testSuite,
                testCase.getName(),
                testCase.getDeclaredAnnotations());

        final Filter filter = new Filter() {
            @Override
            public boolean shouldRun(Description description) {
                if (description.isTest()) {
                    String methodName = description.getMethodName();
                    String className = description.getClassName();
                    // in case of parameterized test, whose methodName will be like "testArchive[0]"
                    if (methodName.contains("[") && methodName.contains("]")){
                        methodName = methodName.substring(0, methodName.indexOf('['));
                    }
                    return testDescription.getMethodName().equals(methodName)
                            && testDescription.getClassName().equals(className);
                }
                // explicitly check if any children want to run
                for (Description each : description.getChildren()) {
                    if (shouldRun(each)) {
                        return true;
                    }
                }
                return false;
            }
            @Override
            public String describe() {
                return String.format("Method %s", testDescription.getDisplayName());
            }
        };
        return new AdaptedJUnitTestUnit(testSuite, Option.some(filter));
    }

    private static boolean isAbstract(final Class<?> clazz) {
        final int mod = clazz.getModifiers();
        return Modifier.isInterface(mod) || Modifier.isAbstract(mod);
    }

    // Non-static nested classes are called inner classes.
    // JUnit/Maven will not run tests for such test classes.
    private static boolean isInnerClass(final Class<?> clazz) {
        final int mod = clazz.getModifiers();
        return clazz.isMemberClass() && !Modifier.isStatic(mod);
    }
}