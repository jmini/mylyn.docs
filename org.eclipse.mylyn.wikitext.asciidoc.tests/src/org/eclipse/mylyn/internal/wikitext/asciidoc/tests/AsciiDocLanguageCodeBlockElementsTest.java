/*******************************************************************************
 * Copyright (c) 2015, 2016 Max Rydahl Andersen and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Max Rydahl Andersen - Bug 474084: initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.asciidoc.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Tests for asciidoc code block elements.
 *
 * @author Max Rydahl Andersen
 */
public class AsciiDocLanguageCodeBlockElementsTest extends AsciiDocLanguageTestBase {

	@Test
	public void basicCodeBlock() {
		String html = parseToHtml("----\n" //
				+ "10 PRINT \"Hello World!\"\n" //
				+ "20 GOTO 10\n" //
				+ "----");
		assertEquals("<div class=\"listingblock\">" //
				+ "<div class=\"content\">" //
				+ "<pre class=\"nowrap\">" //
				+ "10 PRINT \"Hello World!\"<br/>" //
				+ "20 GOTO 10<br/>" //
				+ "</pre>" //
				+ "</div></div>", html);
	}

	@Test
	public void titledCodeBlock() {
		String html = parseToHtml(".Helloworld.bas\n" //
				+ "----\n" //
				+ "10 PRINT \"Hello World!\"\n" //
				+ "20 GOTO 10\n" //
				+ "----");
		assertEquals("<div class=\"listingblock\">" //
				+ "<div class=\"title\">Helloworld.bas</div>" //
				+ "<div class=\"content\">" //
				+ "<pre class=\"nowrap\">" //
				+ "10 PRINT \"Hello World!\"<br/>" //
				+ "20 GOTO 10<br/>" //
				+ "</pre>" //
				+ "</div></div>", html);
	}

	@Test
	public void titledCodeWithSourceBlock() {
		String html = parseToHtml(".Helloworld.bas\n" //
				+ "[source,basic]\n" //
				+ "----\n" //
				+ "10 PRINT \"Hello World!\"\n" //
				+ "20 GOTO 10\n" //
				+ "----");
		assertEquals("<div class=\"listingblock\">" //
				+ "<div class=\"title\">Helloworld.bas</div>" //
				+ "<div class=\"content\">" //
				+ "<pre class=\"nowrap source-basic\">" //
				+ "10 PRINT \"Hello World!\"<br/>" //
				+ "20 GOTO 10<br/>" //
				+ "</pre>" //
				+ "</div></div>", html);
	}

	@Test
	public void unbalancedCodeBlock() {
		// ascidoctor requires matching start/end blocks
		// http://asciidoctor.org/docs/user-manual/#delimiter-lines
		String html = parseToHtml("----\n" //
				+ "10 PRINT \"Hello World!\"\n" //
				+ "20 GOTO 10\n" //
				+ "---");
		assertEquals("<div class=\"listingblock\">" //
				+ "<div class=\"content\">" //
				+ "<pre class=\"nowrap\">" //
				+ "10 PRINT \"Hello World!\"<br/>" //
				+ "20 GOTO 10<br/>" //
				+ "---<br/>" + "</pre>" //
				+ "</div></div>", html);

	}
}
