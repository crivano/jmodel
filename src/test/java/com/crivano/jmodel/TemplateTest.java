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

import junit.framework.TestCase;

public class TemplateTest extends TestCase {

	public TemplateTest() throws Exception {
	}

	public void testOneField() throws Exception {
		String s = Template.markdownToFreemarker(null, "Hi [@field var='name'/]!");
		assertEquals(""
				+ "[@interview]\n"
				+ "  [@field var='name'/]\n"
				+ "[/@interview]\n"
				+ "\n"
				+ "[@document]\n"
				+ "  <p>Hi\n"
				+ "    [@value var='name'/]!</p>\n"
				+ "[/@document]", s);
	}
}
