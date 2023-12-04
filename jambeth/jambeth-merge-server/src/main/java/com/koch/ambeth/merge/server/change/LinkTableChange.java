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

import com.koch.ambeth.ioc.config.Property;
import com.koch.ambeth.merge.server.service.IChangeAggregator;
import com.koch.ambeth.persistence.api.ILink;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.HashMap;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.config.UtilConfigurationConstants;

/**
 * Change collector for link tables
 */
public class LinkTableChange extends AbstractTableChange {
    protected final HashMap<IObjRef, ILinkChangeCommand> rowCommands = new HashMap<>();

    @Property(name = UtilConfigurationConstants.DebugMode, defaultValue = "false")
    protected boolean debugMode;

    @Override
    public void addChangeCommand(IChangeCommand command) {
        if (command instanceof ILinkChangeCommand) {
            addChangeCommand((ILinkChangeCommand) command);
        } else {
            throw new IllegalCommandException("Cannot add create/update/delete to a LinkTableChange!");
        }
    }

    @Override
    public void addChangeCommand(ILinkChangeCommand command) {
        var rowCommands = this.rowCommands;
        var reference = command.getReference();
        var existingCommand = rowCommands.get(reference);
        if (existingCommand != null) {
            if (existingCommand.getDirectedLink() != command.getDirectedLink()) {
                var uniformDirectedLink = existingCommand.getDirectedLink();
                // uniform link direction
                var refsToLink = command.getRefsToLink();
                for (int a = refsToLink.size(); a-- > 0; ) {
                    var lcc = new LinkChangeCommand(refsToLink.get(a), uniformDirectedLink);
                    lcc.addRefToLink(reference);
                    addChangeCommand(lcc);
                }
                var refsToUnLink = command.getRefsToUnlink();
                for (int a = refsToUnLink.size(); a-- > 0; ) {
                    var lcc = new LinkChangeCommand(refsToUnLink.get(a), uniformDirectedLink);
                    lcc.addRefToUnlink(reference);
                    addChangeCommand(lcc);
                }
                return;
            }
            existingCommand.addCommand(command);
        } else {
            rowCommands.put(reference, command);
        }
    }

    @Override
    public void execute(IChangeAggregator changeAggreagator) {
        ILink link = null;
        try {
            var ids = new ArrayList<>();
            for (var entry : rowCommands) {
                var command = entry.getValue();
                if (debugMode && !command.isReadyToExecute()) {
                    throw new IllegalCommandException("LinkChangeCommand is not ready to be executed!");
                }
                var directedLink = command.getDirectedLink();
                if (link == null) {
                    link = directedLink.getLink();
                    link.startBatch();
                }
                var id = command.getReference().getId();
                {
                    var refs = command.getRefsToLink();
                    if (!refs.isEmpty()) {
                        for (int j = refs.size(); j-- > 0; ) {
                            ids.add(refs.get(j).getId());
                        }
                        link.linkIds(directedLink, id, ids);
                        ids.clear();
                    }
                }
                {
                    var refs = command.getRefsToUnlink();
                    if (!refs.isEmpty()) {
                        for (int j = refs.size(); j-- > 0; ) {
                            ids.add(refs.get(j).getId());
                        }
                        link.unlinkIds(directedLink, id, ids);
                        ids.clear();
                    }
                }
            }
            if (link != null) {
                link.finishBatch();
            }
        } finally {
            if (link != null) {
                link.clearBatch();
            }
        }
    }

    public IMap<IObjRef, ILinkChangeCommand> getRowCommands() {
        return rowCommands;
    }
}
