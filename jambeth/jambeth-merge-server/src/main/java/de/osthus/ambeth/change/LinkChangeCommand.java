package de.osthus.ambeth.change;

import java.util.List;

import de.osthus.ambeth.collections.ArrayList;
import de.osthus.ambeth.collections.EmptyList;
import de.osthus.ambeth.collections.IList;
import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.persistence.IDirectedLink;

public class LinkChangeCommand extends AbstractChangeCommand implements ILinkChangeCommand
{
	protected final IDirectedLink link;

	protected IList<IObjRef> refsToLink = EmptyList.<IObjRef> getInstance();

	protected IList<IObjRef> refsToUnlink = EmptyList.<IObjRef> getInstance();

	public LinkChangeCommand(IObjRef reference, IDirectedLink link)
	{
		super(reference);
		this.link = link;
	}

	@Override
	public IDirectedLink getDirectedLink()
	{
		return link;
	}

	@Override
	public List<IObjRef> getRefsToLink()
	{
		return refsToLink;
	}

	@Override
	public List<IObjRef> getRefsToUnlink()
	{
		return refsToUnlink;
	}

	@Override
	public IChangeCommand addCommand(IChangeCommand other)
	{
		if (!(other instanceof ILinkChangeCommand))
		{
			throw new IllegalCommandException("Cannot add create/update/change command to a link command!");
		}
		ILinkChangeCommand linkChangeCommand = (ILinkChangeCommand) other;
		addRefsToLink(linkChangeCommand.getRefsToLink());
		addRefsToUnlink(linkChangeCommand.getRefsToUnlink());
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
		IList<IObjRef> refs = refsToLink;
		for (int i = refs.size(); i-- > 0;)
		{
			if (refs.get(i).getId() == null)
			{
				return false;
			}
		}
		refs = refsToUnlink;
		for (int i = refs.size(); i-- > 0;)
		{
			if (refs.get(i).getId() == null)
			{
				return false;
			}
		}
		return true;
	}

	public void addRefsToLink(IObjRef[] addedORIs)
	{
		if (addedORIs.length == 0)
		{
			return;
		}
		IList<IObjRef> refsToLink = this.refsToLink;
		if (refsToLink.size() == 0)
		{
			refsToLink = new ArrayList<IObjRef>();
			this.refsToLink = refsToLink;
		}
		refsToLink.addAll(addedORIs);
	}

	public void addRefsToLink(List<IObjRef> addedORIs)
	{
		if (addedORIs.size() == 0)
		{
			return;
		}
		IList<IObjRef> refsToLink = this.refsToLink;
		if (refsToLink.size() == 0)
		{
			refsToLink = new ArrayList<IObjRef>();
			this.refsToLink = refsToLink;
		}
		refsToLink.addAll(addedORIs);
	}

	public void addRefsToUnlink(IObjRef[] removedORIs)
	{
		if (removedORIs.length == 0)
		{
			return;
		}
		IList<IObjRef> refsToUnlink = this.refsToUnlink;
		if (refsToUnlink.size() == 0)
		{
			refsToUnlink = new ArrayList<IObjRef>();
			this.refsToUnlink = refsToUnlink;
		}
		refsToUnlink.addAll(removedORIs);
	}

	public void addRefsToUnlink(List<IObjRef> removedORIs)
	{
		if (removedORIs.size() == 0)
		{
			return;
		}
		IList<IObjRef> refsToUnlink = this.refsToUnlink;
		if (refsToUnlink.size() == 0)
		{
			refsToUnlink = new ArrayList<IObjRef>();
			this.refsToUnlink = refsToUnlink;
		}
		refsToUnlink.addAll(removedORIs);
	}
}
