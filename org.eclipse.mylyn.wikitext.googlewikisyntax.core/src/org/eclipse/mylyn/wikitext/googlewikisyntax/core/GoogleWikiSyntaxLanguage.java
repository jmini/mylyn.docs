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
package org.eclipse.mylyn.wikitext.googlewikisyntax.core;

import java.util.List;

import org.eclipse.mylyn.internal.wikitext.googlewikisyntax.core.block.CodeBlock;
import org.eclipse.mylyn.internal.wikitext.googlewikisyntax.core.block.HeadingBlock;
import org.eclipse.mylyn.internal.wikitext.googlewikisyntax.core.block.ListBlock;
import org.eclipse.mylyn.internal.wikitext.googlewikisyntax.core.block.ParagraphBlock;
import org.eclipse.mylyn.internal.wikitext.googlewikisyntax.core.phrase.SimplePhraseModifier;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.SpanType;
import org.eclipse.mylyn.wikitext.core.parser.MarkupParser;
import org.eclipse.mylyn.wikitext.core.parser.markup.AbstractMarkupLanguage;
import org.eclipse.mylyn.wikitext.core.parser.markup.Block;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguageConfiguration;

/**
 * A textile dialect that parses Google WikiSyntax markup. Based on the spec available at <a
 * href="http://code.google.com/p/support/wiki/WikiSyntax">http://code.google.com/p/support/wiki/WikiSyntax</a>,
 * 
 * @author Jeremie Bresson
 * @since 1.0
 */
public class GoogleWikiSyntaxLanguage extends AbstractMarkupLanguage {
	private static final String BUNDLE_NAME = "org.eclipse.mylyn.wikitext.googlewikisyntax.core.language"; //$NON-NLS-1$

//	private boolean preprocessFootnotes = false;

	public GoogleWikiSyntaxLanguage() {
		setName("Google WikiSyntax"); //$NON-NLS-1$
	}

	/**
	 * subclasses may override this method to add blocks to the Google WikiSyntax language. Overriding classes should
	 * call <code>super.addBlockExtensions(blocks,paragraphBreakingBlocks)</code> if the default language extensions are
	 * desired (glossary and table of contents).
	 * 
	 * @param blocks
	 *            the list of blocks to which extensions may be added
	 * @param paragraphBreakingBlocks
	 *            the list of blocks that end a paragraph
	 */
	@Override
	protected void addBlockExtensions(List<Block> blocks, List<Block> paragraphBreakingBlocks) {
//		blocks.add(new TextileGlossaryBlock());
//		blocks.add(new TableOfContentsBlock());
//		super.addBlockExtensions(blocks, paragraphBreakingBlocks);
	}

	@Override
	protected void addStandardBlocks(List<Block> blocks, List<Block> paragraphBreakingBlocks) {
		// IMPORTANT NOTE: Most items below have order dependencies.  DO NOT REORDER ITEMS BELOW!!

//		blocks.add(new HeadingBlock());

		blocks.add(new HeadingBlock());
		ListBlock listBlock = new ListBlock();
		blocks.add(listBlock);
		paragraphBreakingBlocks.add(listBlock);
//		blocks.add(new PreformattedBlock());
//		blocks.add(new QuoteBlock());
		blocks.add(new CodeBlock());
//		blocks.add(new FootnoteBlock());
//		TableBlock tableBlock = new TableBlock();
//		blocks.add(tableBlock);
//		paragraphBreakingBlocks.add(tableBlock);
	}

	@Override
	protected void addStandardPhraseModifiers(PatternBasedSyntax phraseModifierSyntax) {
//		boolean escapingHtml = configuration == null ? false : configuration.isEscapingHtmlAndXml();

//		phraseModifierSyntax.add(new HtmlEndTagPhraseModifier(escapingHtml));
//		phraseModifierSyntax.add(new HtmlStartTagPhraseModifier(escapingHtml));
//		phraseModifierSyntax.beginGroup("(?:(?<=[\\s\\.,\\\"'?!;:\\)\\(\\{\\}\\[\\]])|^)(?:", 0); //$NON-NLS-1$
//		phraseModifierSyntax.add(new EscapeTextilePhraseModifier());

		phraseModifierSyntax.add(new SimplePhraseModifier("*", SpanType.STRONG, true, null)); //$NON-NLS-1$
		phraseModifierSyntax.add(new SimplePhraseModifier("_", SpanType.ITALIC, true, null)); //$NON-NLS-1$
		phraseModifierSyntax.add(new SimplePhraseModifier("`", SpanType.MONOSPACE, true, null)); //$NON-NLS-1$
		phraseModifierSyntax.add(new SimplePhraseModifier(",,", SpanType.SUBSCRIPT, true, null)); //$NON-NLS-1$
		phraseModifierSyntax.add(new SimplePhraseModifier("^", SpanType.SUPERSCRIPT, true, null)); //$NON-NLS-1$
		phraseModifierSyntax.add(new SimplePhraseModifier("~~", SpanType.SPAN, true, "text-decoration: line-through")); //$NON-NLS-1$ //$NON-NLS-2$

//		phraseModifierSyntax.add(new ImageTextilePhraseModifier());
//		phraseModifierSyntax.add(new HyperlinkPhraseModifier()); // hyperlinks are actually a phrase modifier see bug 283093
//		phraseModifierSyntax.endGroup(")(?=\\W|$)", 0); //$NON-NLS-1$

	}

	@Override
	protected void addStandardTokens(PatternBasedSyntax tokenSyntax) {
//		tokenSyntax.add(new EntityReferenceReplacementToken("(tm)", "#8482")); //$NON-NLS-1$ //$NON-NLS-2$
//		tokenSyntax.add(new EntityReferenceReplacementToken("(TM)", "#8482")); //$NON-NLS-1$ //$NON-NLS-2$
//		tokenSyntax.add(new EntityReferenceReplacementToken("(c)", "#169")); //$NON-NLS-1$ //$NON-NLS-2$
//		tokenSyntax.add(new EntityReferenceReplacementToken("(C)", "#169")); //$NON-NLS-1$ //$NON-NLS-2$
//		tokenSyntax.add(new EntityReferenceReplacementToken("(r)", "#174")); //$NON-NLS-1$ //$NON-NLS-2$
//		tokenSyntax.add(new EntityReferenceReplacementToken("(R)", "#174")); //$NON-NLS-1$ //$NON-NLS-2$
//		tokenSyntax.add(new FootnoteReferenceReplacementToken());
//		if (configuration == null || !configuration.isOptimizeForRepositoryUsage()) {
//			ResourceBundle res = ResourceBundle.getBundle(
//					BUNDLE_NAME,
//					configuration == null || configuration.getLocale() == null
//							? Locale.ENGLISH
//							: configuration.getLocale());
//
//			tokenSyntax.add(new EntityWrappingReplacementToken(
//					"\"", res.getString("quote_left"), res.getString("quote_right"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//			tokenSyntax.add(new EntityWrappingReplacementToken(
//					"'", res.getString("singlequote_left"), res.getString("singlequote_right"))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
//
//			tokenSyntax.add(new PatternEntityReferenceReplacementToken("(?:(?<=\\w)(')(?=\\w))", "#8217")); // apostrophe //$NON-NLS-1$ //$NON-NLS-2$
//		}
//		tokenSyntax.add(new PatternEntityReferenceReplacementToken("(?:(--)(?=\\s\\w))", "#8212")); // emdash //$NON-NLS-1$ //$NON-NLS-2$
//		tokenSyntax.add(new PatternEntityReferenceReplacementToken("(?:(?<=\\w\\s)(-)(?=\\s\\w))", "#8211")); // endash //$NON-NLS-1$ //$NON-NLS-2$
//		tokenSyntax.add(new PatternEntityReferenceReplacementToken("(?:(?<=\\d\\s)(x)(?=\\s\\d))", "#215")); // mul //$NON-NLS-1$ //$NON-NLS-2$
//		if (configuration == null || !configuration.isOptimizeForRepositoryUsage()) {
//			tokenSyntax.add(new AcronymReplacementToken());
//		}
//		tokenSyntax.add(new EntityReplacementToken());
	}

	@Override
	protected Block createParagraphBlock() {
		return new ParagraphBlock();
	}

//	/**
//	 * indicate if footnotes should be preprocessed to avoid false-positives when footnote references are used
//	 * inadvertently. The default is false.
//	 */
//	public boolean isPreprocessFootnotes() {
//		return preprocessFootnotes;
//	}
//
//	/**
//	 * indicate if footnotes should be preprocessed to avoid false-positives when footnote references are used
//	 * inadvertently. The default is false.
//	 */
//	public void setPreprocessFootnotes(boolean preprocessFootnotes) {
//		this.preprocessFootnotes = preprocessFootnotes;
//	}

	@Override
	public void configure(MarkupLanguageConfiguration configuration) throws UnsupportedOperationException {
//		if (configuration.isOptimizeForRepositoryUsage()) {
//			setPreprocessFootnotes(true);
//		}
		super.configure(configuration);
	}

	@Override
	public GoogleWikiSyntaxLanguage clone() {
		GoogleWikiSyntaxLanguage copy = (GoogleWikiSyntaxLanguage) super.clone();
//		copy.preprocessFootnotes = preprocessFootnotes;
		return copy;
	}

	@Override
	public void processContent(MarkupParser parser, String markupContent, boolean asDocument) {
//		if (preprocessFootnotes) {
//			boolean previousBlocksOnly = isBlocksOnly();
//			boolean previousFilterGenerativeContents = isFilterGenerativeContents();
//			setBlocksOnly(true);
//			setFilterGenerativeContents(true);
//
//			DocumentBuilder builder = parser.getBuilder();
//			parser.setBuilder(new NoOpDocumentBuilder());
//			currentState = new GoogleWikiSyntaxContentState();
//			GoogleWikiSyntaxContentState preprocessingState = currentState;
//			super.processContent(parser, markupContent, asDocument);
//
//			setBlocksOnly(previousBlocksOnly);
//			setFilterGenerativeContents(previousFilterGenerativeContents);
//
//			currentState = new GoogleWikiSyntaxContentState();
//			currentState.setFootnoteNumbers(preprocessingState.getFootnoteNumbers());
//			parser.setBuilder(builder);
//			super.processContent(parser, markupContent, asDocument);
//
//			currentState = null;
//		} else {
//		currentState = null;
		super.processContent(parser, markupContent, asDocument);
//		}
	}
}
