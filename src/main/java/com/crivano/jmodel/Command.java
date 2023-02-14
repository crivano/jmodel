package com.crivano.jmodel;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Command {
	String command;
	String expr;
	String var;
	String params;

	public Command(String s) {
		Pattern pattern = Pattern.compile(
				"\\{(?<command>(?>field |write |if |/if|for |/for))?(?<expr>.*?)(?<params>\\s*[a-zA-Z0-9_]+\\s*=\\s*[^=].*)?\\}$");
		Matcher m = pattern.matcher(s);
		if (m.find()) {
			command = Utils.sorn(m.group("command"));
			expr = Utils.sorn(m.group("expr"));
			params = Utils.sorn(m.group("params"));
		}

		if (command == null && expr != null && params == null) {
			if (expr.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
				// A single variable identifier should be mapped to a "campo" command
				command = "field";
				var = expr;
				expr = null;
				params = "var='" + this.var + "'";
			} else {
				// A single expression should be mapped to a "escrever" command
				command = "write";
				params = "value=" + expr;
			}
		}

	}

	public boolean isSelfContained() {
		return !isOpening() && !isClosing();
	}

	public boolean isClosing() {
		return command.startsWith("/");
	}

	public boolean isOpening() {
		return "if".equals(command) || "for".equals(command);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (isClosing()) {
			sb.append("[/@");
			sb.append(command.substring(1));
		} else {
			sb.append("[@");
			sb.append(command);
		}
		if (expr != null) {
			sb.append(" ");
			sb.append(expr);
			sb.append(" ");
		}
		if (params != null) {
			sb.append(" ");
			sb.append(params);
		}
		sb.append(isSelfContained() ? "/]" : "]");
		return sb.toString();
	}
}
