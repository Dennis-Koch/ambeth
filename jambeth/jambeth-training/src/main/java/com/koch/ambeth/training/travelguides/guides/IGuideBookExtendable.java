package com.koch.ambeth.training.travelguides.guides;

import com.koch.ambeth.training.travelguides.annotation.LogCalls;

@LogCalls
public interface IGuideBookExtendable
{
	void register(IGuideBook book, String name);

	void unregister(IGuideBook book, String name);

	IGuideBook getBook(String string);

}
