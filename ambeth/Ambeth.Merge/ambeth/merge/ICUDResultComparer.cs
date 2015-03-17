using De.Osthus.Ambeth.Merge.Model;

namespace De.Osthus.Ambeth.Merge
{
    public interface ICUDResultComparer
    {
        bool EqualsCUDResult(ICUDResult left, ICUDResult right);

        ICUDResult DiffCUDResult(ICUDResult left, ICUDResult right);
    }
}