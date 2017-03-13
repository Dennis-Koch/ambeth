package com.koch.ambeth.merge.server.change;

import java.util.List;

import com.koch.ambeth.merge.model.IDirectObjRef;
import com.koch.ambeth.persistence.api.IDirectedLink;
import com.koch.ambeth.service.merge.model.IObjRef;
import com.koch.ambeth.util.collections.ArrayList;
import com.koch.ambeth.util.collections.EmptyList;
import com.koch.ambeth.util.collections.IList;

public class LinkChangeCommand extends AbstractChangeCommand implements ILinkChangeCommand
{
	protected final IDirectedLink link;

	protected IList<IObjRef> refsToLink = EmptyList.<IObjRef> getInstance();

	protected IList<IObjRef> refsToUnlink = EmptyList.<IObjRef> getInstance();

	protected final byte toIdIndex;

	public LinkChangeCommand(IObjRef reference, IDirectedLink link)
	{
		super(reference);
		this.link = link;
		byte fromIdIndex = getDirectedLink().getMetaData().getFromIdIndex();
		if (!(reference instanceof IDirectObjRef) && fromIdIndex != reference.getIdNameIndex())
		{
			throw new IllegalStateException();
		}
		toIdIndex = getDirectedLink().getMetaData().getToIdIndex();
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

	protected void checkCorrectIdIndex(IObjRef objRef)
	{
		// if (objRef instanceof IDirectObjRef && ((IDirectObjRef) objRef).getDirect() != null)
		// {
		// return;
		// }
		// if (objRef.getIdNameIndex() != toIdIndex)
		// {
		// System.out.println("AAAAAAAAAAAAAAAA");
		// // throw new IllegalStateException();
		// }
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
		for (IObjRef addedORI : addedORIs)
		{
			checkCorrectIdIndex(addedORI);
			refsToLink.add(addedORI);
		}
		// refsToLink.addAll(addedORIs);
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
		for (int a = 0, size = addedORIs.size(); a < size; a++)
		{
			IObjRef addedObjRef = addedORIs.get(a);
			checkCorrectIdIndex(addedObjRef);
			refsToLink.add(addedObjRef);
		}
		// refsToLink.addAll(addedORIs);
	}

	public void addRefToLink(IObjRef addedObjRef)
	{
		IList<IObjRef> refsToLink = this.refsToLink;
		if (refsToLink.size() == 0)
		{
			refsToLink = new ArrayList<IObjRef>();
			this.refsToLink = refsToLink;
		}
		checkCorrectIdIndex(addedObjRef);
		refsToLink.add(addedObjRef);
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
		for (IObjRef removedORI : removedORIs)
		{
			checkCorrectIdIndex(removedORI);
			refsToUnlink.add(removedORI);
		}
		// refsToUnlink.addAll(removedORIs);
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
		for (int a = 0, size = removedORIs.size(); a < size; a++)
		{
			IObjRef removedORI = removedORIs.get(a);
			checkCorrectIdIndex(removedORI);
			refsToUnlink.add(removedORI);
		}
		// refsToUnlink.addAll(removedORIs);
	}

	public void addRefToUnlink(IObjRef removedObjRef)
	{
		IList<IObjRef> refsToUnlink = this.refsToUnlink;
		if (refsToUnlink.size() == 0)
		{
			refsToUnlink = new ArrayList<IObjRef>();
			this.refsToUnlink = refsToUnlink;
		}
		checkCorrectIdIndex(removedObjRef);
		refsToUnlink.add(removedObjRef);
	}
}
