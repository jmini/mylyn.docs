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

import java.util.regex.Pattern;

import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.SpanType;
import org.eclipse.mylyn.wikitext.core.parser.markup.PatternBasedElement;
import org.eclipse.mylyn.wikitext.core.parser.markup.PatternBasedElementProcessor;

/**
 * @author David Green
 */
public class SimpleWrappedPhraseModifier extends PatternBasedElement {

	protected static final int CONTENT_GROUP = 1;

	private static class SimplePhraseModifierProcessor extends PatternBasedElementProcessor {
		private final SpanType[] spanType;

		private final boolean nesting;

		private final String cssStyle;

		public SimplePhraseModifierProcessor(SpanType[] spanType, boolean nesting, String cssStyle) {
			this.spanType = spanType;
			this.nesting = nesting;
			this.cssStyle = cssStyle;
		}

		@Override
		public void emit() {
			for (SpanType type : spanType) {
				Attributes attributes = new Attributes();
				attributes.setCssStyle(cssStyle);
				getBuilder().beginSpan(type, attributes);
			}
			if (nesting) {
				getMarkupLanguage().emitMarkupLine(parser, state, getStart(this), getContent(this), 0);
			} else {
				getMarkupLanguage().emitMarkupText(parser, state, getContent(this));
			}
			for (int x = 0; x < spanType.length; ++x) {
				getBuilder().endSpan();
			}
		}
	}

	private final String startDelimiter;

	private final String endDelimiter;

	private final SpanType[] spanType;

	private final boolean nesting;

	private final String cssStyle;

	public SimpleWrappedPhraseModifier(String startDelimiter, String endDelimiter, SpanType[] spanType) {
		this(startDelimiter, endDelimiter, spanType, false, null);
	}

	public SimpleWrappedPhraseModifier(String startDelimiter, String endDelimiter, SpanType[] spanType,
			boolean nesting, String cssStyle) {
		this.startDelimiter = startDelimiter;
		this.endDelimiter = endDelimiter;
		this.spanType = spanType;
		this.nesting = nesting;
		this.cssStyle = cssStyle;
	}

	@Override
	protected String getPattern(int groupOffset) {
		return Pattern.quote(startDelimiter) + "([^\\s-](?:.*?[^\\s-])?)(?:(?<=[^!])" + // content: note that we dont allow preceding '-' or trailing '-' to avoid conflict with strikethrough and emdash //$NON-NLS-1$
				Pattern.quote(endDelimiter) + ")"; //$NON-NLS-1$
	}

	@Override
	protected int getPatternGroupCount() {
		return 1;
	}

	protected static String getContent(PatternBasedElementProcessor processor) {
		return processor.group(CONTENT_GROUP);
	}

	protected static int getStart(PatternBasedElementProcessor processor) {
		return processor.start(CONTENT_GROUP);
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new SimplePhraseModifierProcessor(spanType, nesting, cssStyle);
	}
}
