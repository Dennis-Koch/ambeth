package de.osthus.ambeth.state;

/**
 * Allows to rollback a given action. In many cases the action made is a modification of a static or instance field which can then be reverted/rolled back.<br/>
 * For instances of this interface please inherit always from <code>AbstractStateRollback</code>.<br/>
 * <br/>
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
 *        protected void rollbackIntern() throws Throwable
 *        {
 *          MyClass.this.value = oldValue;
 *        }
 *      }
 *   }
 * }
 * </pre>
 */
public interface IStateRollback
{
	void rollback();
}
