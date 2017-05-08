using System.IO;

namespace OfficeToImages
{
    public interface IFileParser
    {
        void PreParse();

        bool Parse(FileInfo sourceFile, DirectoryInfo targetDir);

        void PostParse();
    }
}
