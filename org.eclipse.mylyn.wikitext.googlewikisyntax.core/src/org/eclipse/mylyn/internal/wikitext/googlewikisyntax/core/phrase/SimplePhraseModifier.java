/*******************************************************************************
 * Copyright (c) 2011 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *     Jeremie Bresson - initial Google WikiSyntax support (bug 291239)
 *******************************************************************************/
package org.eclipse.mylyn.internal.wikitext.googlewikisyntax.core.phrase;

import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.SpanType;

/**
 * @author David Green
 */
public class SimplePhraseModifier extends SimpleWrappedPhraseModifier {

	public SimplePhraseModifier(String delimiter, SpanType spanType) {
		super(delimiter, delimiter, new SpanType[] { spanType });
	}

	public SimplePhraseModifier(String delimiter, SpanType spanType, boolean nesting, String cssStyle) {
		super(delimiter, delimiter, new SpanType[] { spanType }, nesting, cssStyle);
	}

	public SimplePhraseModifier(String delimiter, SpanType[] spanType) {
		super(delimiter, delimiter, spanType);
	}

	public SimplePhraseModifier(String delimiter, SpanType[] spanType, boolean nesting, String cssStyle) {
		super(delimiter, delimiter, spanType, nesting, cssStyle);
	}
}
