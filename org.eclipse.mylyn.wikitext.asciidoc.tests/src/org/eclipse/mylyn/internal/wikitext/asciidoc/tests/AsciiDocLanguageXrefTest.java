/*******************************************************************************
 * Copyright (c) 2016 Jeremie Bresson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jeremie Bresson - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.asciidoc.tests;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * @author Jeremie Bresson
 */
public class AsciiDocLanguageXrefTest extends AsciiDocLanguageTestBase {

	@Test
	public void testInText() {
		String text = "Lorem <<xxx>> ipsum";

		String html = parseToHtml(text);

		assertEquals("<p>Lorem <a href=\"xxx\">xxx</a> ipsum</p>\n", html);
	}

	@Test
	public void testInTextWithText() {
		String text = "Lorem <<xxx, yyy>> ipsum";

		String html = parseToHtml(text);

		assertEquals("<p>Lorem <a href=\"xxx\">yyy</a> ipsum</p>\n", html);
	}

}
