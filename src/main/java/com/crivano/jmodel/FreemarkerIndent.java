package com.crivano.jmodel;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Document.OutputSettings.Syntax;
import org.jsoup.parser.ParseSettings;
import org.jsoup.parser.Parser;

public class FreemarkerIndent {
	static Pattern patternBody = Pattern.compile("<body[^>]*>\\s*(.*?)\\s*</body>",
			Pattern.CASE_INSENSITIVE + Pattern.DOTALL);

	static Pattern patternRemoveBodyIndent = Pattern.compile("^    (.*)$", Pattern.MULTILINE);

	static Pattern patternUnmarshall = Pattern
			.compile("</?fm:indent\\s?/?>\\s*</?fm:cmd n=\"(\\w+)\" kind=\"(\\w+)\"\\s?/>");

	static Pattern patternUnindent = Pattern
			.compile("  (<fm:indent\\s?/>\\s*</?fm:cmd n=\"(\\w+)\" kind=\"unindent\"\\s?/>)");

	static Pattern patternAfterOpen = Pattern
			.compile("^(\\s*)(<!--fm-(?:open|selfcontained|unindent)=\"\\d+\"-->)\\s*([^\\s].*?)$", Pattern.MULTILINE);

	static Pattern patternBeforeClose = Pattern.compile(
			"^(\\s*)([^\\s].*?)\\s*(<!--fm-(?:close|selfcontained|unindent)=\"\\d+\"-->.*)$", Pattern.MULTILINE);

	static Pattern patternSingleTag = Pattern.compile(
			"^([ ]*)([^ ].*?)[ ]*(<!--fm-(?:open|close|selfcontained|unindent)=\"\\d+\"-->)[ ]*(.*)$",
			Pattern.MULTILINE);

	static Pattern patternMultipleTags = Pattern.compile(
			"^([ ]*)(.*<!--fm-(?:open|close|selfcontained|unindent)=\"\\d+\"-->)(.*<!--fm-(?:open|close|selfcontained|unindent)=\"\\d+\"-->.*)$",
			Pattern.MULTILINE);

	public static String bodyOnly(String s) {
		Matcher m = patternBody.matcher(s);
		if (m.find()) {
			String body = m.group(1);

			Matcher matcher = patternRemoveBodyIndent.matcher(body);
			StringBuffer output = new StringBuffer();
			while (matcher.find()) {
				String rep = matcher.group(1);
				matcher.appendReplacement(output, rep.replace("$", "\\$"));
			}
			matcher.appendTail(output);
			return output.toString();
		}
		return null;
	}

	public static String convertFtl2Html(String input, List<String> lftl) {
		String fmm = new FreemarkerMarker(input).addMarks();

		StringBuffer output = new StringBuffer();
		Matcher matcher = FreemarkerMarker.patternFMM.matcher(fmm);
		while (matcher.find()) {
			String ftl = matcher.group(1);
			lftl.add(ftl);
			boolean open = (ftl.startsWith("[#") || ftl.startsWith("[@")) && !ftl.endsWith("/]");
			boolean close = ftl.startsWith("[/");
			boolean unindent = false;
			String rep = "";

			if (open) {
				if (ftl.startsWith("[#else") || ftl.startsWith("[#case") || ftl.startsWith("[#default")
						|| ftl.startsWith("[#recover")) {
					open = false;
					unindent = true;
				}
				if (ftl.startsWith("[#break"))
					open = false;
				if (ftl.startsWith("[#--"))
					open = false;
			}

			String kind = null;
			if (unindent)
				kind = "unindent";
			else if (!open && !close)
				kind = "selfcontained";
			else if (open)
				kind = "open";
			else if (close)
				kind = "close";

			rep += "<";
			if (close)
				rep += "/";
			rep += "fm:indent";
			if (!open && !close)
				rep += "/";
			rep += ">";
			rep += "<fm:cmd n=\"" + lftl.size() + "\" kind=\"" + kind + "\"/>";

			// System.out.println("rep: " + rep);
			matcher.appendReplacement(output, rep);
		}
		matcher.appendTail(output);
		return output.toString();
	}

	private static String unmarshal(String input, List<String> lftl) {
		StringBuffer output = new StringBuffer();
		Matcher matcher = patternUnmarshall.matcher(input);
		while (matcher.find()) {
			String ftl = matcher.group(1);
			if (ftl != null) {
				String rep = lftl.get(Integer.valueOf(ftl) - 1);
				matcher.appendReplacement(output, rep.replace("$", "\\$"));
			}
		}
		matcher.appendTail(output);
		return output.toString();
	}

	public static String convertHtml2Ftl(String input, List<String> lftl) {
		String output = input;
		output = unmarshal(output, lftl);
		return output;
	}

	private static String multipleTags(String input) {
		StringBuffer output = new StringBuffer();
		Matcher matcher = patternMultipleTags.matcher(input);
		while (matcher.find()) {
			String spc = matcher.group(1);
			String open = matcher.group(2);
			String after = matcher.group(3);
			String rep = spc + open + "\n" + spc + after;
			matcher.appendReplacement(output, rep.replace("$", "\\$"));
		}
		matcher.appendTail(output);
		return output.toString();
	}

	private static String afterOpen(String input) {
		StringBuffer output = new StringBuffer();
		Matcher matcher = patternAfterOpen.matcher(input);
		while (matcher.find()) {
			String spc = matcher.group(1);
			String open = matcher.group(2);
			String after = matcher.group(3);
			String rep = spc + open + "\n" + spc + after;
			matcher.appendReplacement(output, rep.replace("$", "\\$"));
		}
		matcher.appendTail(output);
		return output.toString();
	}

	private static String beforeClose(String input) {
		StringBuffer output = new StringBuffer();
		Matcher matcher = patternBeforeClose.matcher(input);
		while (matcher.find()) {
			String spc = matcher.group(1);
			String before = matcher.group(2);
			String close = matcher.group(3);
			String rep = spc + before + "\n" + spc + close;
			matcher.appendReplacement(output, rep.replace("$", "\\$"));
		}
		matcher.appendTail(output);
		return output.toString();
	}

	private static String singleTag(String input) {
		StringBuffer output = new StringBuffer();
		Matcher matcher = patternSingleTag.matcher(input);
		while (matcher.find()) {
			String spc = matcher.group(1);
			String before = matcher.group(2);
			String tag = matcher.group(3);
			String after = matcher.group(4);
			String rep = "";
			if (before.trim().length() > 0)
				rep += spc + before + "\n";
			rep += spc + tag;
			if (after.trim().length() > 0)
				rep += "\n" + spc + after;
			matcher.appendReplacement(output, rep.replace("$", "\\$"));
		}
		matcher.appendTail(output);
		return output.toString();
	}

	private static String removeOddSpace(String input) {
		String lines[] = input.split("\n");
		StringBuffer sb = new StringBuffer();

		for (String s : lines) {
			if (sb.length() > 0)
				sb.append("\n");
			int len = 0;
			for (; len < s.length() && s.charAt(len) == ' '; len++)
				;
			if (len % 2 == 1)
				sb.append(s, 1, s.length());
			else
				sb.append(s);
		}
		return sb.toString();
	}

	private static String unindentFreemarkerTags(String input) {
		StringBuffer output = new StringBuffer();
		Matcher matcher = patternUnindent.matcher(input);
		while (matcher.find()) {
			String ftl = matcher.group(1);
			if (ftl != null) {
				matcher.appendReplacement(output, matcher.group(1));
			}
		}
		matcher.appendTail(output);
		return output.toString();
	}

	protected static String tidy(String s) throws UnsupportedEncodingException, IOException {
		Parser parser = Parser.xmlParser();
		parser.settings(new ParseSettings(true, true));
		Document document = parser.parseInput(s, "");

//	    final Document document = Jsoup.parse(s);
		document.outputSettings().prettyPrint(true);
		document.outputSettings().syntax(Syntax.xml);
		document.outputSettings().indentAmount(2);
		String html = document.body().html();
		return html;
	}

	public static String indent(String s) throws IOException {
		List<String> lftl = new ArrayList<>();
		s = convertFtl2Html(s, lftl);

		s = "<body>" + s + "</body>";

		String sResult = tidy(s);
//		sResult = bodyOnly(sResult);
		// if (true) return sResult;
		while (true) {
			String sLastResult = multipleTags(sResult);
			if (sResult.equals(sLastResult))
				break;
			else
				sResult = sLastResult;
		}

		sResult = singleTag(sResult);
		// sResult = removeOddSpace(sResult);
		// sResult = afterOpen(sResult);
		// sResult = beforeClose(sResult);
		sResult = unindentFreemarkerTags(sResult);
		sResult = sResult.replace("<br />", "<br/>");

		sResult = convertHtml2Ftl(sResult, lftl);
		return sResult;
	}

}
