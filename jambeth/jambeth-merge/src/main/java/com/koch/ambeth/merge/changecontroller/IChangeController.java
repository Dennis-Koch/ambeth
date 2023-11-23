package com.koch.ambeth.merge.changecontroller;

import com.koch.ambeth.util.state.IStateRollback;

/**
 * Allows to temporarily deactivate the EDBL pipeline. This is mostly helpful in unit test setups,
 * where you do not want the EDBL to trigger when you create the initial test dataset before the
 * real unit tests start
 */
public interface IChangeController {

    /**
     * Allows to temporarily deactivate the EDBL pipeline. This is mostly helpful in unit test setups,
     * where you do not want the EDBL to trigger when you create the initial test dataset before the
     * real unit tests start.<br>
     * <br>
     * Usage:<br>
     * <code>
     * IStateRollback rollback = changeController.pushRunWithoutEDBL();<br>
     * try {<br>
     * ... do stuff<br>
     * }<br>
     * finally {<br>
     * rollback.rollback();<br>
     * }<br>
     * </code>
     *
     * @param rollbacks Previously created rollbacks for the current stack to be unified with the
     *                  to-be-created rollback handle.
     * @return A rollback handle to revert all changes made by this push operation and calls all given
     * additional rollbacks.
     */
    IStateRollback pushRunWithoutEDBL();
}
