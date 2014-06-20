namespace De.Osthus.Ambeth.Merge.Independent
{
    public abstract class BaseEntity
    {
        public int Id { get; set; }

        public int Version { get; set; }
    }
}