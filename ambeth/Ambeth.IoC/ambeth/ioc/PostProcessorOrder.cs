using De.Osthus.Ambeth.Util;
using System;

namespace De.Osthus.Ambeth.Ioc
{
    public class PostProcessorOrder : IImmutableType
    {
        public static readonly PostProcessorOrder DEFAULT = new PostProcessorOrder("DEFAULT", 3);

        public static readonly PostProcessorOrder HIGHEST = new PostProcessorOrder("HIGHEST", 0);

        public static readonly PostProcessorOrder HIGHER = new PostProcessorOrder("HIGHER", 1);

        public static readonly PostProcessorOrder HIGH = new PostProcessorOrder("HIGH", 2);

        public static readonly PostProcessorOrder NORMAL = new PostProcessorOrder("NORMAL", 3);

        public static readonly PostProcessorOrder LOW = new PostProcessorOrder("LOW", 4);

        public static readonly PostProcessorOrder LOWER = new PostProcessorOrder("LOWER", 5);

        public static readonly PostProcessorOrder LOWEST = new PostProcessorOrder("LOWEST", 6);

        public static PostProcessorOrder[] Values
        {
            get
            {
                return new PostProcessorOrder[] { DEFAULT, HIGHEST, HIGH, NORMAL, LOW, LOWEST };
            }
        }

        private readonly int position;

        private readonly String name;

        private PostProcessorOrder(String name, int position)
        {
            this.name = name;
            this.position = position;
        }

        public int Position
        {
            get
            {
                return position;
            }
        }

        public override string ToString()
        {
            return name;
        }
    }
}