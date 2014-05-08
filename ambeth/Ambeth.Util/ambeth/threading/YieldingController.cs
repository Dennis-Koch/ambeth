using De.Osthus.Ambeth.Util;

namespace De.Osthus.Ambeth.Threading
{
    public class YieldingController : IYieldingController
    {
        protected long yieldingInterval;

        protected long nextYieldingTime;

        public YieldingController(long yieldingInterval)
        {
            this.yieldingInterval = yieldingInterval;
        }

        public void CalculateNextInterval()
        {
            if (yieldingInterval == 0)
            {
                return;
            }
            long currentTimeMillis = DateTimeUtil.CurrentTimeMillis();
            nextYieldingTime = currentTimeMillis + yieldingInterval;
        }

        public bool IsShouldYield
        {
            get
            {
                return nextYieldingTime != 0 && DateTimeUtil.CurrentTimeMillis() >= nextYieldingTime;
            }
        }
    }
}
