/*******************************************************************************
 * Copyright (c) 2016 Jeremie Bresson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jeremie Bresson- initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.asciidoc.core.token;

import org.eclipse.mylyn.wikitext.core.parser.markup.PatternBasedElement;
import org.eclipse.mylyn.wikitext.core.parser.markup.PatternBasedElementProcessor;

/**
 * Cross-Reference links
 *
 * @author Jeremie Bresson
 */
public class XrefReplacementToken extends PatternBasedElement {

	final static String XREF_PATTERN = "<<([^\\],]+)(,[^>]+)?>>"; //$NON-NLS-1$

	@Override
	protected String getPattern(int groupOffset) {
		return XREF_PATTERN;
	}

	@Override
	protected int getPatternGroupCount() {
		return 2;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new XrefReplacementTokenProcessor();
	}

	private static class XrefReplacementTokenProcessor extends PatternBasedElementProcessor {
		@Override
		public void emit() {
			String hrefOrHashName = group(1);
			String text;
			if (group(2) != null) {
				text = group(2).substring(1).trim();
			} else {
				text = group(1);
			}
			builder.link(hrefOrHashName, text);
		}
	}
}
