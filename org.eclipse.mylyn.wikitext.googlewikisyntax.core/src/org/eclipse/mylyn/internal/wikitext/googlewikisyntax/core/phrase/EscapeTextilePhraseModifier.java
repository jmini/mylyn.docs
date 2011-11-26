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

import org.eclipse.mylyn.wikitext.core.parser.markup.PatternBasedElement;
import org.eclipse.mylyn.wikitext.core.parser.markup.PatternBasedElementProcessor;
import org.eclipse.mylyn.wikitext.core.parser.markup.phrase.LiteralPhraseModifierProcessor;

/**
 * @author David Green
 */
public class EscapeTextilePhraseModifier extends PatternBasedElement {

	@Override
	protected String getPattern(int groupOffset) {
		String quotedDelimiter = Pattern.quote("=="); //$NON-NLS-1$

		return quotedDelimiter + "(\\S(?:.*?\\S)?)" + // content //$NON-NLS-1$
				quotedDelimiter;
	}

	@Override
	protected int getPatternGroupCount() {
		return 1;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new LiteralPhraseModifierProcessor(false);
	}

}
