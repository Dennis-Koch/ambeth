using System;

namespace De.Osthus.Ambeth.Persistence
{
    public interface IPermissionGroup
    {
        ITable Table { get; }

        ITable TargetTable { get; }

        IField PermissionGroupFieldOnTarget { get; }

        IField UserField { get; }

        IField PermissionGroupField { get; }

        IField ReadPermissionField { get; }

        IField UpdatePermissionField { get; }

        IField DeletePermissionField { get; }
    }
}