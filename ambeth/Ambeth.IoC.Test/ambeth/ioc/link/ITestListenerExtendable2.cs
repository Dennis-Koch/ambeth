namespace De.Osthus.Ambeth.Ioc.Link
{
    public interface ITestListenerExtendable2
    {
        void RegisterTestListener(ITestListener testListener);

        void UnregisterTestListener(ITestListener testListener);
    }
}