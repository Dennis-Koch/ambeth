using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;

namespace De.Osthus.Ambeth.Testutil
{
    public abstract class BlockJUnit4ClassRunner
    {
        [ThreadStatic]
        private static bool? alreadyCalled;
        
        protected abstract Object CreateTest();

        protected virtual Statement MethodBlock(MethodInfo method)
        {
            Object test = CreateTest();

            Statement statement = MethodInvoker(method, test);
            statement = PossiblyExpectingExceptions(method, test, statement);
            statement = WithPotentialTimeout(method, test, statement);
            statement = WithBefores(method, test, statement);
            statement = WithAfters(method, test, statement);
            statement = WithRules(method, test, statement);
            return statement;
        }

        protected virtual Statement MethodInvoker(MethodInfo method, Object test)
        {
            return new Statement(delegate()
                {
                    //method.Invoke(test, null);
                });
        }

        protected virtual Statement PossiblyExpectingExceptions(MethodInfo method, Object test, Statement statement)
        {
            return statement;
        }

        protected virtual void RunChild(MethodInfo method, Object notifier)
        {
            if (alreadyCalled.HasValue && alreadyCalled.Value)
            {
                return;
            }
            alreadyCalled = true;
            try
            {
                MethodBlock(method)();
            }
            finally
            {
                alreadyCalled = null;
            }
        }

        protected virtual Statement WithAfters(MethodInfo method, Object test, Statement statement)
        {
            return statement;
        }

        protected virtual Statement WithAfterClasses(Statement statement)
        {
            return statement;
        }

        protected virtual Statement WithBefores(MethodInfo method, Object test, Statement statement)
        {
            return statement;
        }

        protected virtual Statement WithPotentialTimeout(MethodInfo method, Object test, Statement statement)
        {
            return statement;
        }

        protected virtual Statement WithRules(MethodInfo method, Object test, Statement statement)
        {
            return statement;
        }
    }
}
