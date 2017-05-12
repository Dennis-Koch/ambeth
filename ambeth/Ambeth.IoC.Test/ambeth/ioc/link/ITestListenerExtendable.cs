namespace De.Osthus.Ambeth.Ioc.Link
{
    public interface ITestListenerExtendable
    {
        void RegisterTestListener(ITestListener testListener);

        void UnregisterTestListener(ITestListener testListener);
    }
}