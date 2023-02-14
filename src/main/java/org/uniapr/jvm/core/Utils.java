/*
 * Copyright (C) Illinois - All Rights Reserved.
 * Unauthorized copying of this file via any medium is
 * strictly prohibited.
 * This code base is proprietary and confidential.
 * Designed by the UniAPR team.
 */

package org.uniapr.jvm.core;

import java.io.FileWriter;
import java.io.PrintWriter;

public class Utils
{
	public static void log(String s) {
		try {
			FileWriter fileWriter = new FileWriter("log.log", true); // Set true
																		// for
																		// append
																		// mode
			PrintWriter printWriter = new PrintWriter(fileWriter);
			printWriter.println(s); // New line
			printWriter.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
