/*******************************************************************************
 * Copyright (c) 2007, 2009 David Green and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     David Green - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.internal.wikitext.ui.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.IPreferenceStore;
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
import org.eclipse.mylyn.internal.wikitext.ui.editor.assist.AnchorCompletionProcessor;
import org.eclipse.mylyn.internal.wikitext.ui.editor.assist.MarkupTemplateCompletionProcessor;
import org.eclipse.mylyn.internal.wikitext.ui.editor.assist.MultiplexingContentAssistProcessor;
import org.eclipse.mylyn.internal.wikitext.ui.editor.outline.QuickOutlinePopupDialog;
import org.eclipse.mylyn.internal.wikitext.ui.editor.reconciler.MarkupMonoReconciler;
import org.eclipse.mylyn.internal.wikitext.ui.editor.reconciler.MarkupValidationReconcilingStrategy;
import org.eclipse.mylyn.internal.wikitext.ui.editor.reconciler.MultiReconcilingStrategy;
import org.eclipse.mylyn.internal.wikitext.ui.editor.syntax.FastMarkupPartitioner;
import org.eclipse.mylyn.internal.wikitext.ui.editor.syntax.MarkupDamagerRepairer;
import org.eclipse.mylyn.internal.wikitext.ui.editor.syntax.MarkupTokenScanner;
import org.eclipse.mylyn.wikitext.core.parser.markup.MarkupLanguage;
import org.eclipse.mylyn.wikitext.core.parser.outline.OutlineItem;
import org.eclipse.mylyn.wikitext.core.parser.outline.OutlineParser;
import org.eclipse.mylyn.wikitext.ui.viewer.AbstractTextSourceViewerConfiguration;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.IShowInTarget;
import org.eclipse.ui.texteditor.HippieProposalProcessor;

/**
 * A source viewer configuration suitable for installing on a markup editor
 * 
 * @author David Green
 */
public class MarkupSourceViewerConfiguration extends AbstractTextSourceViewerConfiguration {

	private ITokenScanner scanner;

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

	public MarkupSourceViewerConfiguration(IPreferenceStore preferenceStore) {
		super(preferenceStore);
		defaultFont = JFaceResources.getDefaultFont();
	}

	protected ITokenScanner getMarkupScanner() {
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
		HippieProposalProcessor hippieProcessor = new HippieProposalProcessor();

		MultiplexingContentAssistProcessor processor = new MultiplexingContentAssistProcessor();
		if (anchorCompletionProcessor != null) {
			processor.addDelegate(anchorCompletionProcessor);
		}
		processor.addDelegate(completionProcessor);
		processor.addDelegate(hippieProcessor);

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

	public void setMarkupLanguage(MarkupLanguage markupLanguage) {
		this.markupLanguage = markupLanguage;
		if (completionProcessor != null) {
			completionProcessor.setMarkupLanguage(markupLanguage);
		}
		if (markupValidationReconcilingStrategy != null) {
			markupValidationReconcilingStrategy.setMarkupLanguage(markupLanguage);
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

	public void setFile(IFile file) {
		this.file = file;
		if (markupValidationReconcilingStrategy != null) {
			markupValidationReconcilingStrategy.setResource(file);
		}
	}

	@Override
	public ITextHover getTextHover(ISourceViewer sourceViewer, String contentType) {
		if (textHover == null) {
			textHover = new DefaultTextHover(sourceViewer);
		}
		return textHover;
	}

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

	public void setOutline(OutlineItem outlineModel) {
		this.outline = outlineModel;
		if (anchorCompletionProcessor != null) {
			anchorCompletionProcessor.setOutline(outline);
		}
	}

	public Font getDefaultFont() {
		return defaultFont;
	}

	public void setDefaultFont(Font defaultFont) {
		this.defaultFont = defaultFont;
	}

	public Font getDefaultMonospaceFont() {
		return defaultMonospaceFont;
	}

	public void setDefaultMonospaceFont(Font defaultMonospaceFont) {
		this.defaultMonospaceFont = defaultMonospaceFont;
	}

	public IShowInTarget getShowInTarget() {
		return showInTarget;
	}

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
						return outlineParser.parse(markup);
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

}
