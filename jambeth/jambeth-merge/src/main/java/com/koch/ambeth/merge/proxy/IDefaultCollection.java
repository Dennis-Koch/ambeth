package com.koch.ambeth.merge.proxy;

/**
 * Marker interface for collections which do not force an eager load of relations by themselves
 * 
 */
public interface IDefaultCollection
{
	boolean hasDefaultState();
}
