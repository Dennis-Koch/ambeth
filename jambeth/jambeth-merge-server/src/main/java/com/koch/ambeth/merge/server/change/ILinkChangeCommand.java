package com.koch.ambeth.merge.server.change;

import java.util.List;

import com.koch.ambeth.persistence.api.IDirectedLink;
import com.koch.ambeth.service.merge.model.IObjRef;

public interface ILinkChangeCommand extends IChangeCommand
{
	IDirectedLink getDirectedLink();

	List<IObjRef> getRefsToLink();

	List<IObjRef> getRefsToUnlink();

	boolean isReadyToExecute();
}
