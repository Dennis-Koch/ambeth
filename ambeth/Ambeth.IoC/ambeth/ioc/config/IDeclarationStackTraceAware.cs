using System;
using System.Diagnostics;

namespace De.Osthus.Ambeth.Ioc.Config
{
    public interface IDeclarationStackTraceAware
    {
        StackFrame[] DeclarationStackTrace { set; }
    }
}
