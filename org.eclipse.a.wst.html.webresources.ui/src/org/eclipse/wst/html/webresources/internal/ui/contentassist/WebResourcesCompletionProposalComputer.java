/**
 *  Copyright (c) 2013-2014 Angelo ZERR.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *  Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 */
package org.eclipse.wst.html.webresources.internal.ui.contentassist;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.contentassist.CompletionProposal;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.wst.html.webresources.core.DOMHelper;
import org.eclipse.wst.html.webresources.core.InformationHelper;
import org.eclipse.wst.html.webresources.core.WebResourcesTextRegion;
import org.eclipse.wst.html.webresources.core.WebResourcesType;
import org.eclipse.wst.html.webresources.core.providers.IURIResolver;
import org.eclipse.wst.html.webresources.core.providers.IWebResourcesCollector;
import org.eclipse.wst.html.webresources.core.providers.IWebResourcesProvider;
import org.eclipse.wst.html.webresources.core.providers.WebResourcesProvidersManager;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.ui.contentassist.CompletionProposalInvocationContext;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.ui.internal.contentassist.ContentAssistRequest;
import org.eclipse.wst.xml.ui.internal.contentassist.DefaultXMLCompletionProposalComputer;

/**
 * Completion proposal computer for Web resources inside HTML:
 * 
 * <ul>
 * <li>@class : completion for CSS class name inside @class attribute.</li>
 * <li>@id : completion for CSS ID inside @id attribute.</li>
 * <li>script/@src : completion for JS files inside script/@src attribute.</li>
 * <li>link/@href : completion for CSS files inside link/@href attribute.</li>
 * <li>img/@src : completion for Images files inside img/@src attribute.</li>
 * </ul>
 *
 */
public class WebResourcesCompletionProposalComputer extends
		DefaultXMLCompletionProposalComputer {

	@Override
	protected void addAttributeValueProposals(
			ContentAssistRequest contentAssistRequest,
			CompletionProposalInvocationContext context) {
		IDOMNode element = (IDOMNode) contentAssistRequest.getNode();

		IStructuredDocumentRegion documentRegion = contentAssistRequest
				.getDocumentRegion();
		WebResourcesTextRegion attrValueRegion = DOMHelper.getTextRegion(
				documentRegion, context.getInvocationOffset());
		if (attrValueRegion != null) {
			String attrValue = DOMHelper.getAttrValue(documentRegion
					.getText(attrValueRegion.getRegion()));
			switch (attrValueRegion.getType()) {
			case CSS_CLASS_NAME:
			case CSS_ID:
				// Completion for CSS class name or id.
				processCSSCompletion(contentAssistRequest, context, attrValue,
						attrValueRegion);
				break;
			case SCRIPT_SRC:
			case LINK_HREF:
			case IMG_SRC:
				// Completion for js, css, images files
				processFilesCompletion(contentAssistRequest, context,
						attrValue, attrValueRegion);
				break;
			}

		}
	}

	/**
	 * Process completion for CSS class name or id.
	 * 
	 * @param contentAssistRequest
	 * @param context
	 * @param attrValue
	 * @param attrValueRegion
	 */
	private void processCSSCompletion(
			ContentAssistRequest contentAssistRequest,
			CompletionProposalInvocationContext context, String attrValue,
			WebResourcesTextRegion attrValueRegion) {
		CSSContentAssistTraverser traverser = new CSSContentAssistTraverser(
				contentAssistRequest, context.getInvocationOffset(), attrValue,
				attrValueRegion.getType());
		traverser.process();
	}

	/**
	 * Process completion for js, css, images files.
	 * 
	 * @param contentAssistRequest
	 * @param context
	 * @param attrValue
	 * @param attrValueRegion
	 */
	private void processFilesCompletion(
			final ContentAssistRequest contentAssistRequest,
			final CompletionProposalInvocationContext context,
			String attrValue, WebResourcesTextRegion attrValueRegion) {
		IDOMNode node = (IDOMNode) contentAssistRequest.getNode();
		final String matchingString = DOMHelper
				.getAttrValue(contentAssistRequest.getMatchString());
		final int replacementLength = attrValue.length();
		final int replacementOffset = context.getInvocationOffset()
				- matchingString.length();
		WebResourcesType type = attrValueRegion.getType().getType();
		WebResourcesProvidersManager.collect(node, type,
				new IWebResourcesCollector() {

					@Override
					public void add(IResource resource, IDOMNode htmlNode,
							IFile htmlFile, IWebResourcesProvider provider) {
						IURIResolver resolver = provider.getResolver(htmlNode,
								htmlFile);
						IPath location = resolver.resolve(resource, htmlFile);
						String fileName = location.toString();
						if (location.toString().startsWith(matchingString)) {
							String info = InformationHelper
									.getInformation(resource);
							String displayString = resource
									.getProjectRelativePath().toString();
							int cursorPosition = fileName.length();
							Image image = InformationHelper
									.getImage(resource);
							CompletionProposal proposal = new CompletionProposal(
									fileName, replacementOffset,
									replacementLength, cursorPosition, image,
									displayString, null, info);
							contentAssistRequest.addProposal(proposal);
						}
					}
				});
	}
}
