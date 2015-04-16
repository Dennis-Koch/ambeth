package de.osthus.ambeth.training.travelguides.guides;

import de.osthus.ambeth.training.travelguides.annotation.LogCalls;

@LogCalls
public interface IGuideBookExtendable
{
	void register(IGuideBook book, String name);

	void unregister(IGuideBook book, String name);

	IGuideBook getBook(String string);

}
