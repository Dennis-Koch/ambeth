using System;

namespace De.Osthus.Minerva.Command
{
    public interface IFinallyCommand
    {
        // marker interface for commands that must be executed in a Multicommand-chain,
        // even if preceeding commands failed
    }
}
