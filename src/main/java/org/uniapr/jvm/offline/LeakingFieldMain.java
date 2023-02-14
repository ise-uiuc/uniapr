/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */

package org.uniapr.jvm.offline;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.objectweb.asm.ClassReader;

public class LeakingFieldMain
{
	public static Map<String, Boolean> classes = new HashMap<String, Boolean>();
	public static Set<String> visitedAndSafe = new HashSet<String>();
	public static Map<String, String> completeParentMap = new HashMap<String, String>();
	static Map<String, List<String>> prunedInheritanceMap = new HashMap<String, List<String>>();

	public static int counter = 0;
	public static String CLASSLOG = "classes.log";
	public static String INHERILOG = "inheritance.log";


	public static void main(String[] args) {
		PrintStream printer = System.out;
		long time1 = System.currentTimeMillis();
		String path="/Users/lingmingzhang/Research/workspace/example/target/classes:/Users/lingmingzhang/Research/workspace/example/target/test-classes";
		String[] items = path.split(":");
		for (String item : items) {
			if (item.endsWith(".jar")) {
				try {
					LeakingFieldMain.analyze(new JarFile(item));
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				LeakingFieldMain.analyze(item);
			}
		}
		long time2 = System.currentTimeMillis();
		String dir="/Users/lingmingzhang/Research/workspace/example/";
		int serialized = serialize(dir+CLASSLOG, dir+INHERILOG);
		printer.println("Analyzed: " + counter + " files, Leaking: "
				+ classes.size() + " files, After filtering: " + serialized
				+ " files stored!");
		printer.println("Offline analysis cost: " + (time2 - time1) + "ms");
	}
	
	public static void invoke(String path) {
		PrintStream printer = System.out;
		long time1 = System.currentTimeMillis();

		String[] items = path.split(":");
		for (String item : items) {
			if (item.endsWith(".jar")) {
				try {
					LeakingFieldMain.analyze(new JarFile(item));
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				LeakingFieldMain.analyze(item);
			}
		}
		long time2 = System.currentTimeMillis();
		
		int serialized = serialize(CLASSLOG, INHERILOG);
		printer.println("Analyzed: " + counter + " files, Leaking: "
				+ classes.size() + " files, After filtering: " + serialized
				+ " files stored!");
		printer.println("Offline analysis cost: " + (time2 - time1) + "ms");
	}

	public static int serialize(String cf, String inf) {
		int serialized = 0;
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(cf));
			for (String clazz : classes.keySet()) {
				serialized++;
				writer.write(clazz + " " + classes.get(clazz) + "\n");
			}
			writer.flush();
			writer.close();

			BufferedWriter writer2 = new BufferedWriter(new FileWriter(inf));
			for (String clazz : completeParentMap.keySet()) {
				writer2.write(clazz+" " + completeParentMap.get(clazz));
				writer2.write("\n");
			}
			writer2.flush();
			writer2.close();
		} catch (Exception e) {
		}
		return serialized;
	}

	public static Map<String, Boolean> deSerializeClasses(String cf)
			throws IOException {
		Map<String, Boolean> map = new HashMap<String, Boolean>();
		File file=new File(cf);
		if(!file.exists())
			return map;
		
		BufferedReader reader = new BufferedReader(new FileReader(cf));
		String line = reader.readLine();
		while (line != null) {
			String[] items = line.split(" ");
			map.put(items[0], Boolean.valueOf(items[1]));
			line = reader.readLine();
		}
		reader.close();
		return map;
	}

	public static Map<String, String> deSerializeInheritance(String inf)
			throws IOException {
		Map<String, String> map = new HashMap<String, String>();
		File file=new File(inf);
		if(!file.exists())
			return map;
		
		BufferedReader reader = new BufferedReader(new FileReader(inf));
		String line = reader.readLine();
		while (line != null) {
			String[] items = line.split(" ");
			map.put(items[0], items[1]);
			line = reader.readLine();
		}
		reader.close();
		return map;
	}

	public static void analyze(String dir) {
		File dirFile = new File(dir);
		List<File> workList = new ArrayList<File>();
		workList.add(dirFile);
		while (!workList.isEmpty()) {
			File curF = workList.remove(0);
			if (curF.getName().endsWith(".class")) {
				try {
					InputStream classFileInputStream = new FileInputStream(
							curF);
					ClassReader cr = new ClassReader(classFileInputStream);
					LeakingFieldsVisitor ca = new LeakingFieldsVisitor();
					cr.accept(ca, 0);
					classFileInputStream.close();
				} catch (Exception e) {

				}
			} else if (curF.isDirectory()) {
				for (File f : curF.listFiles())
					workList.add(f);
			}
		}
	}

	public static void analyze(JarFile f) {
		// System.out.println(f.getName());
		Enumeration<JarEntry> entries = f.entries();
		while (entries.hasMoreElements()) {
			JarEntry entry = entries.nextElement();
			String entryName = entry.getName();
			if (entryName.endsWith(".class")) {
				try {
					InputStream classFileInputStream = f.getInputStream(entry);
					ClassReader cr = new ClassReader(classFileInputStream);
					LeakingFieldsVisitor ca = new LeakingFieldsVisitor();
					// System.out.println(entryName);
					cr.accept(ca, 0);
					classFileInputStream.close();
				} catch (Exception e) {
					// swallow weird class files
				}
			}
		}
	}


}
