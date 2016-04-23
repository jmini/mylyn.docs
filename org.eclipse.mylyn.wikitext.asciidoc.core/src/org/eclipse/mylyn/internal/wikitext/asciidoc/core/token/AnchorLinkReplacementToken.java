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

import org.eclipse.mylyn.wikitext.core.parser.LinkAttributes;
import org.eclipse.mylyn.wikitext.core.parser.markup.PatternBasedElement;
import org.eclipse.mylyn.wikitext.core.parser.markup.PatternBasedElementProcessor;

/**
 * Anchor links
 *
 * @author Jeremie Bresson
 */
public class AnchorLinkReplacementToken extends PatternBasedElement {

	final static String ANCHOR_PATTERN = "\\[\\[([^\\],]+)(,[^\\]]+)?\\]\\]"; //$NON-NLS-1$

	@Override
	protected String getPattern(int groupOffset) {
		return ANCHOR_PATTERN;
	}

	@Override
	protected int getPatternGroupCount() {
		return 2;
	}

	@Override
	protected PatternBasedElementProcessor newProcessor() {
		return new AnchorLinkReplacementTokenProcessor();
	}

	private static class AnchorLinkReplacementTokenProcessor extends PatternBasedElementProcessor {
		@Override
		public void emit() {
			LinkAttributes attribute = new LinkAttributes();
			attribute.setId(group(1));
			builder.link(attribute, null, ""); //$NON-NLS-1$
		}
	}
}
