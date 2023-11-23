package com.koch.ambeth.merge.util;

/*-
 * #%L
 * jambeth-merge
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.service.merge.model.IObjRef;
import jakarta.persistence.OptimisticLockException;

public final class OptimisticLockUtil {
    public static OptimisticLockException throwDeleted(IObjRef objRef) {
        throw new OptimisticLockException("Object outdated: " + objRef + " has been deleted concurrently", null, objRef);
    }

    public static OptimisticLockException throwDeleted(IObjRef objRef, Object obj) {
        throw new OptimisticLockException("Object outdated: " + objRef + " has been deleted concurrently", null, obj);
    }

    public static OptimisticLockException throwModified(IObjRef objRef, Object givenVersion) {
        return throwModified(objRef, givenVersion, null);
    }

    public static OptimisticLockException throwModified(IObjRef objRef, Object givenVersion, Object obj) {
        String givenVersionString = "";
        if (givenVersion != null) {
            givenVersionString = " - given version: " + givenVersion;
        }
        if (obj != null) {
            throw new OptimisticLockException("Object outdated: " + objRef + " has been modified concurrently" + givenVersionString, null, obj);
        }
        throw new OptimisticLockException("Object outdated: " + objRef + " has been modified concurrently" + givenVersionString, null,
                new ObjRef(objRef.getRealType(), objRef.getIdNameIndex(), objRef.getId(), givenVersion));
    }

    private OptimisticLockUtil() {
        // Intended blank
    }
}
