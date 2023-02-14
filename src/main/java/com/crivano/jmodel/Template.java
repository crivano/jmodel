package com.crivano.jmodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class Template {

	public static String markdownToFreemarker(String input) {

		String mdWithCommandsInFreemarker = processCommands(input, (cmd) -> {
			String comando = cmd.command;
			if (comando == null)
				return "UNKNOWN_COMMAND";
			return cmd.toString();
		});

		List<String> lftl = new ArrayList<>();
		String txtWithPlaceholders = FreemarkerIndent.convertFtl2Html(mdWithCommandsInFreemarker, lftl);

		StringBuilder sb = new StringBuilder();

		sb.append("[@interview]");
		String PREFIX_CAMPO = "[@field ";
		for (String s : lftl) {
			if (s.startsWith(PREFIX_CAMPO)) {
				sb.append("\n  ");
				sb.append(s);
			}
		}
		sb.append("\n[/@interview]");

		// Change "campo" to "valor"
		for (int i = 0; i < lftl.size(); i++) {
			if (lftl.get(i).startsWith(PREFIX_CAMPO)) {
				lftl.set(i, "[@value " + lftl.get(i).substring(PREFIX_CAMPO.length()));
			}
		}

		String ftlInMarkdown = FreemarkerIndent.convertHtml2Ftl(txtWithPlaceholders, lftl);

		String ftlInHtml = markdownToHtml(ftlInMarkdown);

		sb.append("\n\n[@document]\n");
		sb.append(ftlInHtml);
		sb.append("[/@document]");

		try {
			String result = FreemarkerIndent.indent(sb.toString());
			result = result.replaceAll("\\s+\n", "\n");
			result = result.replace("[/@interview]\n[@document]", "[/@interview]\n\n[@document]");
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String markdownToHtml(String input) {
		// Convert markdown to html
		//
		MutableDataSet options = new MutableDataSet();

		options.set(Parser.EXTENSIONS, Arrays.asList(TablesExtension.create()));

		// uncomment to convert soft-breaks to hard breaks
		// options.set(HtmlRenderer.SOFT_BREAK, "<br />\n");
		Parser parser = Parser.builder(options).build();
		HtmlRenderer renderer = HtmlRenderer.builder(options).indentSize(2).build();
		// You can re-use parser and renderer instances
		Node document = parser.parse(input);
		String html = renderer.render(document);
		return html;
	}

	public interface ProcessCommandFunction {
		String modifyCommand(Command cmd);
	}

	private static Integer readCommand(String block, int startIndex) {
		int currPos = startIndex;
		int openBrackets = 0;
		boolean stillSearching = true;
		char waitForChar = '\0';

		while (stillSearching && currPos <= block.length()) {
			char currChar = block.charAt(currPos);

			if (waitForChar == '\0') {
				switch (currChar) {
				case '{':
					openBrackets++;
					break;
				case '}':
					openBrackets--;
					break;
				case '"':
				case '\'':
					waitForChar = currChar;
					break;
				}
			} else {
				if (currChar == waitForChar) {
					if (waitForChar == '"' || waitForChar == '\'') {
						if (block.charAt(currPos - 1) != '\\')
							waitForChar = '\0';
					} else {
						waitForChar = '\0';
					}
				} else if (currChar == '*') {
					if (block.charAt(currPos + 1) == '/')
						waitForChar = '\0';
				}
			}

			currPos++;
			if (openBrackets == 0) {
				stillSearching = false;
				return currPos;
			}
		}
		return null;
	}

	// Replace everything inside {} by the return of a call to func(string
	// found)
	//
	private static String processCommands(String template, ProcessCommandFunction func) {
		int pointer = 0;
		String s = "";
		int start;

		while (-1 != (start = template.indexOf("{", pointer))) {
			s += template.substring(pointer, start);
			Integer end = readCommand(template, start);
			if (end == null) {
				break;
			}
			String command = template.substring(start, end);
//            Command cmd = interpretCommand(command);
			Command cmd = new Command(command);
			String r = func.modifyCommand(cmd);
			if (r != null)
				s += r;

			pointer = end;
		}
		s += template.substring(pointer, template.length());
		return s;
	}
}
