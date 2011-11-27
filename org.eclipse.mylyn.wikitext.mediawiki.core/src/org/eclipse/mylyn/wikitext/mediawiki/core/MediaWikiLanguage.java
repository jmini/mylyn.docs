/*******************************************************************************
 * Copyright (c) 2007, 2010 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.wikitext.mediawiki.core;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.mylyn.internal.wikitext.mediawiki.core.AbstractMediaWikiLanguage;
import org.eclipse.mylyn.internal.wikitext.mediawiki.core.BuiltInTemplateResolver;
import org.eclipse.mylyn.internal.wikitext.mediawiki.core.MediaWikiIdGenerationStrategy;
import org.eclipse.mylyn.internal.wikitext.mediawiki.core.block.CommentBlock;
import org.eclipse.mylyn.internal.wikitext.mediawiki.core.block.EscapeBlock;
import org.eclipse.mylyn.internal.wikitext.mediawiki.core.block.HeadingBlock;
import org.eclipse.mylyn.internal.wikitext.mediawiki.core.block.ListBlock;
import org.eclipse.mylyn.internal.wikitext.mediawiki.core.block.ParagraphBlock;
import org.eclipse.mylyn.internal.wikitext.mediawiki.core.block.PreformattedBlock;
import org.eclipse.mylyn.internal.wikitext.mediawiki.core.block.SourceBlock;
import org.eclipse.mylyn.internal.wikitext.mediawiki.core.block.TableBlock;
import org.eclipse.mylyn.internal.wikitext.mediawiki.core.block.TableOfContentsBlock;
import org.eclipse.mylyn.internal.wikitext.mediawiki.core.phrase.EscapePhraseModifier;
import org.eclipse.mylyn.internal.wikitext.mediawiki.core.phrase.SimplePhraseModifier;
import org.eclipse.mylyn.internal.wikitext.mediawiki.core.token.HyperlinkExternalReplacementToken;
import org.eclipse.mylyn.internal.wikitext.mediawiki.core.token.HyperlinkInternalReplacementToken;
import org.eclipse.mylyn.internal.wikitext.mediawiki.core.token.ImageReplacementToken;
import org.eclipse.mylyn.internal.wikitext.mediawiki.core.token.LineBreakToken;
import org.eclipse.mylyn.wikitext.core.parser.DocumentBuilder.SpanType;
import org.eclipse.mylyn.wikitext.core.parser.markup.Block;
import org.eclipse.mylyn.wikitext.core.parser.markup.IdGenerationStrategy;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.eclipse.mylyn.wikitext.core.parser.markup.phrase.HtmlCommentPhraseModifier;
import org.eclipse.mylyn.wikitext.core.parser.markup.phrase.LimitedHtmlEndTagPhraseModifier;
import org.eclipse.mylyn.wikitext.core.parser.markup.phrase.LimitedHtmlStartTagPhraseModifier;
import org.eclipse.mylyn.wikitext.core.parser.markup.token.EntityReferenceReplacementToken;
import org.eclipse.mylyn.wikitext.core.parser.markup.token.ImpliedHyperlinkReplacementToken;
import org.eclipse.mylyn.wikitext.core.parser.markup.token.PatternLiteralReplacementToken;

/**
 * A markup language for <a href="http://www.mediawiki.org">MediaWiki</a> <a
 * href="http://en.wikipedia.org/wiki/Wikitext">Wikitext markup</a>, which is the wiki format used by <a
 * href="http://www.wikipedia.org>WikiPedia</a> and <a href="http://www.wikimedia.org/">several other major sites</a>.
 * 
 * @author David Green
 * @since 1.0
 */
public class MediaWikiLanguage extends AbstractMediaWikiLanguage {

	private List<Template> templates = new ArrayList<Template>();

	private List<TemplateResolver> templateProviders = new ArrayList<TemplateResolver>();

	private String templateExcludes;

	public MediaWikiLanguage() {
		setName("MediaWiki"); //$NON-NLS-1$
		setInternalLinkPattern("/wiki/{0}"); //$NON-NLS-1$

		templateProviders.add(new BuiltInTemplateResolver());
	}

	/**
	 * Convert a page name to an href to the page.
	 * 
	 * @param pageName
	 *            the name of the page to target
	 * @return the href to access the page
	 * @see MarkupLanguage#getInternalLinkPattern()
	 */
	public String toInternalHref(String pageName) {
		return super.mapPageNameToHref(pageName);
	}

	@Override
	public IdGenerationStrategy getIdGenerationStrategy() {
		return new MediaWikiIdGenerationStrategy();
	}

	@Override
	protected void addStandardBlocks(List<Block> blocks, List<Block> paragraphBreakingBlocks) {
		// IMPORTANT NOTE: Most items below have order dependencies.  DO NOT REORDER ITEMS BELOW!!

		blocks.add(new HeadingBlock());
		blocks.add(new ListBlock());

		if (hasPreformattedBlock()) {
			// preformatted blocks are lines that start with a single space, and thus are non-optimal for
			// repository usage.
			blocks.add(new PreformattedBlock());
		}
		blocks.add(new SourceBlock());

		blocks.add(new TableBlock());
		blocks.add(new TableOfContentsBlock());
		blocks.add(new EscapeBlock());
		blocks.add(new CommentBlock());

		for (Block block : blocks) {
			if (block instanceof ParagraphBlock) {
				continue;
			}
			paragraphBreakingBlocks.add(block);
		}

	}

	private boolean hasPreformattedBlock() {
		return configuration == null ? true : !configuration.isOptimizeForRepositoryUsage();
	}

	@Override
	protected void addStandardPhraseModifiers(PatternBasedSyntax phraseModifierSyntax) {
		phraseModifierSyntax.add(new EscapePhraseModifier());
		phraseModifierSyntax.beginGroup("(?:(?<=[\\s\\.,\\\"'?!;:\\)\\(\\{\\}\\[\\]=>])|^)(?:", 0); //$NON-NLS-1$
		phraseModifierSyntax.add(new SimplePhraseModifier("'''''", new SpanType[] { SpanType.BOLD, SpanType.ITALIC }, //$NON-NLS-1$
				true));
		phraseModifierSyntax.add(new SimplePhraseModifier("'''", SpanType.BOLD, true)); //$NON-NLS-1$
		phraseModifierSyntax.add(new SimplePhraseModifier("''", SpanType.ITALIC, true)); //$NON-NLS-1$
		phraseModifierSyntax.endGroup(")(?=\\W|$)", 0); //$NON-NLS-1$

		boolean escapingHtml = configuration == null ? false : configuration.isEscapingHtmlAndXml();

		if (!escapingHtml) {
			String[] allowedHtmlTags = new String[] { // HANDLED BY LineBreakToken "<br>",
					// HANDLED BY LineBreakToken "<br/>",
					"b", "big", "blockquote", "caption", "center", "cite", "code", "dd", "del", "div", "dl", "dt", "em", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$
					"font", "h1", "h2", "h3", "h4", "h5", "h6", "hr", "i", "ins", "li", "ol", "p", "pre", "rb", "rp", "rt", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$ //$NON-NLS-15$ //$NON-NLS-16$ //$NON-NLS-17$
					"ruby", "s", "small", "span", "strike", "strong", "sub", "sup", "table", "td", "th", "tr", "tt", "u", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$ //$NON-NLS-10$ //$NON-NLS-11$ //$NON-NLS-12$ //$NON-NLS-13$ //$NON-NLS-14$
					"ul", "var" }; //$NON-NLS-1$ //$NON-NLS-2$
			phraseModifierSyntax.add(new LimitedHtmlEndTagPhraseModifier(allowedHtmlTags));
			phraseModifierSyntax.add(new LimitedHtmlStartTagPhraseModifier(allowedHtmlTags));
			phraseModifierSyntax.add(new HtmlCommentPhraseModifier());
		}
	}

	@Override
	protected void addStandardTokens(PatternBasedSyntax tokenSyntax) {
		tokenSyntax.add(new LineBreakToken());
		tokenSyntax.add(new EntityReferenceReplacementToken("(tm)", "#8482")); //$NON-NLS-1$ //$NON-NLS-2$
		tokenSyntax.add(new EntityReferenceReplacementToken("(TM)", "#8482")); //$NON-NLS-1$ //$NON-NLS-2$
		tokenSyntax.add(new EntityReferenceReplacementToken("(c)", "#169")); //$NON-NLS-1$ //$NON-NLS-2$
		tokenSyntax.add(new EntityReferenceReplacementToken("(C)", "#169")); //$NON-NLS-1$ //$NON-NLS-2$
		tokenSyntax.add(new EntityReferenceReplacementToken("(r)", "#174")); //$NON-NLS-1$ //$NON-NLS-2$
		tokenSyntax.add(new EntityReferenceReplacementToken("(R)", "#174")); //$NON-NLS-1$ //$NON-NLS-2$
		tokenSyntax.add(new ImageReplacementToken());
		tokenSyntax.add(new HyperlinkInternalReplacementToken());
		tokenSyntax.add(new HyperlinkExternalReplacementToken());
		tokenSyntax.add(new ImpliedHyperlinkReplacementToken());
		tokenSyntax.add(new PatternLiteralReplacementToken("(?:(?<=^|\\w\\s)(----)(?=$|\\s\\w))", "<hr/>")); // horizontal rule //$NON-NLS-1$ //$NON-NLS-2$
		tokenSyntax.add(new org.eclipse.mylyn.internal.wikitext.mediawiki.core.token.EntityReferenceReplacementToken());
	}

	@Override
	protected Block createParagraphBlock() {
		ParagraphBlock paragraphBlock = new ParagraphBlock(hasPreformattedBlock());
		if (configuration != null && configuration.isNewlinesMustCauseLineBreak()) {
			paragraphBlock.setNewlinesCauseLineBreak(true);
		}
		return paragraphBlock;
	}

	/**
	 * @since 1.3
	 */
	@Override
	public List<Template> getTemplates() {
		return templates;
	}

	/**
	 * @since 1.3
	 */
	public void setTemplates(List<Template> templates) {
		if (templates == null) {
			throw new IllegalArgumentException();
		}
		this.templates = templates;
	}

	/**
	 * @since 1.3
	 */
	@Override
	public List<TemplateResolver> getTemplateProviders() {
		return templateProviders;
	}

	/**
	 * @since 1.3
	 */
	public void setTemplateProviders(List<TemplateResolver> templateProviders) {
		if (templateProviders == null) {
			throw new IllegalArgumentException();
		}
		this.templateProviders = templateProviders;
	}

	@Override
	public MarkupLanguage clone() {
		MediaWikiLanguage copy = (MediaWikiLanguage) super.clone();
		copy.templates = new ArrayList<Template>(templates);
		copy.templateProviders = new ArrayList<TemplateResolver>(templateProviders);
		copy.templateExcludes = templateExcludes;
		return copy;
	}

	/**
	 * Indicate template names to exclude.
	 * 
	 * @param templateExcludes
	 *            a comma-delimited list of names, may include '*' wildcards
	 * @since 1.3
	 */
	public void setTemplateExcludes(String templateExcludes) {
		this.templateExcludes = templateExcludes;
	}

	/**
	 * Indicate template names to exclude.
	 * 
	 * @return a comma-delimited list of names, may include '*' wildcards, or null if none are to be excluded
	 * @since 1.3
	 */
	@Override
	public String getTemplateExcludes() {
		return templateExcludes;
	}

}
