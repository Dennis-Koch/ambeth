package com.koch.ambeth.merge.copy;

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

/**
 * Allows to extend the IObjectCopier with custom copy logic if needed.
 * <p>
 * Note that inheritance and polymorphism functionality will be supported out-of-the-box. Therefore
 * extensions registering themselves to type 'Collection' will be used when copying a List because
 * List implements Collection. On the other hand the registered extension will not be used when
 * registered to type 'java.util.List' and the object to copy is a java.util.HashSet.
 * <p>
 * Note also that - like all extendable interfaces - the explicit call to these register/unregister
 * methods can be done manually, but should in most scenarios be dedicated to the IOC container
 * where the Extension Point / Extension relation will be connected to the corresponding lifecycle
 * of both components.
 */
public interface IObjectCopierExtendable {
    /**
     * Registers the given extension to copy objects which are assignable to the given type
     *
     * @param objectCopierExtension The extension which implements custom copy logic
     * @param type                  The type for which the custom copy logic should be applied
     */
    void registerObjectCopierExtension(IObjectCopierExtension objectCopierExtension, Class<?> type);

    /**
     * Unregisters the given extension which copied objects which are assignable to the given type
     *
     * @param objectCopierExtension The extension which implements custom copy logic
     * @param type                  The type for which the custom copy logic should not be applied any more
     */
    void unregisterObjectCopierExtension(IObjectCopierExtension objectCopierExtension, Class<?> type);
}
