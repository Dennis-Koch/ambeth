package de.osthus.ambeth.change;

import java.util.List;
import java.util.Map.Entry;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.config.Property;
import de.osthus.ambeth.config.UtilConfigurationConstants;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.persistence.IDirectedLink;
import de.osthus.ambeth.persistence.ILink;
import de.osthus.ambeth.service.IChangeAggregator;

/**
 * Change collector for link tables
 */
public class LinkTableChange extends AbstractTableChange
{
	protected final HashMap<IObjRef, ILinkChangeCommand> rowCommands = new HashMap<IObjRef, ILinkChangeCommand>();

	@Property(name = UtilConfigurationConstants.DebugMode, defaultValue = "false")
	protected boolean debugMode;

	@Override
	public void addChangeCommand(IChangeCommand command)
	{
		if (command instanceof ILinkChangeCommand)
		{
			addChangeCommand((ILinkChangeCommand) command);
		}
		else
		{
			throw new IllegalCommandException("Cannot add create/update/delete to a LinkTableChange!");
		}
	}

	@Override
	public void addChangeCommand(ILinkChangeCommand command)
	{
		HashMap<IObjRef, ILinkChangeCommand> rowCommands = this.rowCommands;
		IObjRef reference = command.getReference();
		ILinkChangeCommand existingCommand = rowCommands.get(reference);
		if (existingCommand != null)
		{
			if (existingCommand.getDirectedLink() != command.getDirectedLink())
			{
				IDirectedLink uniformDirectedLink = existingCommand.getDirectedLink();
				// uniform link direction
				List<IObjRef> refsToLink = command.getRefsToLink();
				for (int a = refsToLink.size(); a-- > 0;)
				{
					LinkChangeCommand lcc = new LinkChangeCommand(refsToLink.get(a), uniformDirectedLink);
					lcc.addRefToLink(reference);
					addChangeCommand(lcc);
				}
				List<IObjRef> refsToUnLink = command.getRefsToUnlink();
				for (int a = refsToUnLink.size(); a-- > 0;)
				{
					LinkChangeCommand lcc = new LinkChangeCommand(refsToUnLink.get(a), uniformDirectedLink);
					lcc.addRefToUnlink(reference);
					addChangeCommand(lcc);
				}
				return;
			}
			existingCommand.addCommand(command);
		}
		else
		{
			rowCommands.put(reference, command);
		}
	}

	@Override
	public void execute(IChangeAggregator changeAggreagator)
	{
		ILink link = null;
		try
		{
			ArrayList<Object> ids = new ArrayList<Object>();
			for (Entry<IObjRef, ILinkChangeCommand> entry : rowCommands)
			{
				ILinkChangeCommand command = entry.getValue();
				if (debugMode && !command.isReadyToExecute())
				{
					throw new IllegalCommandException("LinkChangeCommand is not ready to be executed!");
				}
				IDirectedLink directedLink = command.getDirectedLink();
				if (link == null)
				{
					link = directedLink.getLink();
					link.startBatch();
				}
				Object id = command.getReference().getId();
				{
					List<IObjRef> refs = command.getRefsToLink();
					if (!refs.isEmpty())
					{
						for (int j = refs.size(); j-- > 0;)
						{
							ids.add(refs.get(j).getId());
						}
						link.linkIds(directedLink, id, ids);
						ids.clear();
					}
				}
				{
					List<IObjRef> refs = command.getRefsToUnlink();
					if (!refs.isEmpty())
					{
						for (int j = refs.size(); j-- > 0;)
						{
							ids.add(refs.get(j).getId());
						}
						link.unlinkIds(directedLink, id, ids);
						ids.clear();
					}
				}
			}
			if (link != null)
			{
				link.finishBatch();
			}
		}
		finally
		{
			if (link != null)
			{
				link.clearBatch();
			}
		}
	}

	public IMap<IObjRef, ILinkChangeCommand> getRowCommands()
	{
		return rowCommands;
	}
}
