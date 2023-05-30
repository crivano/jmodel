package com.crivano.jmodel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vladsch.flexmark.ext.tables.TablesExtension;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class Template {

	private static final String MARKDOWN_DOCUMENT = "<!-- MARKDOWN-DOCUMENT -->";
	private static final String MARKDOWN_DESCRIPTION = "<!-- MARKDOWN-DESCRIPTION -->";
	private static final String MARKDOWN_HOOK = "<!-- MARKDOWN-HOOK -->";

	final static Pattern patternFreemarkerRepositionUp = Pattern.compile(
			"\\s*(?<html>(?:<p>|<tr>\\s*<td(?:\\s+align=\\\"[a-z]+\\\")?>))\\s*(?<freemarker>\\{\\{fm\\}\\}\\[@(?:if|for) .+?\\{\\{\\/fm\\}\\})\\s*",
			Pattern.MULTILINE);

	final static Pattern patternFreemarkerRepositionDown = Pattern.compile(
			"\\s*(?<freemarker>\\{\\{fm\\}\\}\\[/@(?:if|for)]\\{\\{\\/fm\\}\\})\\s*(?<html>(?:</p>|</td>\\s*</tr>))\\s*",
			Pattern.MULTILINE);

	final static Pattern patternFreemarkerEmptyTopLevelMacros = Pattern.compile(
			"\\[@(?:interview|description|document)\\]\\s*\\[/@(?:interview|description|document)\\]\\s*",
			Pattern.MULTILINE);

	final static Pattern patternFreemarkerDescriptionWithin = Pattern.compile(
			"\\{description\\}(?<description>.+)\\{/description\\}\\s*",
			Pattern.MULTILINE | Pattern.DOTALL);

	final static Pattern patternFreemarkerHookWithin = Pattern.compile(
			"(?<hook>\\{hook ?(?<params>[^}]*)\\}.+\\{/hook\\})\\s*",
			Pattern.MULTILINE | Pattern.DOTALL);

	public static String markdownToFreemarker(String mdDescription, String mdDocument, String mdHook) {

		if (mdDescription == null)
			mdDescription = "";
		if (mdDocument == null)
			mdDocument = "";
		if (mdHook == null)
			mdHook = "";

		// Remove description from within the document markdown
		Matcher mDescr = patternFreemarkerDescriptionWithin.matcher(mdDocument);
		while (mDescr.find()) {
			String descr = Utils.sorn(mDescr.group("description"));
			if (descr != null)
				mdDescription += descr;
		}
		mdDocument = mDescr.replaceAll("");

		// Remove description from within the document markdown
		Matcher mHook = patternFreemarkerHookWithin.matcher(mdDocument);
		while (mHook.find()) {
			String hook = Utils.sorn(mHook.group("hook"));
			if (hook != null)
				mdHook += hook;
		}
		mdDocument = mHook.replaceAll("");

		String input = MARKDOWN_DESCRIPTION + "\n\n" + mdDescription + "\n\n" + MARKDOWN_DOCUMENT + "\n\n"
				+ mdDocument + "\n\n" + MARKDOWN_HOOK + "\n\n" + mdHook;

		String mdWithCommandsInFreemarker = processCommands(input, (cmd) -> {
			String comando = cmd.command;
			if (comando == null)
				return "UNKNOWN_COMMAND";
			return cmd.toString();
		});

		List<String> lftl = new ArrayList<>();
		String txtWithPlaceholders = FreemarkerIndent.convertFtl2Html(mdWithCommandsInFreemarker, lftl);

		StringBuilder sb = new StringBuilder();

		sb.append(buildHead(lftl));
		sb.append(buildInterview(lftl));

		// Change "field" to "value"
		for (int i = 0; i < lftl.size(); i++) {
			if (lftl.get(i).startsWith(CommandEnum.PREFIX_FIELD)) {
				lftl.set(i, CommandEnum.PREFIX_VALUE + lftl.get(i).substring(CommandEnum.PREFIX_FIELD.length()));
			}
		}

		// Remove "@group" and "/@group"
		for (int i = 0; i < lftl.size(); i++) {
			String ftl = lftl.get(i);
			if (ftl.startsWith(CommandEnum.PREFIX_GROUP_BEGIN) || ftl.startsWith(CommandEnum.PREFIX_GROUP_END)) {
				lftl.set(i, "");
			}
		}

		String ftlInMarkdown = FreemarkerIndent.convertHtml2Ftl(txtWithPlaceholders, lftl);

		// The hooks should not be subjected to que markdown parser
		String[] markdownSplit = Utils.sorn(ftlInMarkdown).split(MARKDOWN_HOOK);
		String ftlInHtml = markdownToHtml(markdownSplit[0]) + "\n\n" + MARKDOWN_HOOK + "\n\n"
				+ (markdownSplit.length == 1 ? "" : markdownSplit[1]);

		// Split de HTML to identify the description and the document
		String html[] = ftlInHtml.split(MARKDOWN_DOCUMENT);
		String htmlDescription = Utils.sorn(html[0].replace(MARKDOWN_DESCRIPTION, ""));
		html = Utils.sorn(html[1]).split(MARKDOWN_HOOK);
		String htmlDocument = Utils.sorn(html[0].replace(MARKDOWN_DOCUMENT, ""));
		String htmlHook = (html.length == 1 ? "" : Utils.sorn(html[1]));

		if (htmlDescription != null) {
			sb.append("\n\n[@description]\n");
			sb.append(htmlDescription);
			sb.append("[/@description]");
		}
		if (htmlDocument != null) {
			sb.append("\n\n[@document]\n");
			sb.append(htmlDocument);
			sb.append("[/@document]");
		}

		if (htmlHook != null) {
			sb.append("\n\n");
			sb.append(htmlHook);
		}

		try {
			String result = sb.toString();

			// Remove empty @interview, @description ou @document
			result = patternFreemarkerEmptyTopLevelMacros.matcher(result).replaceAll("");

			result = freemarkerReposition(result);
			result = FreemarkerIndent.indent(result);
			result = result.replaceAll("\\s+\n", "\n");
			result = result.replaceAll("\\]\\s*\\[@interview\\]", "]\n\n[@interview]");
			result = result.replaceAll("\\]\\s*\\[@description\\]", "]\n\n[@description]");
			result = result.replaceAll("\\]\\s*\\[@document\\]", "]\n\n[@document]");
			result = result.replaceAll("\\]\\s*\\[@hook", "]\n\n[@hook");
			return result;
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static String buildHead(List<String> lftl) {
		StringBuilder sb = new StringBuilder();

		Set<String> fields = new HashSet<>();
		String HEAD_COMMANDS[] = new String[] { CommandEnum.PREFIX_SET };
		for (int i = 0; i < lftl.size(); i++) {
			String s = lftl.get(i);
			String next = (i < lftl.size() - 1) ? lftl.get(i + 1) : null;
			for (String prefix : HEAD_COMMANDS) {

				if (s.startsWith(prefix)) {
					sb.append("\n  ");
					if (s.startsWith(CommandEnum.PREFIX_SET))
						s = "[#assign " + s.substring(6);
					sb.append(s);

					// Remove command
					lftl.set(i, "");
				}
			}
		}
		return sb.toString();
	}

	private static String buildInterview(List<String> lftl) {
		StringBuilder sb = new StringBuilder();

		Set<String> fields = new HashSet<>();
		sb.append("[@interview]");

		List<String> l = new ArrayList<>();
		for (String ftl : lftl)
			for (String prefix : CommandEnum.INTERVIEW_COMMANDS)
				if (ftl.startsWith(prefix))
					l.add(ftl);

		for (int i = 0; i < l.size(); i++) {
			String s = l.get(i);
			String next = (i < l.size() - 1) ? l.get(i + 1) : null;
			// Skip if field already seen
			Command cmd = new Command(s);
			String fieldVarAndIndex = cmd.getFieldVarAndIndex();
			if (fieldVarAndIndex != null)
				if (fields.contains(fieldVarAndIndex))
					continue;
				else
					fields.add(fieldVarAndIndex);

			// Skip empty IFs and FORs
			if (CommandEnum.IF_BEGIN.match(s) && CommandEnum.IF_END.match(next)
					|| CommandEnum.FOR_BEGIN.match(s) && CommandEnum.FOR_END.match(next)) {
				i++;
				continue;
			}

			sb.append("\n  ");
			sb.append(s);
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

	// Acredito que a melhor estratégia aqui seja utilizar o parser para marcar qual
	// open se relaciona com qual close. Depois, usar uma expressao regular que
	// identifique os pares, avalie o que existe antes do open e depois do close e,
	// faça o reposicionamento do bloco todo. Isso seria bem mais confiável. Mas
	// também é bem mais difícil. Vou deixar assim por enquanto.
	public static String freemarkerReposition(String s) {
		FreemarkerMarker fmm = new FreemarkerMarker(s);
		s = fmm.addMarks();
		s = fmm.quote(s);
		final String subst = "$2$1";

		s = patternFreemarkerRepositionUp.matcher(s).replaceAll(subst);
		s = patternFreemarkerRepositionDown.matcher(s).replaceAll(subst);
		s = FreemarkerMarker.removeMarks(s);
		return fmm.unquote(s);
	}
}
