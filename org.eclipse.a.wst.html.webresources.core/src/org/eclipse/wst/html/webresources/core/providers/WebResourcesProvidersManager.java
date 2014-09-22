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
package org.eclipse.wst.html.webresources.core.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.wst.html.webresources.core.WebResourceType;
import org.eclipse.wst.html.webresources.core.utils.DOMHelper;
import org.eclipse.wst.html.webresources.internal.core.Trace;
import org.eclipse.wst.html.webresources.internal.core.WebResourcesCorePlugin;
import org.eclipse.wst.html.webresources.internal.core.providers.WebResourcesProviderType;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;

public class WebResourcesProvidersManager {

	private static final String PLUGIN_ID = WebResourcesCorePlugin.PLUGIN_ID;
	private static final String EXTENSION_POINT_ID = "webResourcesProviders";

	private Map<WebResourceType, Collection<WebResourcesProviderType>> providerTypes;

	private static final WebResourcesProvidersManager INSTANCE = new WebResourcesProvidersManager();

	public static WebResourcesProvidersManager getInstance() {
		return INSTANCE;
	}

	public void collect(IDOMNode htmlNode, WebResourceType resourcesType,
			IWebResourcesCollector collector) {
		Collection<WebResourcesProviderType> providerTypes = getProviderTypes(resourcesType);
		IFile htmlFile = DOMHelper.getFile(htmlNode);
		for (WebResourcesProviderType providerType : providerTypes) {
			providerType.collect(htmlNode, htmlFile, collector);
		}
	}

	private Collection<WebResourcesProviderType> getProviderTypes(
			WebResourceType resourcesType) {
		Collection<WebResourcesProviderType> providerTypes = getProvidersMap()
				.get(resourcesType);
		if (providerTypes != null) {
			return providerTypes;
		}
		return Collections.emptyList();
	}

	private Map<WebResourceType, Collection<WebResourcesProviderType>> getProvidersMap() {
		if (providerTypes == null) {
			providerTypes = loadProvidersMap();
		}
		return providerTypes;
	}

	private synchronized Map<WebResourceType, Collection<WebResourcesProviderType>> loadProvidersMap() {
		if (providerTypes != null) {
			return providerTypes;
		}
		Map<WebResourceType, Collection<WebResourcesProviderType>> map = new HashMap<WebResourceType, Collection<WebResourcesProviderType>>();
		IExtensionPoint point = Platform.getExtensionRegistry()
				.getExtensionPoint(PLUGIN_ID, EXTENSION_POINT_ID);
		if (point != null) {
			IConfigurationElement[] elements = point.getConfigurationElements();
			for (int i = 0; i < elements.length; i++) {
				readElement(elements[i], map);
			}
		}
		return map;
	}

	private void readElement(IConfigurationElement element,
			Map<WebResourceType, Collection<WebResourcesProviderType>> map) {
		String className = null;
		try {
			className = element.getAttribute("class");
			Object provider = element.createExecutableExtension("class");
			IWebResourcesProvider resourcesProvider = null;
			IWebResourcesCollectorProvider collectorProvider = null;
			IWebResourcesFileSystemProvider fileSystemProvider = null;
			if (provider instanceof IWebResourcesProvider) {
				resourcesProvider = (IWebResourcesProvider) provider;
			}
			if (provider instanceof IWebResourcesFileSystemProvider) {
				fileSystemProvider = (IWebResourcesFileSystemProvider) provider;
			}
			if (provider instanceof IWebResourcesCollectorProvider) {
				collectorProvider = (IWebResourcesCollectorProvider) provider;
			}
			if (resourcesProvider != null || fileSystemProvider != null) {
				String[] types = element.getAttribute("types").split(",");
				for (int i = 0; i < types.length; i++) {
					WebResourceType resourcesType = WebResourceType
							.get(types[i].trim());
					WebResourcesProviderType providerType = new WebResourcesProviderType(
							resourcesProvider, fileSystemProvider,
							collectorProvider, resourcesType);
					Collection<WebResourcesProviderType> providerTypes = map
							.get(resourcesType);
					if (providerTypes == null) {
						providerTypes = new ArrayList<WebResourcesProviderType>();
						map.put(resourcesType, providerTypes);
					}
					providerTypes.add(providerType);
				}
			}
		} catch (Throwable t) {
			Trace.trace(
					Trace.SEVERE,
					"  Could not load web resources providers: " + className != null ? className
							: "", t);
		}
	}
}
