package com.crivano.jmodel;

import java.util.Collections;
import java.util.stream.Collectors;

public class Utils {

	public static String sorn(String s) {
		if (s == null)
			return null;
		if (s.trim().length() == 0)
			return null;
		return s.trim();
	}

	public static String convertStreamToString(java.io.InputStream is) {
		@SuppressWarnings("resource")
		java.util.Scanner s = new java.util.Scanner(is, "UTF-8").useDelimiter("\\A");
		return s.hasNext() ? s.next() : "";
	}

	public static String addLineNumbers(String template, Integer line, Integer endLine, Integer column,
			Integer endColumn) {

		String a[] = escapeHTML(template).replace("\r", "").trim().split("\n");
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < a.length; i++) {
			if (i > 0)
				sb.append("\n");
			sb.append(String.format("%03d", i + 1));
			if (between(i + 1, line, endLine))
				sb.append("> ");
			else
				sb.append("  ");
			sb.append(a[i]);
			if (between(i + 1, line, endLine) && column != null && column != 0 && endColumn != null) {
				sb.append("\n");
				sb.append("    ");
				sb.append(String.join("", Collections.nCopies(column, " ")));
				sb.append(String.join("", Collections.nCopies(endColumn - column + 1, "^")));
			}
			
		}
		return sb.toString();
	}

	private static boolean between(int i, Integer from, Integer to) {
		if (from == null)
			return false;
		if (to == null)
			return i == from;
		return i >= from && i <= to;
	}

	public static String escapeHTML(String str) {
		return str.codePoints()
				.mapToObj(c -> c > 127 || "\"'<>&".indexOf(c) != -1 ? "&#" + c + ";" : new String(Character.toChars(c)))
				.collect(Collectors.joining());
	}

}