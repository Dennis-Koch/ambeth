
namespace De.Osthus.Minerva.Core
{
    public interface IModelContainer
    {
        // Marker interface for either IModelSingleContainer or IModelMultiContainer
        int Count { get; }
    }
}
