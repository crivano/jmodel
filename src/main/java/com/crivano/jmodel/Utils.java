package com.crivano.jmodel;

public class Utils {

	public static String sorn(String s) {
		if (s == null)
			return null;
		if (s.trim().length() == 0)
			return null;
		return s.trim();
	}

}