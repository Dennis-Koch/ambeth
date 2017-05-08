package com.koch.ambeth.merge.server.change;

/*-
 * #%L
 * jambeth-merge-server
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.merge.transfer.AbstractChangeContainer;

public class LinkContainer extends AbstractChangeContainer {
	protected ILinkChangeCommand command;

	protected String tableName;

	public ILinkChangeCommand getCommand() {
		return command;
	}

	public void setCommand(ILinkChangeCommand command) {
		this.command = command;
		setReference(command.getReference());
		tableName = this.command.getDirectedLink().getLink().getMetaData().getTableName();
	}

	public String getTableName() {
		return tableName;
	}
}
