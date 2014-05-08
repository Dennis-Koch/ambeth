namespace De.Osthus.Ambeth.Ioc.Extendable
{
    public interface ITestListenerExtendable
    {
        void AddTestListener(ITestListener testListener);

        void RemoveTestListener(ITestListener testListener);
    }
}