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
package org.eclipse.mylyn.wikitext.ui.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.text.DefaultTextHover;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextHover;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.Region;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.contentassist.IContentAssistant;
import org.eclipse.jface.text.hyperlink.IHyperlinkDetector;
import org.eclipse.jface.text.information.IInformationPresenter;
import org.eclipse.jface.text.information.IInformationProvider;
import org.eclipse.jface.text.information.IInformationProviderExtension;
import org.eclipse.jface.text.information.IInformationProviderExtension2;
import org.eclipse.jface.text.information.InformationPresenter;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.reconciler.IReconciler;
import org.eclipse.jface.text.reconciler.IReconcilingStrategy;
import org.eclipse.jface.text.reconciler.MonoReconciler;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.mylyn.internal.wikitext.ui.WikiTextUiPlugin;
import org.eclipse.mylyn.internal.wikitext.ui.editor.assist.AnchorCompletionProcessor;
import org.eclipse.mylyn.internal.wikitext.ui.editor.assist.MarkupTemplateCompletionProcessor;
import org.eclipse.mylyn.internal.wikitext.ui.editor.assist.MultiplexingContentAssistProcessor;
import org.eclipse.mylyn.internal.wikitext.ui.editor.outline.QuickOutlinePopupDialog;
import org.eclipse.mylyn.internal.wikitext.ui.editor.reconciler.MarkupMonoReconciler;
import org.eclipse.mylyn.internal.wikitext.ui.editor.reconciler.MarkupValidationReconcilingStrategy;
import org.eclipse.mylyn.internal.wikitext.ui.editor.reconciler.MultiReconcilingStrategy;
import org.eclipse.mylyn.internal.wikitext.ui.editor.syntax.FastMarkupPartitioner;
import org.eclipse.mylyn.internal.wikitext.ui.editor.syntax.MarkupDamagerRepairer;
import org.eclipse.mylyn.internal.wikitext.ui.editor.syntax.MarkupHyperlinkDetector;
import org.eclipse.mylyn.internal.wikitext.ui.editor.syntax.MarkupTokenScanner;
import org.eclipse.mylyn.internal.wikitext.ui.util.WikiTextUiResources;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.eclipse.mylyn.wikitext.core.parser.outline.OutlineItem;
import org.eclipse.mylyn.wikitext.core.parser.outline.OutlineParser;
import org.eclipse.mylyn.wikitext.ui.viewer.AbstractTextSourceViewerConfiguration;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.texteditor.HippieProposalProcessor;
import org.eclipse.ui.themes.IThemeManager;

/**
 * A source viewer configuration suitable for installing on a markup editor
 * 
 * @author David Green
 * @since 1.1
 */
public class MarkupSourceViewerConfiguration extends AbstractTextSourceViewerConfiguration {

	private MarkupTokenScanner scanner;

	private MarkupTemplateCompletionProcessor completionProcessor;

	private AnchorCompletionProcessor anchorCompletionProcessor;

	private MarkupLanguage markupLanguage;

	private MarkupValidationReconcilingStrategy markupValidationReconcilingStrategy;

	private IFile file;

	private ITextHover textHover;

	private OutlineItem outline;

	private Font defaultFont;

	private Font defaultMonospaceFont;

	private InformationPresenter informationPresenter;

	private IShowInTarget showInTarget;

	private String fontPreference;

	private String monospaceFontPreference;

	private MarkupHyperlinkDetector markupHyperlinkDetector;

	private boolean enableHippieContentAssist = true;

	private boolean enableSelfContainedIncrementalFind = false;

	/**
	 * @since 1.3
	 * @param preferenceStore
	 * @param fontPreference
	 *            the preference key of the text font
	 * @param monospaceFontPreference
	 *            the preference key of the monospace text font
	 * @see #initializeDefaultFonts()
	 */
	public MarkupSourceViewerConfiguration(IPreferenceStore preferenceStore, String textFontPreference,
			String monospaceFontPreference) {
		super(preferenceStore);
		this.fontPreference = textFontPreference;
		this.monospaceFontPreference = monospaceFontPreference;
		initializeDefaultFonts();
	}

	/**
	 * Initialize with the default font preference keys
	 */
	public MarkupSourceViewerConfiguration(IPreferenceStore preferenceStore) {
		this(preferenceStore, WikiTextUiResources.PREFERENCE_TEXT_FONT, WikiTextUiResources.PREFERENCE_MONOSPACE_FONT);
	}

	@Override
	protected List<IHyperlinkDetector> createCustomHyperlinkDetectors(ISourceViewer sourceViewer) {
		List<IHyperlinkDetector> detectors = new ArrayList<IHyperlinkDetector>();
		if (markupHyperlinkDetector == null) {
			markupHyperlinkDetector = new MarkupHyperlinkDetector();
			markupHyperlinkDetector.setMarkupLanguage(markupLanguage);
			markupHyperlinkDetector.setFile(file);
		}
		detectors.add(markupHyperlinkDetector);
		detectors.addAll(super.createCustomHyperlinkDetectors(sourceViewer));
		return detectors;
	}

	/**
	 * Initialize default fonts. Causes this to re-read font preferences from the preference store. Calling this method
	 * should only be necessary if font preferences have changed, or if the font preference keys have changed.
	 * 
	 * @since 1.3
	 * @see #getFontPreference()
	 * @see #getMonospaceFontPreference()
	 */
	public void initializeDefaultFonts() {
		Font defaultFont = null;
		Font defaultMonospaceFont = null;
		if (WikiTextUiPlugin.getDefault() != null) {
			IThemeManager themeManager = PlatformUI.getWorkbench().getThemeManager();
			FontRegistry fontRegistry = themeManager.getCurrentTheme().getFontRegistry();
			defaultFont = fontRegistry.get(fontPreference);
			defaultMonospaceFont = fontRegistry.get(monospaceFontPreference);
		}
		if (defaultFont == null) {
			// could be the case when running stand-alone
			defaultFont = JFaceResources.getDefaultFont();
		}
		if (this.defaultFont != defaultFont || this.defaultMonospaceFont != defaultMonospaceFont) {
			this.defaultFont = defaultFont;
			this.defaultMonospaceFont = defaultMonospaceFont;
			if (scanner != null) {
				scanner.resetFonts(defaultFont, defaultMonospaceFont);
			}
		}
	}

	/**
	 * the font preference key for the text font
	 * 
	 * @see #initializeDefaultFonts()
	 * @since 1.3
	 */
	public String getFontPreference() {
		return fontPreference;
	}

	/**
	 * the font preference key for the text font
	 * 
	 * @see #initializeDefaultFonts()
	 * @since 1.3
	 */
	public void setFontPreference(String textFontPreference) {
		this.fontPreference = textFontPreference;
	}

	/**
	 * the monospace font preference key for the text font
	 * 
	 * @see #initializeDefaultFonts()
	 * @since 1.3
	 */
	public String getMonospaceFontPreference() {
		return monospaceFontPreference;
	}

	/**
	 * the monospace font preference key for the text font
	 * 
	 * @see #initializeDefaultFonts()
	 * @since 1.3
	 */
	public void setMonospaceFontPreference(String monospaceFontPreference) {
		this.monospaceFontPreference = monospaceFontPreference;
	}

	/**
	 * @since 1.1
	 */
	public ITokenScanner getMarkupScanner() {
		if (scanner == null) {
			scanner = new MarkupTokenScanner(defaultFont, defaultMonospaceFont);
		}
		return scanner;
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(ISourceViewer sourceViewer) {
		PresentationReconciler reconciler = new PresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		MarkupDamagerRepairer damagerRepairer = new MarkupDamagerRepairer(getMarkupScanner());
		for (String partitionType : FastMarkupPartitioner.ALL_CONTENT_TYPES) {
			reconciler.setDamager(damagerRepairer, partitionType);
			reconciler.setRepairer(damagerRepairer, partitionType);
		}
		reconciler.setDamager(damagerRepairer, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(damagerRepairer, IDocument.DEFAULT_CONTENT_TYPE);

		return reconciler;
	}

	@Override
	public IContentAssistant getContentAssistant(ISourceViewer sourceViewer) {
		if (completionProcessor == null) {
			completionProcessor = new MarkupTemplateCompletionProcessor();
			completionProcessor.setMarkupLanguage(markupLanguage);
		}
		if (anchorCompletionProcessor == null && outline != null) {
			anchorCompletionProcessor = new AnchorCompletionProcessor();
			anchorCompletionProcessor.setOutline(outline);
		}
		MultiplexingContentAssistProcessor processor = new MultiplexingContentAssistProcessor();
		if (anchorCompletionProcessor != null) {
			processor.addDelegate(anchorCompletionProcessor);
		}
		processor.addDelegate(completionProcessor);

		if (enableHippieContentAssist) {
			HippieProposalProcessor hippieProcessor = new HippieProposalProcessor();
			processor.addDelegate(hippieProcessor);
		}

		IContentAssistProcessor[] processors = createContentAssistProcessors();
		if (processors != null) {
			for (IContentAssistProcessor cap : processors) {
				processor.addDelegate(cap);
			}
		}

		ContentAssistant assistant = new ContentAssistant();
		assistant.enableAutoActivation(true);
		assistant.enableAutoInsert(true);
		assistant.setContentAssistProcessor(processor, IDocument.DEFAULT_CONTENT_TYPE);

		for (String partitionType : FastMarkupPartitioner.ALL_CONTENT_TYPES) {
			assistant.setContentAssistProcessor(processor, partitionType);
		}

		return assistant;
	}

	/**
	 * subclasses may override this method to create additional content assist processors.
	 * 
	 * @return processors, or null if there are none.
	 */
	protected IContentAssistProcessor[] createContentAssistProcessors() {
		return null;
	}

	/**
	 * Set the markup language of the configuration. Causes the completion processor, validating reconciling strategy
	 * and other configuration elements to be aware of the markup language in use. This may be called more than once
	 * during the lifecycle of the editor.
	 * 
	 * @param markupLanguage
	 *            the markup language
	 */
	public void setMarkupLanguage(MarkupLanguage markupLanguage) {
		this.markupLanguage = markupLanguage;
		if (completionProcessor != null) {
			completionProcessor.setMarkupLanguage(markupLanguage);
		}
		if (markupValidationReconcilingStrategy != null) {
			markupValidationReconcilingStrategy.setMarkupLanguage(markupLanguage);
		}
		if (markupHyperlinkDetector != null) {
			markupHyperlinkDetector.setMarkupLanguage(markupLanguage);
		}
	}

	@Override
	public IReconciler getReconciler(ISourceViewer sourceViewer) {
		IReconcilingStrategy strategy;
		{
			if (markupValidationReconcilingStrategy == null) {
				markupValidationReconcilingStrategy = new MarkupValidationReconcilingStrategy(sourceViewer);
				markupValidationReconcilingStrategy.setMarkupLanguage(markupLanguage);
				markupValidationReconcilingStrategy.setResource(file);
			}
			IReconciler reconciler = super.getReconciler(sourceViewer);
			if (reconciler != null) {
				MultiReconcilingStrategy multiStrategy = new MultiReconcilingStrategy();
				for (String contentType : FastMarkupPartitioner.ALL_CONTENT_TYPES) {
					maybeAddReconcilingStrategyForContentType(multiStrategy, reconciler, contentType);
				}
				maybeAddReconcilingStrategyForContentType(multiStrategy, reconciler, IDocument.DEFAULT_CONTENT_TYPE);
				multiStrategy.add(markupValidationReconcilingStrategy);
				strategy = multiStrategy;
			} else {
				strategy = markupValidationReconcilingStrategy;
			}
		}
		MonoReconciler reconciler = new MarkupMonoReconciler(strategy, false);
		reconciler.setIsIncrementalReconciler(false);
		reconciler.setProgressMonitor(new NullProgressMonitor());
		reconciler.setDelay(500);
		return reconciler;
	}

	private void maybeAddReconcilingStrategyForContentType(MultiReconcilingStrategy multiStrategy,
			IReconciler reconciler, String contentType) {
		final IReconcilingStrategy reconcilingStrategy = reconciler.getReconcilingStrategy(contentType);
		if (reconcilingStrategy != null && !multiStrategy.contains(reconcilingStrategy)) {
			multiStrategy.add(reconcilingStrategy);
		}
	}

	/**
	 * Set the file being edited. If a file is being edited this allows for validation to create markers on the file.
	 * Some editors are not file-based and thus need not invoke this method.
	 * 
	 * @param file
	 *            the file, which may be null.
	 */
	public void setFile(IFile file) {
		this.file = file;
		if (markupValidationReconcilingStrategy != null) {
			markupValidationReconcilingStrategy.setResource(file);
		}
		if (markupHyperlinkDetector != null) {
			markupHyperlinkDetector.setFile(file);
		}
	}

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		if (textHover == null) {
			textHover = new DefaultTextHover(sourceViewer);
		}
		return textHover;
	}

	/**
	 * provide access to an information presenter that can be used to pop-up a quick outline. Source viewers should
	 * configure as follows:
	 * 
	 * <pre>
	 * public void configure(SourceViewerConfiguration configuration) {
	 * 	super.configure(configuration);
	 * 	if (configuration instanceof MarkupSourceViewerConfiguration) {
	 * 		outlinePresenter = ((MarkupSourceViewerConfiguration) configuration).getOutlineInformationPresenter(this);
	 * 		outlinePresenter.install(this);
	 * 	}
	 * }
	 * </pre>
	 * 
	 * @param sourceViewer
	 *            the source viewer for which the presenter should be created
	 * @return the presenter
	 */
	public IInformationPresenter getOutlineInformationPresenter(ISourceViewer sourceViewer) {
		if (informationPresenter == null) {
			IInformationControlCreator controlCreator = getOutlineInformationControlCreator();
			informationPresenter = new InformationPresenter(controlCreator);
			informationPresenter.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

			// Register information provider
			IInformationProvider provider = new InformationProvider(controlCreator);
			String[] contentTypes = getConfiguredContentTypes(sourceViewer);
			for (String contentType : contentTypes) {
				informationPresenter.setInformationProvider(provider, contentType);
			}

			informationPresenter.setSizeConstraints(60, 20, true, true);
		}
		return informationPresenter;
	}

	@Override
	public String[] getConfiguredContentTypes(ISourceViewer sourceViewer) {
		List<String> contentTypes = new ArrayList<String>(3);
		contentTypes.addAll(Arrays.asList(FastMarkupPartitioner.ALL_CONTENT_TYPES));
		contentTypes.add(IDocument.DEFAULT_CONTENT_TYPE);
		return contentTypes.toArray(new String[contentTypes.size()]);
	}

	protected IInformationControlCreator getOutlineInformationControlCreator() {
		return new IInformationControlCreator() {
			public IInformationControl createInformationControl(Shell parent) {
				QuickOutlinePopupDialog dialog = new QuickOutlinePopupDialog(parent, showInTarget);
				return dialog;
			}
		};
	}

	/**
	 * Set the outline on this configuration. Outlines are used for document-internal references as well as for quick
	 * outline. Editors that call this method must keep the outline up to date as the source document changes. Editors
	 * that do not maintain an outline need not call this method, since the outline will be computed as needed for the
	 * quick outline.
	 * 
	 * @param outlineModel
	 */
	public void setOutline(OutlineItem outlineModel) {
		this.outline = outlineModel;
		if (anchorCompletionProcessor != null) {
			anchorCompletionProcessor.setOutline(outline);
		}
	}

	/**
	 * the default font, as used by the {@link #getMarkupScanner() scanner}.
	 */
	public Font getDefaultFont() {
		return defaultFont;
	}

	/**
	 * The default font, as used by the {@link #getMarkupScanner() scanner}. Note that if a preference store is used
	 * then {@link #setFontPreference(String)} should be used instead.
	 */
	public void setDefaultFont(Font defaultFont) {
		if (defaultFont == null) {
			throw new IllegalArgumentException();
		}
		if (defaultFont != this.defaultFont) {
			this.defaultFont = defaultFont;
			if (scanner != null) {
				scanner.resetFonts(defaultFont, defaultMonospaceFont);
			}
		}
	}

	/**
	 * the default font for monospace text, as used by the {@link #getMarkupScanner() scanner}.
	 */
	public Font getDefaultMonospaceFont() {
		return defaultMonospaceFont;
	}

	/**
	 * the default font for monospace text, as used by the {@link #getMarkupScanner() scanner}. Note that if a
	 * preference store is used then {@link #setMonospaceFontPreference(String)} should be used instead.
	 */
	public void setDefaultMonospaceFont(Font defaultMonospaceFont) {
		if (this.defaultMonospaceFont != defaultMonospaceFont) {
			this.defaultMonospaceFont = defaultMonospaceFont;
			if (scanner != null) {
				scanner.resetFonts(defaultFont, defaultMonospaceFont);
			}
		}
	}

	/**
	 * provide a {@link IShowInTarget show in target} to connect the quick-outline popup with the editor.
	 */
	public IShowInTarget getShowInTarget() {
		return showInTarget;
	}

	/**
	 * provide a {@link IShowInTarget show in target} to connect the quick-outline popup with the editor.
	 */
	public void setShowInTarget(IShowInTarget showInTarget) {
		this.showInTarget = showInTarget;
	}

	private class InformationProvider implements IInformationProvider, IInformationProviderExtension,
			IInformationProviderExtension2 {

		private final IInformationControlCreator controlCreator;

		public InformationProvider(IInformationControlCreator controlCreator) {
			this.controlCreator = controlCreator;
		}

		@Deprecated
		public String getInformation(ITextViewer textViewer, IRegion subject) {
			return getInformation2(textViewer, subject).toString();
		}

		public Object getInformation2(ITextViewer textViewer, IRegion subject) {
			if (outline == null) {
				// If the outline was not set then parse it.  This can happen in a task editor
				if (markupLanguage != null) {
					IDocument document = textViewer.getDocument();
					if (document != null && document.getLength() > 0) {
						MarkupLanguage language = markupLanguage.clone();
						OutlineParser outlineParser = new OutlineParser();
						outlineParser.setMarkupLanguage(language.clone());
						String markup = document.get();
						final OutlineItem outline = outlineParser.parse(markup);
						if (MarkupSourceViewerConfiguration.this.file != null) {
							outline.setResourcePath(MarkupSourceViewerConfiguration.this.file.getFullPath().toString());
						}
						return outline;
					}
				}
			}
			return outline;
		}

		public IRegion getSubject(ITextViewer textViewer, int offset) {
			return new Region(offset, 0);
		}

		public IInformationControlCreator getInformationPresenterControlCreator() {
			return controlCreator;
		}
	}

	/**
	 * Indicate if Hippie content assist should be enabled. The default is true.
	 * 
	 * @since 1.4
	 * @see HippieProposalProcessor
	 */
	public boolean isEnableHippieContentAssist() {
		return enableHippieContentAssist;
	}

	/**
	 * Indicate if Hippie content assist should be enabled.
	 * 
	 * @since 1.4
	 */
	public void setEnableHippieContentAssist(boolean enableHippieContentAssist) {
		this.enableHippieContentAssist = enableHippieContentAssist;
	}

	/**
	 * Indicate if incremental find should be supported in a self-contained manner. For use when SourceViewer is not
	 * used in a {@link TextEditor}. Defaults to false.
	 * 
	 * @since 1.6
	 */
	public boolean isEnableSelfContainedIncrementalFind() {
		return enableSelfContainedIncrementalFind;
	}

	/**
	 * Indicate if incremental find should be supported in a self-contained manner. For use when SourceViewer is not
	 * used in a {@link TextEditor}.
	 * 
	 * @since 1.6
	 */
	public void setEnableSelfContainedIncrementalFind(boolean enableSelfContainedIncrementalFind) {
		this.enableSelfContainedIncrementalFind = enableSelfContainedIncrementalFind;
	}

}
