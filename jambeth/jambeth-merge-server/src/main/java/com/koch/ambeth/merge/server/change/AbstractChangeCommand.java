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

import com.koch.ambeth.merge.model.IChangeContainer;
import com.koch.ambeth.merge.model.IPrimitiveUpdateItem;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.persistence.api.ITable;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.ILinkedMap;

public abstract class AbstractChangeCommand implements IChangeCommand {
    protected final IObjRef reference;

    protected ITable table;

    public AbstractChangeCommand(IObjRef reference) {
        this.reference = reference;
    }

    @Override
    public void configureFromContainer(IChangeContainer changeContainer, ITable table) {
        this.table = table;
    }

    @Override
    public IObjRef getReference() {
        return reference;
    }

    @Override
    public IChangeCommand addCommand(IChangeCommand other) {
        IChangeCommand toExecute;
        if (other instanceof ICreateCommand createCommand) {
            toExecute = addCommand(createCommand);
        } else if (other instanceof IUpdateCommand updateCommand) {
            toExecute = addCommand(updateCommand);
        } else if (other instanceof IDeleteCommand deleteCommand) {
            toExecute = addCommand(deleteCommand);
        } else {
            throw new IllegalCommandException("Unknown command object!");
        }
        return toExecute;
    }

    @Override
    public String toString() {
        return this.getClass() + " for " + reference;
    }

    protected void repackPuis(IPrimitiveUpdateItem[] puis, ILinkedMap<IFieldMetaData, Object> target) {
        if (puis == null) {
            return;
        }
        var table = this.table.getMetaData();
        for (int i = puis.length; i-- > 0; ) {
            var pui = puis[i];
            if (pui == null) {
                continue;
            }
            var field = table.getFieldByMemberName(pui.getMemberName());
            if (field == null) {
                // field is transient
                continue;
            }
            target.put(field, pui.getNewValue());
        }
    }

    protected abstract IChangeCommand addCommand(ICreateCommand other);

    protected abstract IChangeCommand addCommand(IUpdateCommand other);

    protected abstract IChangeCommand addCommand(IDeleteCommand other);
}
