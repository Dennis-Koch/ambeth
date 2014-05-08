package de.osthus.ambeth.change;

import java.util.List;
import java.util.Map.Entry;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.HashMap;
import de.osthus.ambeth.collections.IMap;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.persistence.IDirectedLink;
import de.osthus.ambeth.persistence.ILink;
import de.osthus.ambeth.service.IChangeAggregator;

/**
 * Change collector for link tables
 */
public class LinkTableChange extends AbstractTableChange
{
	protected final IMap<IObjRef, ILinkChangeCommand> rowCommands = new HashMap<IObjRef, ILinkChangeCommand>();

	@Override
	public void dispose()
	{
		rowCommands.clear();

		super.dispose();
	}

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
		if (!rowCommands.containsKey(command.getReference()))
		{
			rowCommands.put(command.getReference(), command);
		}
		else
		{
			rowCommands.get(command.getReference()).addCommand(command);
		}
	}

	@Override
	public void execute(IChangeAggregator changeAggreagator)
	{
		ArrayList<Object> ids = new ArrayList<Object>();
		for (Entry<IObjRef, ILinkChangeCommand> entry : rowCommands)
		{
			ILinkChangeCommand command = entry.getValue();
			if (!command.isReadyToExecute())
			{
				throw new IllegalCommandException("LinkChangeCommand is not ready to be executed!");
			}
			IDirectedLink directedLink = command.getDirectedLink();
			ILink link = directedLink.getLink();
			link.startBatch();
			try
			{
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
				link.finishBatch();
			}
			finally
			{
				link.clearBatch();
			}
		}
	}
}
