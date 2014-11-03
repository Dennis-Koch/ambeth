using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;

namespace OfficeToImages
{
    public interface IFileParserExtendable
    {
        void RegisterFileParser(IFileParser fileParser, String fileExtensionName);

        void UnregisterFileParser(IFileParser fileParser, String fileExtensionName);
    }
}
