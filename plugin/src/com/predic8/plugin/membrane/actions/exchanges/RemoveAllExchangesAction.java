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

package com.predic8.plugin.membrane.actions.exchanges;

import com.predic8.plugin.membrane.MembraneUIPlugin;
import com.predic8.plugin.membrane.PlatformUtil;
import com.predic8.plugin.membrane.actions.rules.AbstractProxyAction;
import com.predic8.plugin.membrane.resources.ImageKeys;

public class RemoveAllExchangesAction extends AbstractProxyAction {

	public RemoveAllExchangesAction() {
		super("remove all exhanges action", "Remove all Exchanges");
		setImageDescriptor(MembraneUIPlugin.getDefault().getImageRegistry().getDescriptor(ImageKeys.IMAGE_DELETE_EXCHANGE));
	}
	
	public void run() {
		PlatformUtil.getRouter().getExchangeStore().removeAllExchanges(selectedProxy);
	}

}
