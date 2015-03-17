package de.osthus.ambeth.merge.model;

public interface ICreateOrUpdateContainer extends IChangeContainer
{
	IPrimitiveUpdateItem[] getFullPUIs();

	IRelationUpdateItem[] getFullRUIs();
}
