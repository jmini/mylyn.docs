/*******************************************************************************
 * Copyright (c) 2007, 2011 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *     Jeremie Bresson - initial Google WikiSyntax support (bug 291239)
 *******************************************************************************/
package org.eclipse.mylyn.wikitext.googlewikisyntax.core;

import java.io.IOException;

import junit.framework.TestCase;

import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.tests.TestUtil;

/**
 * Test for {@link GoogleWikiSyntaxLanguage} based on the syntax described at <a
 * href="http://code.google.com/p/support/wiki/WikiSyntax">http://code.google.com/p/support/wiki/WikiSyntax</a>
 * 
 * @author Jeremie Bresson
 * @see GoogleWikiSyntaxLanguage
 */
public class GoogleWikiSyntaxLanguageTest extends TestCase {

	private MarkupParser parser;

	private GoogleWikiSyntaxLanguage markupLanaguage;

	@Override
	public void setUp() throws Exception {
		super.setUp();
		initParser();
	}

	private void initParser() throws IOException {
		parser = new MarkupParser();
		markupLanaguage = new GoogleWikiSyntaxLanguage();
		parser.setMarkupLanguage(markupLanaguage);
	}

	public void testTypefaceItalic() {
		String html = parser.parseToHtml("_italic_");
		TestUtil.println("HTML: \n" + html);
		assertTrue(html.contains("<i>italic</i>"));
	}

	public void testTypefaceBold() {
		String html = parser.parseToHtml("*bold*");
		TestUtil.println("HTML: \n" + html);
		assertTrue(html.contains("<strong>bold</strong>"));
	}

	public void testTypefaceCode1() {
		String html = parser.parseToHtml("`code`");
		TestUtil.println("HTML: \n" + html);
		assertTrue(html.contains("<tt>code</tt>"));
	}

	public void testTypefaceCode2() {
		String html = parser.parseToHtml("{{{code}}}");
		TestUtil.println("HTML: \n" + html);
		assertTrue(html.contains("<tt>code</tt>"));
	}

	public void testTypefaceSuperScript() {
		String html = parser.parseToHtml("^super^script");
		TestUtil.println("HTML: \n" + html);
		assertTrue(html.contains("<sup>super</sup>script"));
	}

	public void testTypefaceSubScript() {
		String html = parser.parseToHtml(",,sub,,script");
		TestUtil.println("HTML: \n" + html);
		assertTrue(html.contains("<sub>sub</sub>script"));
	}

	public void testTypefaceStrikeout() {
		String html = parser.parseToHtml("~~strikeout~~");
		TestUtil.println("HTML: \n" + html);
		assertTrue(html.contains("<span style=\"text-decoration: line-through\">strikeout</span>"));
	}

	public void testTypefaceBoldInItalics() {
		String html = parser.parseToHtml("_*bold* in italics_");
		TestUtil.println("HTML: \n" + html);
		assertTrue(html.contains("<i><strong>bold</strong> in italics</i>"));
	}

	public void testTypefaceItalicsInBold() {
		String html = parser.parseToHtml("*_italics_ in bold*");
		TestUtil.println("HTML: \n" + html);
		assertTrue(html.contains("<strong><i>italics</i> in bold</strong>"));
	}

	public void testTypefaceStrikeWorksToo() {
		String html = parser.parseToHtml("*~~strike~~ works too*");
		TestUtil.println("HTML: \n" + html);
		assertTrue(html.contains("<strong><span style=\"text-decoration: line-through\">strike</span> works too</strong>"));
	}

	public void testTypefaceAsWellAsThisWayRound() {
		String html = parser.parseToHtml("~~as well as _this_ way round~~");
		TestUtil.println("HTML: \n" + html);
		assertTrue(html.contains("<span style=\"text-decoration: line-through\">as well as <i>this</i> way round</span>"));
	}

	public void testCode() {
		String html = parser.parseToHtml("{{{\n" + "def fib(n):\n" + "  if n == 0 or n == 1:\n" + "    return n\n"
				+ "  else:\n" + "    # This recursion is not good for large numbers.\n"
				+ "    return fib(n-1) + fib(n-2)\n" + "}}}");
		TestUtil.println("HTML: \n" + html);
		assertTrue(html.contains("<pre class=\"prettyprint\">\n" + "def fib(n):\n" + "  if n == 0 or n == 1:\n"
				+ "    return n\n" + "  else:\n" + "    # This recursion is not good for large numbers.\n"
				+ "    return fib(n-1) + fib(n-2)\n" + "</pre>"));
	}

	public void testHeading1() {
		String html = parser.parseToHtml("= Heading =");
		TestUtil.println("HTML: \n" + html);
		assertTrue(html.contains("<h1>Heading</h1>"));
	}

	public void testHeading2() {
		String html = parser.parseToHtml("== Subheading ==");
		TestUtil.println("HTML: \n" + html);
		assertTrue(html.contains("<h2>Subheading</h2>"));
	}

	public void testHeading3() {
		String html = parser.parseToHtml("=== Level 3 ===");
		TestUtil.println("HTML: \n" + html);
		assertTrue(html.contains("<h3>Level 3</h3>"));
	}

	public void testHeading4() {
		String html = parser.parseToHtml("==== Level 4 ====");
		TestUtil.println("HTML: \n" + html);
		assertTrue(html.contains("<h4>Level 4</h4>"));
	}

	public void testHeading5() {
		String html = parser.parseToHtml("===== Level 5 =====");
		TestUtil.println("HTML: \n" + html);
		assertTrue(html.contains("<h5>Level 5</h5>"));
	}

	public void testHeading6() {
		String html = parser.parseToHtml("====== Level 6 ======");
		TestUtil.println("HTML: \n" + html);
		assertTrue(html.contains("<h6>Level 6</h6>"));
	}

	public void testDivider() {
		//TODO: Four or more dashes on a line by themselves results in a horizontal rule.
		fail("TODO: Four or more dashes on a line by themselves results in a horizontal rule.");
	}

	public void testList() {
		String html = parser.parseToHtml("The following is:\n  * A list\n  * Of bulleted items\n    # This is a numbered sublist\n    # Which is done by indenting further\n  * And back to the main bulleted list\n");
		TestUtil.println("HTML: \n" + html);
		assertTrue(html.contains("<p>The following is: <ul><li>A list </li><li>Of bulleted items </li><ol><li>This is a numbered sublist </li><li>Which is done by indenting further </li></ol><li>And back to the main bulleted list </li></ul></p>"));
	}

	public void testListSingleLeadingSpace() {
		String html = parser.parseToHtml(" * This is also a list\n * With a single leading space\n * Notice that it is rendered\n  # At the same levels\n  # As the above lists.\n * Despite the different indentation levels.\n");
		TestUtil.println("HTML: \n" + html);
		assertTrue(html.contains("<ul><li>This is also a list </li><li>With a single leading space </li><li>Notice that it is rendered </li><ol><li>At the same levels </li><li>As the above lists. </li></ol><li>Despite the different indentation levels. </li></ul>"));
	}
}
