package de.osthus.ambeth.generic;

import java.util.List;

public abstract class C4 extends C1<String>
{
	public void halloC4()
	{
		halloC1("test");
		Number number = halloC0((List<String>) null, Integer.valueOf(1));
		number.intValue();
	}
}
