using System;
using System.Xml.Linq;

namespace De.Osthus.Ambeth.Util.Xml
{
    public class XmlConstants
    {
        public static readonly String TRUE = "true";

        public static readonly XName ENTITY = XName.Get("entity");
        public static readonly XName NAME = XName.Get("name");
        public static readonly XName CLASS = XName.Get("class");
        public static readonly XName TYPE = XName.Get("type");
        public static readonly XName EXTERN = XName.Get("external");
        public static readonly XName TABLE = XName.Get("table");
        public static readonly XName SEQ = XName.Get("sequence-generator");
        public static readonly XName ATTR = XName.Get("attributes");
        public static readonly XName ID = XName.Get("id");
        public static readonly XName VERSION = XName.Get("version");
        public static readonly XName CREATED_ON = XName.Get("created-on");
        public static readonly XName CREATED_BY = XName.Get("created-by");
        public static readonly XName UPDATED_ON = XName.Get("updated-on");
        public static readonly XName UPDATED_BY = XName.Get("updated-by");
        public static readonly XName BASIC = XName.Get("basic");
        public static readonly XName ALT_ID = XName.Get("alternate-id");
        public static readonly XName TO_ONE = XName.Get("to-one");
        public static readonly XName TO_MANY = XName.Get("to-many");
        public static readonly XName COLUMN = XName.Get("column");
        public static readonly XName TARGET_ENTITY = XName.Get("target-entity");
        public static readonly XName CONSTRAINT_NAME = XName.Get("constraint");
        public static readonly XName DO_DELETE = XName.Get("do-delete");
        public static readonly XName MAY_DELETE = XName.Get("may-delete");
        public static readonly XName JOIN_TABLE = XName.Get("join-table");
        public static readonly XName JOIN_COLUMN = XName.Get("join-column");
        public static readonly XName INV_JOIN_COLUMN = XName.Get("inverse-join-column");
        public static readonly XName INV_JOIN_ATTR = XName.Get("inverse-join-attribute");
        public static readonly XName VALUE_OBJECT = XName.Get("value-object");
        public static readonly XName RELATION = XName.Get("relation");
        public static readonly XName LIST_TYPE = XName.Get("list-type");
        public static readonly XName TARGET_VALUE_OBJECT = XName.Get("target-value-object");
        public static readonly XName TARGET_ELEMENT_TYPE = XName.Get("target-element-type");
        public static readonly XName NAME_IN_ENTITY = XName.Get("name-in-entity");
        public static readonly XName IGNORE = XName.Get("ignore");
        public static readonly XName WITHOUT = XName.Get("without");

        public static readonly XName LINK_MAPPINGS = XName.Get("link-mappings");
        public static readonly XName ENTITY_MAPPINGS = XName.Get("entity-mappings");
        public static readonly XName EXTERNAL_ENTITY = XName.Get("external-entity");
        public static readonly XName LINK = XName.Get("link");
        public static readonly XName EXTERNAL_LINK = XName.Get("external-link");
        public static readonly XName INDEPENDENT_LINK = XName.Get("independent-link");
        public static readonly XName SOURCE = XName.Get("source");
        public static readonly XName CASCADE_DELETE = XName.Get("cascade-delete");
        public static readonly XName LEFT = XName.Get("left");
        public static readonly XName RIGHT = XName.Get("right");
        public static readonly XName BOTH = XName.Get("both");
        public static readonly XName NONE = XName.Get("none");
        public static readonly XName ALIAS = XName.Get("alias");
        public static readonly XName SOURCE_COLUMN = XName.Get("source-column");
        public static readonly XName TARGET_MEMBER = XName.Get("target-member");
        public static readonly XName NO_VERSION = XName.Get("no-version");
        public static readonly XName THIS = XName.Get("this");

        private XmlConstants()
        {
            // Intended blank
        }
    }
}
