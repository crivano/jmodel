/*******************************************************************************
 * Copyright (c) 2006 - 2011 SJRJ.
 * 
 *     This file is part of SIGA.
 * 
 *     SIGA is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 * 
 *     SIGA is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 * 
 *     You should have received a copy of the GNU General Public License
 *     along with SIGA.  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.crivano.jmodel;

import java.io.InputStream;

import junit.framework.TestCase;

public class ExamplesTest extends TestCase {

	public ExamplesTest() throws Exception {
	}

	public void testAllExamples() throws Exception {
		try (InputStream stream = ExamplesTest.class.getClassLoader()
				.getResourceAsStream("com/crivano/jmodel/examples.md")) {
			String examples = Utils.convertStreamToString(stream);
			String[] lines = examples.split("\n");
			String md = null;
			String fm = null;
			for (String s : lines) {
				switch (s.toLowerCase().trim()) {
				case "```markdown":
					md = "";
					break;
				case "```freemarker":
					fm = "";
					break;
				case "```":
					if (md != null && fm != null) {
						md = md.replace("\r", "");
						fm = fm.replace("\r", "");
						String converted = Template.markdownToFreemarker(md);
						assertEquals(fm, converted);
						md = null;
						fm = null;
					}
					break;
				default:
					if (fm != null)
						fm += (fm.isEmpty() ? "" : "\n") + s;
					else if (md != null)
						md += (md.isEmpty() ? "" : "\n") + s;
				}
			}
		}
	}
}
