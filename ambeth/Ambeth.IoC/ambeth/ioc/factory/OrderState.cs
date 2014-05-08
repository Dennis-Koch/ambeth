using System.Collections.Generic;

namespace De.Osthus.Ambeth.Ioc.Factory
{
    public class OrderState : List<BeanConfigState>
    {
        private int processedIndex = -1;

        public BeanConfigState ConsumeBeanConfigState()
        {
            if (processedIndex + 1 < Count)
            {
                return this[++processedIndex];
            }
            return null;
        }
    }
}