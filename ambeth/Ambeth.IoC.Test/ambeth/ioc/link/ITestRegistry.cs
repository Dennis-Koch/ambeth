using System.ComponentModel;
namespace De.Osthus.Ambeth.Ioc.Link
{
    public interface ITestRegistry
    {
        ITestListener[] GetTestListeners();

        PropertyChangedEventHandler[] GetPceListeners();
    }
}