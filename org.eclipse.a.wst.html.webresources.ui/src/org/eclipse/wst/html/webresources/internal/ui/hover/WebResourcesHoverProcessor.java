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
package org.eclipse.wst.html.webresources.internal.ui.hover;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.wst.html.webresources.core.DOMHelper;
import org.eclipse.wst.html.webresources.core.InformationHelper;
import org.eclipse.wst.html.webresources.core.WebResourceRegion;
import org.eclipse.wst.html.webresources.core.WebResourcesTextRegion;
import org.eclipse.wst.html.webresources.core.WebResourcesType;
import org.eclipse.wst.html.webresources.core.providers.IURIResolver;
import org.eclipse.wst.html.webresources.core.providers.IWebResourcesCollector;
import org.eclipse.wst.html.webresources.core.providers.IWebResourcesProvider;
import org.eclipse.wst.html.webresources.core.providers.WebResourcesProvidersManager;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.sse.ui.internal.contentassist.ContentAssistUtils;
import org.eclipse.wst.sse.ui.internal.taginfo.AbstractHoverProcessor;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;

/**
 * Hover processor for Web resources inside HTML:
 * 
 * <ul>
 * <li>@class : hover for CSS class name inside @class attribute.</li>
 * <li>@id : hover for CSS ID inside @id attribute.</li>
 * <li>script/@src : hover for JS files inside script/@src attribute.</li>
 * <li>link/@href : hover for CSS files inside link/@href attribute.</li>
 * <li>img/@src : hover for Images files inside img/@src attribute.</li>
 * </ul>
 *
 */
public class WebResourcesHoverProcessor extends AbstractHoverProcessor {

	@Override
	public String getHoverInfo(ITextViewer textViewer, IRegion hoverRegion) {
		if (hoverRegion instanceof WebResourceRegion) {
			WebResourceRegion resourceRegion = (WebResourceRegion) hoverRegion;
			IDOMNode xmlnode = (IDOMNode) ContentAssistUtils.getNodeAt(
					textViewer, hoverRegion.getOffset());
			switch (resourceRegion.getType()) {
			case CSS_CLASS_NAME:
			case CSS_ID:
				return getCSSHoverInfo(resourceRegion, xmlnode);
			default:
				return getFileHoverInfo(resourceRegion, xmlnode);
			}
		}
		return null;
	}

	private String getCSSHoverInfo(WebResourceRegion hoverRegion,
			IDOMNode xmlnode) {
		CSSHoverTraverser traverser = new CSSHoverTraverser(xmlnode,
				hoverRegion);
		traverser.process();
		return traverser.getInfo();
	}

	private String getFileHoverInfo(WebResourceRegion resourceRegion,
			IDOMNode xmlnode) {
		final String fileName = resourceRegion.getValue();
		final StringBuilder info = new StringBuilder();
		WebResourcesType type = resourceRegion.getType().getType();
		WebResourcesProvidersManager.collect(xmlnode, type,
				new IWebResourcesCollector() {
					@Override
					public void add(IResource resource, IDOMNode htmlNode,
							IFile htmlFile, IWebResourcesProvider provider) {
						IURIResolver resolver = provider.getResolver(htmlNode,
								htmlFile);
						IPath resourceFileLoc = resolver.resolve(resource,
								htmlFile);
						if (resourceFileLoc.toString().equals(fileName)) {
							InformationHelper.addInformation(resource, info);
						}
					}
				});
		return info.length() > 0 ? info.toString() : null;
	}

	@Override
	public IRegion getHoverRegion(ITextViewer textViewer, int offset) {
		if ((textViewer == null) || (textViewer.getDocument() == null)) {
			return null;
		}

		IStructuredDocumentRegion documentRegion = ContentAssistUtils
				.getStructuredDocumentRegion(textViewer, offset);
		WebResourcesTextRegion attrValueRegion = DOMHelper.getTextRegion(
				documentRegion, offset);
		if (attrValueRegion != null) {
			switch (attrValueRegion.getType()) {
			case CSS_CLASS_NAME:
			case CSS_ID:
				return DOMHelper.getCSSRegion(attrValueRegion, documentRegion,
						textViewer.getDocument(), offset);
			case SCRIPT_SRC:
			case LINK_HREF:
			case IMG_SRC:
				return DOMHelper.getAttrValueRegion(attrValueRegion,
						documentRegion, textViewer.getDocument(), offset);
			}
		}
		return null;
	}
}
