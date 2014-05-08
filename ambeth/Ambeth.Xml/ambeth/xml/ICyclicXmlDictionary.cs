using System;

namespace De.Osthus.Ambeth.Xml
{
    public interface ICyclicXmlDictionary
    {
        // Elements
        String ArrayElement { get; }

        String BlobElement { get; }

        String ClassElement { get; }

        String EntityRefElement { get; }

        String ExceptionElement { get; }

        String ListElement { get; }

        String EnumElement { get; }

        String NumberElement { get; }

        String NullElement { get; }

        String ObjectElement { get; }

        String PostProcessElement { get; }

        String PrimitiveElement { get; }

        String RefElement { get; }

        String RootElement { get; }

        String SetElement { get; }

        String OriWrapperElement { get; }
 
        String SimpleObjectElement { get; }

        String StringElement { get; }

        String TypeElement { get; }

        // Attributes
        String ClassIdAttribute { get; }

        String ClassNameAttribute { get; }

        String ClassNamespaceAttribute { get; }

        String ClassMemberAttribute { get; }

        String SizeAttribute { get; }

        String ArrayDimensionAttribute { get; }

        String EntityRefKeyAttribute { get; }

        String EntityRefKeyIndexAttribute { get; }

        String EntityRefVersionAttribute { get; }

        String IdAttribute { get; }

        String ValueAttribute { get; }
   }
}