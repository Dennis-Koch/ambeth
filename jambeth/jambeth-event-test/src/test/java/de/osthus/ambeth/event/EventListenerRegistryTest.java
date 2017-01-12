package de.osthus.ambeth.event;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class EventListenerRegistryTest
{
	private static final IEventListener DUMMY_LISTENER = new IEventListener()
	{

		@Override
		public void handleEvent(Object eventObject, long dispatchTime, long sequenceId) throws Exception
		{
		}
	};

	private EventListenerRegistry eventListenerRegistry;

	@Before
	public void setUp() throws Exception
	{
		eventListenerRegistry = new EventListenerRegistry();
	}

	@Test
	public void testHasListeners_noListeners()
	{
		Assert.assertFalse(eventListenerRegistry.hasListeners(Object.class));
		Assert.assertFalse(eventListenerRegistry.hasListeners(String.class));
		Assert.assertFalse(eventListenerRegistry.hasListeners(EventListenerRegistryTest.class));
	}

	@Test
	public void testHasListeners_someListeners()
	{
		eventListenerRegistry.registerEventListener(DUMMY_LISTENER, String.class);
		eventListenerRegistry.registerEventListener(DUMMY_LISTENER, EventListenerRegistryTest.class);

		Assert.assertTrue(eventListenerRegistry.hasListeners(String.class));
		Assert.assertTrue(eventListenerRegistry.hasListeners(EventListenerRegistryTest.class));

		Assert.assertFalse(eventListenerRegistry.hasListeners(Number.class));
		Assert.assertFalse(eventListenerRegistry.hasListeners(Object.class));
	}

	@Test
	public void testHasListeners_globalListener()
	{
		eventListenerRegistry.registerEventListener(DUMMY_LISTENER);

		Assert.assertTrue(eventListenerRegistry.hasListeners(Object.class));
		Assert.assertTrue(eventListenerRegistry.hasListeners(String.class));
		Assert.assertTrue(eventListenerRegistry.hasListeners(EventListenerRegistryTest.class));

	}
}
