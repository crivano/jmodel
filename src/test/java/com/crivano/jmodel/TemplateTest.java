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
		String s = Template.markdownToFreemarker("Hi [@field var='name'/]!");
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

	public void testOneAutoField() throws Exception {
		String s = Template.markdownToFreemarker("Hi {name}!");
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

	public void testOneAutoFieldWithOptions() throws Exception {
		String s = Template.markdownToFreemarker("Hi {field var='name' options='Foo;Bar'}!");
		assertEquals(""
				+ "[@interview]\n"
				+ "  [@field var='name' options='Foo;Bar'/]\n"
				+ "[/@interview]\n"
				+ "\n"
				+ "[@document]\n"
				+ "  <p>Hi\n"
				+ "    [@value var='name' options='Foo;Bar'/]!</p>\n"
				+ "[/@document]", s);
	}

	public void testTwoAutoFields() throws Exception {
		String s = Template.markdownToFreemarker(""
				+ "Country: {field var='country' optios='Brazil;Argentina' refresh='country'}\n"
				+ "\n"
				+ "Gender: {field var='gender' optios='Male;Female' refresh='gender'}");
		assertEquals(""
				+ "[@interview]\n"
				+ "  [@field var='country' optios='Brazil;Argentina' refresh='country'/]\n"
				+ "  [@field var='gender' optios='Male;Female' refresh='gender'/]\n"
				+ "[/@interview]\n"
				+ "\n"
				+ "[@document]\n"
				+ "  <p>Country:\n"
				+ "    [@value var='country' optios='Brazil;Argentina' refresh='country'/]</p>\n"
				+ "  <p>Gender:\n"
				+ "    [@value var='gender' optios='Male;Female' refresh='gender'/]</p>\n"
				+ "[/@document]", s);
	}

	public void testIf() throws Exception {
		String s = Template.markdownToFreemarker(""
				+ "Country: {field var='country' options='Brazil;Argentina' refresh='country'}\n"
				+ "\n"
				+ "You {if country == 'Brazil' depend='country'}didn't{/if} win the WorldCup!");
		assertEquals(""
				+ "[@interview]\n"
				+ "  [@field var='country' options='Brazil;Argentina' refresh='country'/]\n"
				+ "[/@interview]\n"
				+ "\n"
				+ "[@document]\n"
				+ "  <p>Country:\n"
				+ "    [@value var='country' options='Brazil;Argentina' refresh='country'/]</p>\n"
				+ "  <p>You\n"
				+ "    [@if country == 'Brazil'  depend='country']didn't\n"
				+ "    [/@if] win the WorldCup!</p>\n"
				+ "[/@document]", s);
	}

	public void testIfRepositionAroundParagraph() throws Exception {
		String s = Template.markdownToFreemarker(""
				+ "Country: {field var='country' options='Brazil;Argentina' refresh='country'}\n"
				+ "\n"
				+ "{if country == 'Brazil' depend='country'}State: {field var='state' options='Rio de Janeiro;São Paulo'}{/if}");
		assertEquals(""
				+ "[@interview]\n"
				+ "  [@field var='country' options='Brazil;Argentina' refresh='country'/]\n"
				+ "  [@field var='state' options='Rio de Janeiro;São Paulo'/]\n"
				+ "[/@interview]\n"
				+ "\n"
				+ "[@document]\n"
				+ "  <p>Country:\n"
				+ "    [@value var='country' options='Brazil;Argentina' refresh='country'/]</p>\n"
				+ "  [@if country == 'Brazil'  depend='country']\n"
				+ "    <p>State:\n"
				+ "      [@value var='state' options='Rio de Janeiro;São Paulo'/]</p>\n"
				+ "  [/@if]\n"
				+ "[/@document]", s);
	}

}
