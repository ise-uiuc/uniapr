
/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */

package org.uniapr.jvm.core;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.uniapr.jvm.agent.JVMClinitClassTransformer;

public class JVMStatus
{
	public final static String RECLINIT = "prapr_reclinit";
	public final static String PRAPR_GEN = "prapr_gen";
	public final static String LOCKCHECK = "lockedCheck";
	public final static String REFLOCKCHECK = "reflectionLockedCheck";
	public final static String CLASS = "org/uniapr/jvm/core/JVMStatus";
	//public static ConcurrentMap<String, Boolean> classStatusMap = new ConcurrentHashMap<String, Boolean>();
	public static ConcurrentMap<String, Integer> interfaces = new ConcurrentHashMap<String, Integer>();
	
	
	public static int classId = 0;
	public final static int MAX_CLASSNUM = 10000;
	public static boolean[] classStatusArray = new boolean[MAX_CLASSNUM];
	// whether the reflection methods already tried to reinitialize the class (avoid StackOverflowError)
	public static boolean[] reflectionClassStatusArray = new boolean[MAX_CLASSNUM];
	public static ConcurrentMap<String, Integer> classIdMap = new ConcurrentHashMap<String, Integer>();
	public static ConcurrentMap<Integer, String> idClassMap = new ConcurrentHashMap<Integer, String>();


	public static Set<String> excludedPrefixes = new HashSet<String>();
	public static Set<String> excludedTestPrefixes = new HashSet<String>();

	static {
		excludedPrefixes.add("org/junit"); // exclude junit
		excludedPrefixes.add("junit/"); // exclude junit
		excludedPrefixes.add("org/hamcrest"); // exclude junit
		excludedPrefixes.add("org/apache/maven"); // exclude build system
		excludedPrefixes.add("org/mudebug"); // exclude itself and used libs
		excludedPrefixes.add("org/pitest");
		excludedPrefixes.add("org/uniapr");
		excludedPrefixes.add("org/objectweb"); // exclude asm
		excludedPrefixes.add("org/codehaus");
		excludedPrefixes.add("org/prapr");
		excludedPrefixes.add("java/");
		excludedPrefixes.add("javax/");
		excludedPrefixes.add("sun/");
		excludedPrefixes.add("com/sun/");
		excludedPrefixes.add("org/xml/");
		excludedPrefixes.add("org/w3c/");
		excludedPrefixes.add("jdk/");
		excludedPrefixes.add("netscape/");
		excludedPrefixes.add("org/ietf/");
		excludedPrefixes.add("org/mockito/cglib/core");
		
		
		// unclear problematic cases that have to be excluded for now
		excludedPrefixes.add("org/jfree/data/time/RegularTimePeriod");
		excludedPrefixes.add("org/jfree/chart/util/ShapeUtilities");
		excludedPrefixes.add("org/jfree/chart/util/UnitType");
		
		String separator="/";
		// excluded since they are test inputs rather than source code
		excludedPrefixes.add("org"+separator+"apache/commons/lang3/reflect/testbed/");
		excludedPrefixes.add("org"+separator+"apache/commons/lang/reflect/testbed");
		excludedPrefixes.add("org"+separator+"apache/commons/lang/enum");
		
		// avoiding direct string constant since Maven Shade can transform them...
		/*excludedPrefixes.add("org"+File.separator+"apache/commons/lang/builder/StandardToStringStyleTest");
		excludedPrefixes.add("org"+File.separator+"apache/commons/lang/builder/ReflectionToStringBuilder");
		excludedPrefixes.add("org"+File.separator+"apache/commons/lang/builder/ToStringBuilder");*/
		
		// excluding problematic tests
		String dot=".";
		// excluded due to the flaky test bug in commons-lang
		excludedTestPrefixes.add("org"+dot+"apache.commons.lang.EntitiesPerformanceTest");		
		// excluded due to a potential bug in maven surefire as it works when run with ant
		excludedTestPrefixes.add("org"+dot+"apache.commons.lang.builder.ToStringBuilderTest");
		// excluded due to a potential bug in maven surefire as it works when run with ant
		excludedTestPrefixes.add("org"+dot+"joda.time.TestDateMidnight_Basics");
		excludedTestPrefixes.add("org"+dot+"apache.commons.lang3.EntitiesPerformanceTest");	
        excludedTestPrefixes.add("org"+dot+"apache.commons.lang3.builder.ToStringBuilderTest");

        excludedTestPrefixes.add("org"+dot+"jfree.data.time.junit.TimeSeriesTests");	
		
	}

	public static void resetClinit() {
		resetSystemProperties();
		for(int i=0;i<classStatusArray.length;i++){
			classStatusArray[i]=false;
			reflectionClassStatusArray[i] = false;
		}
	}

	public static boolean lockedCheck(int id) {
		if (classStatusArray[id]) {
			return true;
		} else {
			synchronized (Integer.valueOf(id)) {
				if (classStatusArray[id]) {
					return true;
				}
				classStatusArray[id] = true;
				return false;
			}
		}
	}

	public static void reflectionLockedCheck(Class clazz) {
		String slashName = clazz.getName().replace(".", "/");
		if (JVMClinitClassTransformer.leakingClasses.keySet()
				.contains(slashName)) {
			int id = registerClass(slashName);
			synchronized (clazz) {
				if (!reflectionClassStatusArray[id]) {
					reflectionClassStatusArray[id] = true;
					try {
						Method praprClinit = clazz.getMethod(RECLINIT, null);
						praprClinit.invoke(null);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
	}

	public static synchronized int registerClass(String slashClazz) {
		if(classIdMap.containsKey(slashClazz))
			return classIdMap.get(slashClazz);
		
		int id=nextId();
		classIdMap.put(slashClazz, id);
		idClassMap.put(id, slashClazz);

		return id;
	}
	
	private synchronized static int nextId() {
		return classId++;
	}

	public static void resetSystemProperties() {
		// reset all properties
		System.setProperties(null);
		// reset security manager
		System.setSecurityManager(null);
	}


	public static boolean isExcluded(String className) {
		for (String prefix : JVMStatus.excludedPrefixes) {
			if (className.startsWith(prefix))
				return true;
		}
		return false;
	}
	
	public static boolean isPrimitive(String desc){
		return desc.startsWith("L");
	}

}
