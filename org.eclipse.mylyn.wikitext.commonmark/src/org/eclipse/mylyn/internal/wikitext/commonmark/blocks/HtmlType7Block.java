/*******************************************************************************
 * Copyright (c) 2015 David Green.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.wikitext.commonmark.blocks;

import static org.eclipse.mylyn.internal.wikitext.commonmark.blocks.HtmlConstants.HTML_TAG_NAME;
import static org.eclipse.mylyn.internal.wikitext.commonmark.blocks.HtmlConstants.REPEATING_ATTRIBUTE;

import java.util.regex.Pattern;

import org.eclipse.mylyn.internal.wikitext.commonmark.Line;
import org.eclipse.mylyn.internal.wikitext.commonmark.LineSequence;
import org.eclipse.mylyn.internal.wikitext.commonmark.ProcessingContext;
import org.eclipse.mylyn.internal.wikitext.commonmark.SourceBlock;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder;

public class HtmlType7Block extends SourceBlock {

	private final Pattern startPattern = Pattern.compile(
			"\\s{0,3}<" + HTML_TAG_NAME + REPEATING_ATTRIBUTE + "\\s*>?\\s*",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	private final Pattern closePattern = Pattern.compile("\\s{0,3}</" + HTML_TAG_NAME + "\\s*(/?>)?\\s*",
			Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);

	@Override
	public void process(ProcessingContext context, DocumentBuilder builder, LineSequence lineSequence) {
		Line line = lineSequence.getCurrentLine();
		while (line != null && !line.isEmpty()) {
			builder.charactersUnescaped(line.getText());
			builder.charactersUnescaped("\n");

			lineSequence.advance();
			line = lineSequence.getCurrentLine();
		}
	}

	@Override
	public boolean canStart(LineSequence lineSequence) {
		Line line = lineSequence.getCurrentLine();
		if (line != null) {
			return startPattern.matcher(line.getText()).matches() || closePattern.matcher(line.getText()).matches();
		}
		return false;
	}
}