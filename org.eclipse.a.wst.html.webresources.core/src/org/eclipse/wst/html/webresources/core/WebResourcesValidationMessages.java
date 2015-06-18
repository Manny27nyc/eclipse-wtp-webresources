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
package org.eclipse.wst.html.webresources.core;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.osgi.util.NLS;

/**
 * Web resources validation messages.
 * 
 */
public final class WebResourcesValidationMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.wst.html.webresources.core.WebResourcesValidationMessages"; //$NON-NLS-1$

	private static ResourceBundle fResourceBundle;

	// Validation
	public static String Validation_CSS_CLASS_UNDEFINED;
	public static String Validation_CSS_ID_UNDEFINED;
	public static String Validation_FILE_JS_UNDEFINED;
	public static String Validation_FILE_CSS_UNDEFINED;
	public static String Validation_FILE_IMG_UNDEFINED;
	public static String Validation_URL_JS_UNDEFINED;
	public static String Validation_URL_CSS_UNDEFINED;
	public static String Validation_URL_IMG_UNDEFINED;

	private WebResourcesValidationMessages() {
	}

	public static ResourceBundle getResourceBundle() {
		try {
			if (fResourceBundle == null)
				fResourceBundle = ResourceBundle.getBundle(BUNDLE_NAME);
		} catch (MissingResourceException x) {
			fResourceBundle = null;
		}
		return fResourceBundle;
	}

	static {
		NLS.initializeMessages(BUNDLE_NAME,
				WebResourcesValidationMessages.class);
	}
}
