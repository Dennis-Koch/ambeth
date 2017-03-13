package com.koch.ambeth.merge.model;

public interface ICreateOrUpdateContainer extends IChangeContainer
{
	IPrimitiveUpdateItem[] getFullPUIs();

	IRelationUpdateItem[] getFullRUIs();
}
