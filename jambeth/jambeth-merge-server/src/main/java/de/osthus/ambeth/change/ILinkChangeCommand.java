package de.osthus.ambeth.change;

import java.util.List;

import de.osthus.ambeth.merge.model.IObjRef;
import de.osthus.ambeth.persistence.IDirectedLink;

public interface ILinkChangeCommand extends IChangeCommand
{
	IDirectedLink getDirectedLink();

	List<IObjRef> getRefsToLink();

	List<IObjRef> getRefsToUnlink();

	boolean isReadyToExecute();
}
