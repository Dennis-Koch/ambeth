using System;
using System.Collections.Generic;
using De.Osthus.Ambeth.Annotation;
using De.Osthus.Ambeth.Test.Model;

namespace De.Osthus.Ambeth.Xml.Test.Transfer
{
    [XmlType(Name = "ITestService")]
    public interface ITestService
    {
        void NoParamNoReturn();

        void PrimitiveParamNoReturn(int param);

        void DateParamNoReturn(DateTime param);

        void PrimitiveArrayParamNoReturn(int[] param);

        void PrimitiveListParamNoReturn(List<Int32> param);

        void EntityParamNoReturn(MaterialGroup param);

        void EntityWithRelationParamNoReturn(Material param);

        void MixedParamsNoReturn(int number, Material material1, String text, MaterialGroup materialGroup, Material material2, DateTime date);

        int NoParamPrimitiveReturn();

        DateTime NoParamDateReturn();

        int[] NoParamPrimitiveArrayReturn();

        List<Int32> NoParamPrimitiveListReturn();

        MaterialGroup NoParamEntityReturn();

        Material NoParamEntityWithRelationReturn();
    }
}
