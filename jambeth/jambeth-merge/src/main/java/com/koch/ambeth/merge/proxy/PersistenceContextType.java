package com.koch.ambeth.merge.proxy;

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
 * Used for customizing the {@link PersistenceContext} annotation
 */
public enum PersistenceContextType {
	/**
	 * States to not bother explicitly with transactions - there may already be an aquired transaction
	 * or there may be not. In any case no explicit action is followed in this mode. This is most
	 * often used for annotations on class level to allow to configure the real intended behavior on a
	 * per-method basis.
	 */
	NOT_REQUIRED,

	/**
	 * On the annotated class or method scope it is ensured that for each entered method a transaction
	 * will be ensured. If this means that this transaction has been acquired directly before method
	 * entering it will also be committed on graceful method return - or rolled back on any unhandled
	 * exception during method invocation.<br>
	 * <br>
	 * Note that processing the persistence context multiple times on the same thread stack does not
	 * lead to nested transactions: In such a case only the outermost persistence context matters.
	 */
	REQUIRED,

	/**
	 * Similar to {@link #REQUIRED} but - if applied - only acquires a read-only transaction: That is
	 * a transaction that will be rolled back in any case and is most often used for very specific
	 * usecases where the application code knows that he does not want to modify anything and this
	 * mode configures also potentially addressed JDBC drivers. There are also checks that ensure that
	 * a merge process instance will not succeed in generating data repository updates (i.e. SQL
	 * INSERT, UPDATE, DELETE) when the current transaction is in read-only mode.<br>
	 * <br>
	 * Note that processing the persistence context multiple times on the same thread stack does not
	 * lead to nested transactions: In such a case only the outermost persistence context matters. In
	 * this case this means that the outermost {@link #REQUIRED} or {@link #REQUIRED_READ_ONLY}
	 * defines the behavior of being readonly or not.
	 */
	REQUIRED_READ_ONLY,

	/**
	 * Default mode if not explicitly specified: In its runtime behavior very similar to
	 * {@link #REQUIRED} but - if applied - only marks the annotated class or method level of being
	 * transaction-aware. It is not eagerly acquiring a transaction before invoking the target method
	 * but rather any cascaded action with data which acquires a least one time a transaction will
	 * delay the commit/rollback of its transaction till the originally entered {@link #REQUIRED_LAZY}
	 * method is finished. The benefit of this specific behavior is to allow - in principle -
	 * transaction-aware processing logic but without eagerly binding a database connection and a
	 * transaction to the current thread for usecases where not every codepath in the processing logic
	 * really needs it. In many cases a given processing logic is based on conditions or the global
	 * state to decide whether it just does in-memory work and early-return from its scope. In such a
	 * case you perceive a major performance gain by not having to deal with latency in regard to data
	 * source, connection and transactions lifecycle.<br>
	 * <br>
	 * Note that processing the persistence context multiple times on the same thread stack does not
	 * lead to nested transactions: In such a case only the outermost persistence context matters. In
	 * this case this means that the outermost {@link #REQUIRED} or {@link #REQUIRED_READ_ONLY}
	 * defines the behavior of being readonly or not and having already an outer-managed transaction
	 * or not.
	 */
	REQUIRED_LAZY,

	/**
	 * Does not deal directly with transaction lifecycles: It just ensures that a transaction is
	 * already acquired by any outer {@link #REQUIRED} or {@link #REQUIRED_READ_ONLY} behavior before
	 * invoking the target method. If no transaction is bound it throws a
	 * {@link UnsupportedOperationException} instead.
	 */
	EXPECTED,

	/**
	 * The inverse behavior of {@link #EXPECTED} and does also not deal directly with transaction
	 * lifecycles: It just ensures that a transaction is not already acquired by any outer
	 * {@link #REQUIRED} or {@link #REQUIRED_READ_ONLY} behavior before invoking the target method. If
	 * a transaction is bound it throws a {@link UnsupportedOperationException} instead.
	 */
	FORBIDDEN;
}
