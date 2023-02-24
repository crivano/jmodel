package com.crivano.jmodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class Template {

	private static final String MARKDOWN_DOCUMENT = "<!-- MARKDOWN-DOCUMENT -->";

	private static final String MARKDOWN_DESCRIPTION = "<!-- MARKDOWN-DESCRIPTION -->";

	public static String markdownToFreemarker(String mdDescription, String mdDocument) {

		if (mdDescription == null)
			mdDescription = "";
		if (mdDocument == null)
			mdDocument = "";

		String input = MARKDOWN_DESCRIPTION + "\n\n" + mdDescription + "\n\n" + MARKDOWN_DOCUMENT + "\n\n"
				+ mdDocument;

		String mdWithCommandsInFreemarker = processCommands(input, (cmd) -> {
			String comando = cmd.command;
			if (comando == null)
				return "UNKNOWN_COMMAND";
			return cmd.toString();
		});

		List<String> lftl = new ArrayList<>();
		String txtWithPlaceholders = FreemarkerIndent.convertFtl2Html(mdWithCommandsInFreemarker, lftl);

		StringBuilder sb = new StringBuilder();

		sb.append(buildInterview(lftl));

		// Change "campo" to "valor"
		String PREFIX_CAMPO = "[@field ";
		for (int i = 0; i < lftl.size(); i++) {
			if (lftl.get(i).startsWith(PREFIX_CAMPO)) {
				lftl.set(i, "[@value " + lftl.get(i).substring(PREFIX_CAMPO.length()));
			}
		}

		String ftlInMarkdown = FreemarkerIndent.convertHtml2Ftl(txtWithPlaceholders, lftl);

		String ftlInHtml = markdownToHtml(ftlInMarkdown);

		// Split de HTML to identify the description and the document
		String html[] = ftlInHtml.split(MARKDOWN_DOCUMENT);
		html[0] = Utils.sorn(html[0].replace(MARKDOWN_DESCRIPTION, ""));
		html[1] = Utils.sorn(html[1]);

		if (html[0] != null) {
			sb.append("\n\n[@description]\n");
			sb.append(html[0]);
			sb.append("[/@description]");
		}
		if (html[1] != null) {
			sb.append("\n\n[@document]\n");
			sb.append(html[1]);
			sb.append("[/@document]");
		}

		try {
			String result = sb.toString();
			result = freemarkerReposition(result);
			result = FreemarkerIndent.indent(result);
			result = result.replaceAll("\\s+\n", "\n");
			result = result.replace("]\n[@description]", "]\n\n[@description]");
			result = result.replace("]\n[@document]", "]\n\n[@document]");
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String buildInterview(List<String> lftl) {
		StringBuilder sb = new StringBuilder();

		Set<String> fields = new HashSet<>();
		sb.append("[@interview]");
		String INTERVIEW_COMMANDS[] = new String[] { "[@field ", "[@if ", "[/@if]", "[@for ", "[/@for]" };
		for (int i = 0; i < lftl.size(); i++) {
			String s = lftl.get(i);
			String next = (i < lftl.size() - 1) ? lftl.get(i + 1) : null;
			for (String prefix : INTERVIEW_COMMANDS) {

				// Skip if field already seen
				Command cmd = new Command(s);
				String fieldVarAndIndex = cmd.getFieldVarAndIndex();
				if (fieldVarAndIndex != null)
					if (fields.contains(fieldVarAndIndex))
						continue;
					else
						fields.add(fieldVarAndIndex);

				// Skip empty IFs and FORs
				if (s.startsWith("[@if ") && "[/@if]".equals(next)
						|| s.startsWith("[@for ") && "[/@for]".equals(next)) {
					i++;
					continue;
				}

				if (s.startsWith(prefix)) {
					sb.append("\n  ");
					sb.append(s);
				}
			}
		}
		sb.append("\n[/@interview]");
		return sb.toString();
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

	final static Pattern patternFreemarkerRepositionUp = Pattern.compile(
			"\\s*(?<html>(?:<p>|<tr>\\s*<td>))\\s*(?<freemarker>\\{\\{fm\\}\\}\\[@(?:if|for) .+?\\{\\{\\/fm\\}\\})\\s*",
			Pattern.MULTILINE);

	final static Pattern patternFreemarkerRepositionDown = Pattern.compile(
			"\\s*(?<freemarker>\\{\\{fm\\}\\}\\[/@(?:if|for)]\\{\\{\\/fm\\}\\})\\s*(?<html>(?:</p>|</td>\\s*</tr>))\\s*",
			Pattern.MULTILINE);

	// Acredito que a melhor estratégia aqui seja utilizar o parser para marcar qual
	// open se relaciona com qual close. Depois, usar uma expressao regular que
	// identifique os pares, avalie o que existe antes do open e depois do close e,
	// faça o reposicionamento do bloco todo. Isso seria bem mais confiável. Mas
	// também é bem mais difícil. Vou deixar assim por enquanto.
	public static String freemarkerReposition(String s) {
		FreemarkerMarker fmm = new FreemarkerMarker(s);
		s = fmm.addMarks();
		final String subst = "$2$1";

		s = patternFreemarkerRepositionUp.matcher(s).replaceAll(subst);
		s = patternFreemarkerRepositionDown.matcher(s).replaceAll(subst);
		return FreemarkerMarker.removeMarks(s);
	}
}
