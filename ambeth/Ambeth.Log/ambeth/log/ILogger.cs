using System;
using System.ServiceModel;

namespace De.Osthus.Ambeth.Log
{
    public interface ILogger
    {
        void Status(String status);

        bool DebugEnabled { get; }

        bool InfoEnabled { get; }

        bool WarnEnabled { get; }

        bool ErrorEnabled { get; }

        void Debug(String message);

        void Debug(String message, Exception e);

        void Debug(String message, ExceptionDetail e);

        void Debug(Exception e);

        void Debug(ExceptionDetail e);

        void Info(String message);

        void Info(String message, Exception e);

        void Info(String message, ExceptionDetail e);

        void Info(System.Exception e);

        void Info(ExceptionDetail e);

        void Warn(String message);

        void Warn(String message, Exception e);

        void Warn(String message, ExceptionDetail e);

        void Warn(Exception e);

        void Warn(ExceptionDetail e);

        void Error(String message);

        void Error(String message, Exception e);

        void Error(String message, ExceptionDetail e);

        void Error(Exception e);

        void Error(ExceptionDetail e);
    }
}
