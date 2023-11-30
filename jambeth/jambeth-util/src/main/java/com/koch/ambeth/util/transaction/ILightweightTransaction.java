package com.koch.ambeth.util.transaction;

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

import com.koch.ambeth.util.function.CheckedRunnable;
import com.koch.ambeth.util.function.CheckedSupplier;

public interface ILightweightTransaction {
    boolean isActive();

    void runInTransaction(CheckedRunnable runnable);

    <R> R runInTransaction(CheckedSupplier<R> runnable);

    void runInLazyTransaction(CheckedRunnable runnable);

    <R> R runInLazyTransaction(CheckedSupplier<R> runnable);

    void runOnTransactionPreCommit(CheckedRunnable runnable);

    void runOnTransactionPostCommit(CheckedRunnable runnable);
}
