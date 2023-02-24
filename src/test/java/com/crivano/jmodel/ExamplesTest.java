package com.crivano.jmodel;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import junit.framework.ComparisonFailure;

@RunWith(Parameterized.class)
public class ExamplesTest {
	@Parameters(name = "{index}: {1} ({0})")
	public static Iterable<Object[]> data() throws IOException {
		List<Object[]> d = new ArrayList<>();

		String filename = "examples.md";
		try (InputStream stream = ExamplesTest.class.getClassLoader()
				.getResourceAsStream("com/crivano/jmodel/" + filename)) {
			String examples = Utils.convertStreamToString(stream);
			String[] lines = examples.split("\n");
			String title = null;
			String mdDescription = null;
			String mdDocument = null;
			String fm = null;
			int i = 1;
			int ln = 0;
			for (String s : lines) {
				if (s.startsWith("### "))
					title = s.substring(4).trim();

				switch (s.toLowerCase().trim()) {
				case "```markdown description":
					mdDescription = "";
					break;
				case "```markdown":
				case "```markdown document":
					mdDocument = "";
					break;
				case "```freemarker":
					fm = "";
					ln = i + 1;
					break;
				case "```":
					if (mdDocument != null && fm != null) {
						if (mdDescription != null)
							mdDescription = mdDescription.replace("\r", "");
						mdDocument = mdDocument.replace("\r", "");
						fm = fm.replace("\r", "");
						String filenameAndLineNumber = filename + ":" + ln;

						Object o[] = new Object[] { filenameAndLineNumber, title, mdDescription, mdDocument, fm };
						d.add(o);

						mdDescription = null;
						mdDocument = null;
						fm = null;
						title = null;
					}
					break;
				default:
					if (fm != null)
						fm += (fm.isEmpty() ? "" : "\n") + s;
					else if (mdDocument != null)
						mdDocument += (mdDocument.isEmpty() ? "" : "\n") + s;
					else if (mdDescription != null)
						mdDescription += (mdDescription.isEmpty() ? "" : "\n") + s;
				}
				i++;
			}

		}
		return d;
	}

	private String filenameAndLineNumber;
	private String title;
	private String mdDescription;
	private String mdDocument;
	private String fm;

	public ExamplesTest(String filenameAndLineNumber, String title, String mdDescription, String mdDocument,
			String fm) {
		this.filenameAndLineNumber = filenameAndLineNumber;
		this.title = title;
		this.mdDescription = mdDescription;
		this.mdDocument = mdDocument;
		this.fm = fm;
	}

	@Test
	public void test() {
		String converted = Template.markdownToFreemarker(mdDescription, mdDocument);
		try {
			String suffix = "\n\n---\nat " + filenameAndLineNumber;
			assertEquals(fm + suffix, converted + suffix);
		} catch (ComparisonFailure e) {
			System.err.println(e.getClass().getName() + " at (" + filenameAndLineNumber + ")");
			throw e;
		}
	}
}