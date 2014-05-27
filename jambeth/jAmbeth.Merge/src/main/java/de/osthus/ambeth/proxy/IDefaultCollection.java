package de.osthus.ambeth.proxy;

/**
 * Marker interface for collections which do not force an eager load of relations by themselves
 * 
 */
public interface IDefaultCollection
{
	boolean hasDefaultState();
}
