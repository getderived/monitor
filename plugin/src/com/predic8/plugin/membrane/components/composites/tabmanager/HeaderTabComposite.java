/* Copyright 2009 predic8 GmbH, www.predic8.com

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License. */

package com.predic8.plugin.membrane.components.composites.tabmanager;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TabFolder;

import com.predic8.membrane.core.http.Header;
import com.predic8.membrane.core.http.HeaderField;
import com.predic8.membrane.core.http.HeaderName;
import com.predic8.membrane.core.http.Message;
import com.predic8.plugin.membrane.viewers.HeaderTableViewer;

public class HeaderTabComposite extends AbstractTabComposite {

	protected HeaderTableViewer tableViewer;
	
	public HeaderTabComposite(TabFolder parent) {
		super(parent, "HTTP");
		tableViewer = new HeaderTableViewer(this, SWT.BORDER | SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL | SWT.FULL_SELECTION);
	}

	@Override
	public void updateInternal(Message msg) {
		tableViewer.setInput(msg);
	}
	
	public void setWidgetEditable(boolean editable) {
		tableViewer.setEditable(editable);
	}
	
	public void updateWidget(Message msg, byte[] inputBodyBytes) {
		msg.getHeader().setValue(Header.CONTENT_LENGTH, Integer.toString(inputBodyBytes.length));
		HeaderField hf = msg.getHeader().getValues(new HeaderName(Header.CONTENT_LENGTH)).get(0);
		tableViewer.update(hf, null);
	}
	
}
