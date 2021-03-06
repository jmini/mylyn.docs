/*******************************************************************************
 * Copyright (c) 2007, 2015 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.wikitext.confluence.core;

import org.eclipse.mylyn.wikitext.tests.AbstractMarkupGenerationTest;
import org.junit.Test;

public class ConfluenceCodeBlockTest extends AbstractMarkupGenerationTest<ConfluenceLanguage> {

	@Override
	protected ConfluenceLanguage createMarkupLanguage() {
		return new ConfluenceLanguage();
	}

	@Test
	public void block() {
		assertMarkup(
				"<h1 id=\"aheader\">a header</h1><p>Some text</p><pre class=\"Java code-Java\"><code class=\"Java code-Java\">\npublic class Foo {\n}\n\n</code></pre><p>More text...</p>",
				"h1. a header\n" + //
						"\n" + //
						"Some text\n" + //
						"{code:language=Java}\n" + //
						"public class Foo {\n" + //
						"}\n" + //
						"{code}\n" + //
						"More text...");
	}

	@Test
	public void blockWithTrailingText() {
		assertMarkup("<pre><code>some code\n</code></pre><p>more text</p>", "{code}some code{code}more text");
	}

	@Test
	public void blockWithPrecedingText() {
		assertMarkup("<p>text</p><pre><code>some code\n</code></pre>", "text{code}some code{code}");
	}

	@Test
	public void blockLanguageJava() {
		assertMarkup(
				"<pre class=\"java code-java\"><code class=\"java code-java\">some code\n</code></pre><p>more text</p>",
				"{code:Java}some code{code}more text");
	}

	@Test
	public void blockWithDoubleSlash() {
		assertMarkup("<pre><code>one\\\\two\n</code></pre>", "{code}one\\\\two{code}");
	}
}
