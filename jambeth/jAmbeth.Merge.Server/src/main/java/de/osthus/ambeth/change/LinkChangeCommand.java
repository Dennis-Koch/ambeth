package de.osthus.ambeth.change;

import java.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.persistence.IDirectedLink;

public class LinkChangeCommand extends AbstractChangeCommand implements ILinkChangeCommand
{
	protected IDirectedLink link;

	protected List<IObjRef> refsToLink = new ArrayList<IObjRef>();

	protected List<IObjRef> refsToUnlink = new ArrayList<IObjRef>();

	@Override
	public IDirectedLink getDirectedLink()
	{
		return this.link;
	}

	@Override
	public void setLink(IDirectedLink link)
	{
		this.link = link;
	}

	@Override
	public List<IObjRef> getRefsToLink()
	{
		return this.refsToLink;
	}

	@Override
	public List<IObjRef> getRefsToUnlink()
	{
		return this.refsToUnlink;
	}

	@Override
	public IChangeCommand addCommand(IChangeCommand other)
	{
		if (!(other instanceof ILinkChangeCommand))
		{
			throw new IllegalCommandException("Cannot add create/update/change command to a link command!");
		}
		ILinkChangeCommand linkChangeCommand = (ILinkChangeCommand) other;
		this.refsToLink.addAll(linkChangeCommand.getRefsToLink());
		this.refsToUnlink.addAll(linkChangeCommand.getRefsToUnlink());
		return null;
	}

	@Override
	protected IChangeCommand addCommand(ICreateCommand other)
	{
		throw new IllegalCommandException("Cannot add create command to a link command!");
	}

	@Override
	protected IChangeCommand addCommand(IUpdateCommand other)
	{
		throw new IllegalCommandException("Cannot add update command to a link command!");
	}

	@Override
	protected IChangeCommand addCommand(IDeleteCommand other)
	{
		throw new IllegalCommandException("Cannot add delete command to a link command!");
	}

	@Override
	public boolean isReadyToExecute()
	{
		for (int i = this.refsToLink.size(); i-- > 0;)
		{
			if (this.refsToLink.get(i).getId() == null)
			{
				return false;
			}
		}
		for (int i = this.refsToUnlink.size(); i-- > 0;)
		{
			if (this.refsToUnlink.get(i).getId() == null)
			{
				return false;
			}
		}
		return true;
	}
}
