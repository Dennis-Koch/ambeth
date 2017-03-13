package com.koch.ambeth.merge.util;

import javax.persistence.OptimisticLockException;

import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.merge.model.IObjRef;

public final class OptimisticLockUtil
{
	public static OptimisticLockException throwDeleted(IObjRef objRef)
	{
		throw new OptimisticLockException("Object outdated: " + objRef + " has been deleted concurrently", null, objRef);
	}

	public static OptimisticLockException throwDeleted(IObjRef objRef, Object obj)
	{
		throw new OptimisticLockException("Object outdated: " + objRef + " has been deleted concurrently", null, obj);
	}

	public static OptimisticLockException throwModified(IObjRef objRef, Object givenVersion)
	{
		return throwModified(objRef, givenVersion, null);
	}

	public static OptimisticLockException throwModified(IObjRef objRef, Object givenVersion, Object obj)
	{
		String givenVersionString = "";
		if (givenVersion != null)
		{
			givenVersionString = " - given version: " + givenVersion;
		}
		if (obj != null)
		{
			throw new OptimisticLockException("Object outdated: " + objRef + " has been modified concurrently" + givenVersionString, null, obj);
		}
		throw new OptimisticLockException("Object outdated: " + objRef + " has been modified concurrently" + givenVersionString, null, new ObjRef(
				objRef.getRealType(), objRef.getIdNameIndex(), objRef.getId(), givenVersion));
	}

	private OptimisticLockUtil()
	{
		// Intended blank
	}
}
