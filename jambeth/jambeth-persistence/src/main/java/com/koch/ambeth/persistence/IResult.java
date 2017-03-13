package com.koch.ambeth.persistence;

import java.util.Enumeration;

import com.koch.ambeth.persistence.api.ICursor;
import com.koch.ambeth.persistence.api.ICursorItem;
import com.koch.ambeth.util.IDisposable;

public interface IResult extends Enumeration<ICursorItem>, IDisposable
{

	ICursor getEnumerator();

}
