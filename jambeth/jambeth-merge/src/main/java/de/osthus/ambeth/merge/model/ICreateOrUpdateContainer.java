package de.osthus.ambeth.merge.model;

public interface ICreateOrUpdateContainer
{
	IPrimitiveUpdateItem[] getFullPUIs();

	IRelationUpdateItem[] getFullRUIs();
}
