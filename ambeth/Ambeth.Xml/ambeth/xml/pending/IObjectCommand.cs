
namespace De.Osthus.Ambeth.Xml.Pending
{
    public interface IObjectCommand
    {
        IObjectFuture ObjectFuture { get; }

        void Execute(IReader reader);
    }
}
