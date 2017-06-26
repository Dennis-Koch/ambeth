package com.koch.ambeth.util.state;

/*-
 * #%L
 * jambeth-util
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
 * Allows to rollback a given action. In many cases the action made is a modification of a static or
 * instance field which can then be reverted/rolled back.<br>
 * For instances of this interface please inherit always from
 * <code>AbstractStateRollback</code>.<br>
 * <br>
 * Example:
 *
 * <pre>
 * {@code
 * public class MyClass
 * {
 *   private int value;
 *
 *   public IStateRollback withValue(int value, IStateRollback... rollbacks)
 *   {
 *      final int oldValue = this.value;
 *      this.value = value;
 *      return new AbstractStateRollback(rollbacks)
 *      {
 *        &#064;Override
 *        protected void rollbackIntern() throws Exception
 *        {
 *          MyClass.this.value = oldValue;
 *        }
 *      }
 *   }
 * }
 * </pre>
 */
public interface IStateRollback {
	void rollback();
}
