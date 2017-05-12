using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Model;
using De.Osthus.Ambeth.Progress.Model;

namespace De.Osthus.Ambeth.Service
{
    [XmlType]
    public interface IProgressService
    {
        IProgressHandle CallProgressableServiceAsync(IServiceDescription serviceDescription);

        IResultProgress CallProgressableService(IServiceDescription serviceDescription);

        IProgress Status(IProgressHandle progressHandle);
    }
}
