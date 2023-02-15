package com.crivano.jmodel;

import java.io.IOException;
import java.util.ArrayList;

import junit.framework.TestCase;

public class IndentTest extends TestCase {

	public void testIndentacao() throws Exception {
		assertEquals("oi", FreemarkerIndent.indent("oi"));

		assertEquals("[#entrevista]\n[/#entrevista]", FreemarkerIndent.indent("[#entrevista][/#entrevista]"));

		assertEquals("[#if]oi\n[/#if]", FreemarkerIndent.indent("[#if]oi[/#if]"));
	}

	public void testIndentacaoUnindent() throws Exception {
		assertEquals("[#if]\n[#else]\n[/#if]", FreemarkerIndent.indent("[#if][#else][/#if]"));
	}

	public void testIndentacaoUnindentComplexo() throws Exception {
		assertEquals("[#if]\n  [@br/]\n[#else]\n  [@br/]\n[/#if]",
				FreemarkerIndent.indent("[#if][@br/][#else][@br/][/#if]"));
	}

	public void testIndentacaoComentarios() throws Exception {
		assertEquals("<!--Teste-->oi", FreemarkerIndent.indent("<!--Teste-->oi"));
	}

	public void testIndentacaoInlineIf() throws Exception {
		assertEquals(""
				+ "<div>\n"
				+ "  oi1\n"
				+ "  [#if]oi2\n"
				+ "  [/#if]oi3\n"
				+ "</div>", FreemarkerIndent.indent("<div>oi1[#if]oi2[/#if]oi3</div>"));
	}

	public void testIndentacaoProblemaDe1CharAMais() throws Exception {
		assertEquals(""
				+ "<div>\n"
				+ "   oi1 \n"
				+ "  [#if] oi2 \n"
				+ "  [/#if] oi3 \n"
				+ "</div>", FreemarkerIndent.indent("<div>\n  oi1\n  [#if]\n    oi2\n  [/#if]\n  oi3\n</div>"));
	}

	public void testIndentacaoDentroDeDiv() throws Exception {
		assertEquals(""
				+ "<div>\n"
				+ "  [#if]oi\n"
				+ "  [/#if]\n"
				+ "</div>", FreemarkerIndent.indent("<div>[#if]oi[/#if]</div>"));
	}

	public void testFtlParser() {
		assertEquals("oi", new FreemarkerMarker("oi").addMarks());
		assertEquals("{{fm}}[#--comment--]{{/fm}}", new FreemarkerMarker("[#--comment--]").addMarks());
		assertEquals("{{fm}}[#teste]{{/fm}}", new FreemarkerMarker("[#teste]").addMarks());
	}

	public void testFMSkip() throws IOException {
		assertEquals("[#assign pess = func.pessoa(.vars['prop' + '_pessoaSel.id']?number) /]",
				FreemarkerIndent.indent("[#assign pess = func.pessoa(.vars['prop' + '_pessoaSel.id']?number) /]"));

		assertEquals("[#assign p = .vars[.vars['x']] /]", FreemarkerIndent.indent("[#assign p = .vars[.vars['x']] /]"));

	}

	public void testConvertFtl2Html() {
		assertEquals("<fm:indent><fm:cmd n=\"1\" kind=\"open\"/>",
				FreemarkerIndent.convertFtl2Html("[#entrevista]", new ArrayList<String>()));
		assertEquals("</fm:indent><fm:cmd n=\"1\" kind=\"close\"/>",
				FreemarkerIndent.convertFtl2Html("[/#entrevista]", new ArrayList<String>()));
		assertEquals("<fm:indent/><fm:cmd n=\"1\" kind=\"selfcontained\"/>",
				FreemarkerIndent.convertFtl2Html("[#entrevista/]", new ArrayList<String>()));
	}

	public void testConvertFtl2Html2Ftl() {
		ArrayList<String> lftl = new ArrayList<String>();
		String ftl = "[#test]";
		String html = FreemarkerIndent.convertFtl2Html(ftl, lftl);
		String ftl2 = FreemarkerIndent.convertHtml2Ftl(html, lftl);
		assertEquals(ftl, ftl2);
	}
}
