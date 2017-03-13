package com.koch.ambeth.training.travelguides.guides;

import org.junit.Assert;
import org.junit.Test;

import com.koch.ambeth.ioc.annotation.Autowired;
import com.koch.ambeth.testutil.AbstractIocTest;
import com.koch.ambeth.testutil.TestModule;
import com.koch.ambeth.training.travelguides.ioc.HelloWorldModule;
import com.koch.ambeth.training.travelguides.ioc.LogModule;

@TestModule({ LogModule.class, HelloWorldModule.class })
public class DalianTest extends AbstractIocTest
{

	private static final String TESTPLACE = "TESTPLACE";

	ThreadLocal<String> nameTL;

	IGuideBook guideBook;
	@Autowired
	IGuideBookExtendable guideBookExtendable;

	void handleRequest()
	{

		String oldName = nameTL.get();
		try
		{
			nameTL.set("new Name");
			this.handleName();
		}
		finally
		{
			nameTL.set(oldName);
		}

	}

	void handleName()
	{
		String name = nameTL.get();
		if (name == null)
		{
			name = "some default";
			nameTL.set(name);
		}

		// do work with name
	}

	@Test
	public void IsCinemaInDirectionOne()
	{
		guideBook = guideBookExtendable.getBook("Dalian");
		Assert.assertEquals("check that there is not test entry", IGuideBook.NO_DIRECTION, guideBook.guideUs(TESTPLACE));
		guideBook.addPlace(TESTPLACE, 4);
		Assert.assertEquals("expect that there is a testplace in direction 4", 4, guideBook.guideUs(TESTPLACE));
		guideBook.removePlace(TESTPLACE);
	}

	@Test
	public void testTheRemoveOfTheZoo()
	{
		guideBook = guideBookExtendable.getBook("Dalian");
		Assert.assertEquals("check that there is not test entry", IGuideBook.NO_DIRECTION, guideBook.guideUs(TESTPLACE));
		guideBook.addPlace(TESTPLACE, 4);
		guideBook.removePlace(TESTPLACE);
		Assert.assertEquals("check that there is not test entry", IGuideBook.NO_DIRECTION, guideBook.guideUs(TESTPLACE));

	}

	@Test
	public void testOurExtendable()
	{
		IGuideBook book = guideBookExtendable.getBook("Dalian");
		Assert.assertEquals(1, book.guideUs("cinema"));

		IGuideBook bookBeijing = guideBookExtendable.getBook("Beijing");
		Assert.assertEquals(IGuideBook.NO_DIRECTION, bookBeijing.guideUs("cinema"));
	}

	@Test
	public void testArrays()
	{
		String a = "a";
		String b = "b";

		String[] arr1 = new String[2];
		String[] arr2 = new String[2];

		arr1[0] = a;
		arr1[1] = b;

		arr2[0] = a;
		arr2[1] = b;

		Assert.assertArrayEquals(arr1, arr2);

	}
}
