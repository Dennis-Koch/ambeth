using De.Osthus.Ambeth.Collections;
using System;

namespace De.Osthus.Ambeth.Util
{
    public class ModuleUtil
    {
        private ModuleUtil()
        {
        }

        public static Type[] MergeModules(Type[] leftModules, params Type[] rightModules)
        {
            if (leftModules == null)
            {
                return rightModules;
            }
            else if (rightModules == null)
            {
                return leftModules;
            }
            LinkedHashSet<Type> modules = new LinkedHashSet<Type>(leftModules.Length + rightModules.Length);
            for (int a = 0, size = leftModules.Length; a < size; a++)
            {
                modules.Add(leftModules[a]);
            }
            for (int a = 0, size = rightModules.Length; a < size; a++)
            {
                modules.Add(rightModules[a]);
            }
            return modules.ToArray();
        }
    }
}