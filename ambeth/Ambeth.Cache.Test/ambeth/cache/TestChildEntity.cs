using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using De.Osthus.Ambeth.Persistence.Transfer;

namespace De.Osthus.Ambeth.Cache.Test
{
    public class TestChildEntity : IEntity
    {
        public uint RecId { get; set; }

        public int Version { get; set; }

        public String CreatedBy { get; set; }

        public DateTime CreatedOn { get; set; }

        public String UpdatedBy { get; set; }

        public DateTime UpdatedOn { get; set; }

        public String Property { get; set; }

        protected String fieldIntern;

        public String fieldExtern;

        public TestParentEntity Parent { get; set; }

        public String GetFieldIntern()
        {
            return fieldIntern;
        }

        public void SetFieldIntern(String fieldIntern)
        {
            this.fieldIntern = fieldIntern;
        }

    }
}
