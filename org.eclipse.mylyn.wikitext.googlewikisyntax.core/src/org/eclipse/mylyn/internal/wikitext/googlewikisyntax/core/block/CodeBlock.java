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
package org.eclipse.mylyn.internal.wikitext.googlewikisyntax.core.block;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.SpanType;
import org.eclipse.mylyn.wikitext.core.parser.markup.Block;

/**
 * Code text block, matches blocks that start with <code>bc. </code>
 * 
 * @author David Green
 */
public class CodeBlock extends Block {
	private static final int LINE_REMAINDER_GROUP_OFFSET = 2;

	private final Pattern startPattern;

	private final Pattern endPattern;

	protected int blockLineCount = 0;

	private Matcher matcher;

	public CodeBlock() {
		startPattern = Pattern.compile("(\\{\\{\\{)(.*)"); //$NON-NLS-1$
		endPattern = Pattern.compile("(\\}\\}\\})(.*)"); //$NON-NLS-1$
	}

	@Override
	public int processLineContent(String line, int offset) {
		boolean begining = false;

		if (blockLineCount == 0) {
			offset = matcher.start(LINE_REMAINDER_GROUP_OFFSET);
			begining = true;
		}

		int end = line.length();
		int segmentEnd = end;
		boolean terminating = false;

		if (offset < end) {
			Matcher endMatcher = endPattern.matcher(line);
			if (blockLineCount == 0) {
				endMatcher.region(offset, end);
			}
			if (endMatcher.find()) {
				terminating = true;
				end = endMatcher.start(LINE_REMAINDER_GROUP_OFFSET);
				segmentEnd = endMatcher.start(1);
			}
		}

		if (end < line.length()) {
			state.setLineSegmentEndOffset(end);
		}

		++blockLineCount;

		if (begining) {
			beginBlock(terminating);
		}

		final String content = line.substring(offset, segmentEnd);
		handleBlockContent(content);

		if (terminating) {
			endBlock(begining);
			setClosed(true);
		}
		return end == line.length() ? -1 : end;
	}

	@Override
	public boolean canStart(String line, int lineOffset) {
		resetState();
		matcher = startPattern.matcher(line);
		if (lineOffset > 0) {
			matcher.region(lineOffset, line.length());
		}
		return matcher.matches();
	}

	protected void beginBlock(boolean singleLine) {
		if (singleLine) {
			builder.beginSpan(SpanType.MONOSPACE, new Attributes());
		} else {
			Attributes preAttributes = new Attributes();
			preAttributes.setCssClass("prettyprint"); //$NON-NLS-1$
			builder.beginBlock(BlockType.PREFORMATTED, preAttributes);
		}
	}

	protected void handleBlockContent(String content) {
		if (blockLineCount > 1) {
			builder.characters("\n"); //$NON-NLS-1$
		}
		builder.characters(content);
	}

	protected void endBlock(boolean singleLine) {
		if (singleLine) {
			builder.endSpan();
		} else {
//			builder.endBlock(); // code
			builder.endBlock(); // pre
		}
	}

	protected void resetState() {
		blockLineCount = 0;
	}
}
