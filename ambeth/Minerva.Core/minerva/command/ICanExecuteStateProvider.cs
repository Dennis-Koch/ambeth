namespace De.Osthus.Minerva.Command
{
    public interface ICanExecuteStateProvider
    {
        bool CanExecute();

        void Execute();
    }
}
