package com.koch.ambeth.persistence.api;

import com.koch.ambeth.query.persistence.IVersionItem;

public interface ICursorItem extends IVersionItem
{

	Object[] getValues();

}
