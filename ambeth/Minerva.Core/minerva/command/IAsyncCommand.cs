using System;

namespace De.Osthus.Minerva.Command
{
    public interface IAsyncCommand
    {
        void Execute(Object parameter, INextCommandDelegate commandFinishedCallback);
    }
}
