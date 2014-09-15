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

import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.internal.text.html.BrowserInformationControl;
import org.eclipse.jface.text.AbstractReusableInformationControlCreator;
import org.eclipse.jface.text.DefaultInformationControl;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;

public class PresenterControlCreator extends
		AbstractReusableInformationControlCreator {

	@Override
	public IInformationControl doCreateInformationControl(Shell parent) {
		if (BrowserInformationControl.isAvailable(parent)) {
			ToolBarManager tbm = new ToolBarManager(SWT.FLAT);
			WebResourcesBrowserInformationControl control = new WebResourcesBrowserInformationControl(
					parent, null, tbm);
			tbm.update(true);
			WebResourcesHoverControlCreator.addLinkListener(control);
			return control;
		} else {
			return new DefaultInformationControl(parent, true);
		}
	}
}
