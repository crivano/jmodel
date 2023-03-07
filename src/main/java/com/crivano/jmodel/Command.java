package com.crivano.jmodel;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Command {
	private static Pattern patternTemplateCommand = Pattern.compile(
			"\\{(?<command>/?[a-zA-Z][a-zA-Z0-9_]*)?(?<expr>.*?)(?<params>\\s*[a-zA-Z0-9_]+\\s*=\\s*[^=].*)?\\}$");

	private static Pattern patternFreemarkerCommand = Pattern.compile(
			"^\\[(?<command>(?>/?@[a-z]+))(?<expr>\\s+.*?)?(?<params>\\s*[a-zA-Z0-9_]+\\s*=\\s*[^=].*)?\\]$");

	private static Pattern patternSplitParams = Pattern.compile(
			"\\s*(?<name>[a-z][a-z0-9]+)\\s*=\\s*(?<value>.+?)\\s*(?=$|[a-z][a-z0-9]+\\s*=)");

	private static Set<String> commands = new HashSet<>(
			Arrays.asList("field", "print", "if", "/if", "for", "/for", "set"));

	String command;
	String expr;
	String var;
	String params;
	Map<String, String> mapParams;

	public Command(String s) {
		Matcher m = patternTemplateCommand.matcher(s);
		if (m.find()) {
			command = Utils.sorn(m.group("command"));
			expr = Utils.sorn(m.group("expr"));
			params = Utils.sorn(m.group("params"));

			if (command != null) {
				CommandEnum ce = CommandEnum.getCommandFromName(command);
				if (ce != null) {
					command = ce.ftlCommand;
				} else if (command.equals(command.toUpperCase())) {
					ce = CommandEnum.PRINT;
					expr = command;
					command = ce.ftlCommand;
				} else {
					ce = CommandEnum.FIELD;
					var = command;
					command = ce.ftlCommand;
					expr = null;
					params = "var='" + this.var + "'" + (params != null ? " " + params : "");
				}
			}
		} else {
			m = patternFreemarkerCommand.matcher(s);
			if (m.find()) {
				command = Utils.sorn(m.group("command"));
				expr = Utils.sorn(m.group("expr"));
				params = Utils.sorn(m.group("params"));
			}
		}

//		if (command == null && expr != null && params == null) {
//			if (expr.matches("^[a-zA-Z][a-zA-Z0-9_]*$")) {
//				// A single variable identifier should be mapped to a "campo" command
//				command = "@field";
//				var = expr;
//				expr = null;
//				params = "var='" + this.var + "'";
//			} else {
//				// A single expression should be mapped to a "escrever" command
//				command = "@print";
//				params = "value=" + expr;
//			}
//		}

		mapParams = new TreeMap<>();
		if (params != null) {
			final Matcher matcher = patternSplitParams.matcher(params);

			while (matcher.find()) {
				String name = Utils.sorn(matcher.group("name"));
				String value = Utils.sorn(matcher.group("value"));
				if (name != null && value != null)
					mapParams.put(name, value);
			}
		}
	}

	public String getFieldVarAndIndex() {
		if (!CommandEnum.FIELD.ftlCommand.equals(command) || !mapParams.containsKey("var"))
			return null;
		String s = command + "|" + mapParams.get("var");
		if (mapParams.containsKey("index"))
			s += "|" + mapParams.get("index");
		return s;
	}

	public boolean isSelfContained() {
		return !isOpening() && !isClosing();
	}

	public boolean isClosing() {
		return command.startsWith("/");
	}

	public boolean isOpening() {
		return CommandEnum.GROUP_BEGIN.ftlCommand.equals(command) || CommandEnum.IF_BEGIN.ftlCommand.equals(command)
				|| CommandEnum.FOR_BEGIN.ftlCommand.equals(command);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (isClosing()) {
			sb.append("[/");
			sb.append(command.substring(1));
		} else {
			sb.append("[");
			sb.append(command);
		}
		if (expr != null) {
			sb.append(" expr=(");
			sb.append(expr);
			sb.append(")");
		}
		if (params != null) {
			sb.append(" ");
			sb.append(params);
		}

		if (CommandEnum.FOR_BEGIN.ftlCommand.equals(command))
			sb.append(" ; index");
		sb.append(isSelfContained() ? "/]" : "]");
		return sb.toString();
	}
}
