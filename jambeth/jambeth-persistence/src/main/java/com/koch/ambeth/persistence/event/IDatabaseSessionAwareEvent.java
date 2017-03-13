package com.koch.ambeth.persistence.event;

public interface IDatabaseSessionAwareEvent
{
	long getSessionId();
}
