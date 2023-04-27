package com.crivano.jmodel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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

	public static String removeSingleQuotes(String str) {
		StringBuilder result = new StringBuilder();
		boolean insideQuote = false;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '\'') {
				insideQuote = !insideQuote;
			} else if (!insideQuote) {
				result.append(c);
			}
		}
		return result.toString();
	}

	public static String removeBuiltins(String str) {
		StringBuilder result = new StringBuilder();
		boolean insideIdentifier = false;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (c == '?' && isIdentifierStart(str, i)) {
				insideIdentifier = true;
			} else if (insideIdentifier && (!Character.isLowerCase(c) && !Character.isDigit(c) && c != '_')) {
				insideIdentifier = false;
			} else if (!insideIdentifier) {
				result.append(c);
			}
		}
		return result.toString();
	}

	private static boolean isIdentifierStart(String str, int index) {
		if (index + 1 >= str.length()) {
			return false;
		}
		char c = str.charAt(index + 1);
		return Character.isLowerCase(c) || c == '_';
	}

	public static List<String> getIdentifiers(String str) {
		List<String> identifiers = new ArrayList<>();
		boolean insideIdentifier = false;
		StringBuilder identifier = new StringBuilder();
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);
			if (insideIdentifier) {
				if (Character.isLetterOrDigit(c) || c == '_') {
					identifier.append(c);
				} else {
					insideIdentifier = false;
					if (identifier.length() > 0 && Character.isLowerCase(identifier.charAt(0))) {
						identifiers.add(identifier.toString());
					}
					identifier.setLength(0);
				}
			} else {
				if (Character.isLowerCase(c) || c == '_') {
					insideIdentifier = true;
					identifier.append(c);
				}
			}
		}
		if (insideIdentifier && identifier.length() > 0 && Character.isLowerCase(identifier.charAt(0))) {
			identifiers.add(identifier.toString());
		}
		return identifiers;
	}

	public static List<String> getVariableNames(String input) {
		input = Utils.removeSingleQuotes(input);
		input = Utils.removeBuiltins(input);
		List<String> expectedOutput = Arrays.asList("country", "myCountry", "tasty_food");
		return Utils.getIdentifiers(input);
	}

}