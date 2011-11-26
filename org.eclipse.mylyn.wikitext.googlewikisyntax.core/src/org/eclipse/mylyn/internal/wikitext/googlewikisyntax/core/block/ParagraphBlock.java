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

import org.eclipse.mylyn.wikitext.core.parser.Attributes;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.BlockType;
import org.eclipse.mylyn.wikitext.core.parser.markup.Block;
import org.eclipse.mylyn.wikitext.googlewikisyntax.core.GoogleWikiSyntaxLanguage;

/**
 * Matches any google WikiSyntax text for paragraph.
 * 
 * @author David Green
 */
public class ParagraphBlock extends Block {

	private int blockLineCount = 0;

	public ParagraphBlock() {
	}

	@Override
	public int processLineContent(String line, int offset) {
		if (blockLineCount == 0) {
			Attributes attributes = new Attributes();
			builder.beginBlock(BlockType.PARAGRAPH, attributes);
		}

		if (markupLanguage.isEmptyLine(line)) {
			setClosed(true);
			return 0;
		}

		GoogleWikiSyntaxLanguage gwsLanguage = (GoogleWikiSyntaxLanguage) getMarkupLanguage();

		for (Block block : gwsLanguage.getParagraphBreakingBlocks()) {
			if (block.canStart(line, offset)) {
				setClosed(true);
				return 0;
			}
		}

		if (blockLineCount != 0) {
			builder.lineBreak();
		}
		++blockLineCount;

		gwsLanguage.emitMarkupLine(getParser(), state, line, offset);

		return -1;
	}

	@Override
	public boolean canStart(String line, int lineOffset) {
		blockLineCount = 0;
		return true;
	}

	@Override
	public void setClosed(boolean closed) {
		if (closed && !isClosed()) {
			builder.endBlock();
		}
		super.setClosed(closed);
	}

}
