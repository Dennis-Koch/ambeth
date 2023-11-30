package com.koch.ambeth.persistence;

/*-
 * #%L
 * jambeth-persistence
 * %%
 * Copyright (C) 2017 Koch Softwaredevelopment
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 * #L%
 */

import com.koch.ambeth.persistence.api.IDatabase;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.query.IOperand;
import com.koch.ambeth.query.IValueOperand;
import com.koch.ambeth.util.appendable.IAppendable;
import com.koch.ambeth.util.collections.ILinkedMap;
import com.koch.ambeth.util.collections.IList;
import com.koch.ambeth.util.collections.IMap;
import com.koch.ambeth.util.state.IStateRollback;
import jakarta.persistence.PersistenceException;

import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Savepoint;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public interface IConnectionDialect {
    String escapeName(CharSequence symbolName);

    IAppendable escapeName(CharSequence symbolName, IAppendable sb);

    String escapeSchemaAndSymbolName(CharSequence schemaName, CharSequence symbolName);

    String getEscapeLiteral();

    String getSelectForUpdateFragment();

    Blob createBlob(Connection connection);

    void releaseBlob(Blob blob);

    Clob createClob(Connection connection);

    void releaseClob(Clob clob);

    void releaseArray(java.sql.Array array);

    String toDefaultCase(String unquotedIdentifier);

    void preProcessConnection(Connection connection, String[] schemaNames, boolean forcePreProcessing);

    IList<IMap<String, String>> getExportedKeys(Connection connection, String[] schemaNames);

    ILinkedMap<String, IList<String>> getFulltextIndexes(Connection connection, String schemaName);

    boolean isSystemTable(String tableName);

    IStateRollback disableConstraints(Connection connection, String... schemaNames);

    void commit(Connection connection);

    void rollback(Connection connection);

    void releaseSavepoint(Savepoint savepoint, Connection connection);

    IOperand getRegexpLikeFunction(IOperand sourceString, IOperand pattern, IOperand matchParameter);

    IOperand getLimitOperand(IOperand operand, IValueOperand valueOperand);

    int getResourceBusyErrorCode();

    PersistenceException createPersistenceException(SQLException e, String relatedSql);

    boolean useVersionOnOptimisticUpdate();

    ResultSet getIndexInfo(Connection connection, String schemaName, String tableName, boolean unique);

    Class<?> getComponentTypeByFieldTypeName(String additionalFieldInfo);

    String getFieldTypeNameByComponentType(Class<?> componentType);

    List<String> getAllFullqualifiedTableNames(Connection connection, String... schemaNames);

    List<String> getAllFullqualifiedViews(Connection connection, String... schemaNames);

    List<String> getAllFullqualifiedSequences(Connection connection, String... schemaNames);

    IList<IColumnEntry> getAllFieldsOfTable(Connection connection, String fqTableName);

    IList<String> queryDefault(Connection connection, String resultColumnName, String sql, Object... args);

    int getMaxInClauseBatchThreshold();

    boolean isEmptyStringAsNullStored(IFieldMetaData field);

    Object convertToFieldType(IFieldMetaData field, Object value);

    Object convertFromFieldType(IDatabase database, IFieldMetaData field, Class<?> expectedType, Object value);

    boolean isTransactionNecessaryDuringLobStreaming();

    void appendIsInOperatorClause(IAppendable appendable);

    boolean isCompactMultiValueRecommended(IList<Object> values);

    void handleWithMultiValueLeftField(IAppendable querySB, Map<Object, Object> nameToValueMap, IList<Object> parameters, IList<IList<Object>> splitValues, boolean caseSensitive,
            Class<?> leftOperandFieldType);

    /**
     * append the best suited list clause IN( ?,?,?,?,?) (with ids as single parameters) _OR_ = ANY
     * [?] (with an array)
     *
     * @param parameters
     * @param sb
     * @param fieldType
     * @param splittedIds
     * @param idDecompositor
     */
    void appendListClause(List<Object> parameters, IAppendable sb, Class<?> fieldType, IList<Object> splittedIds, Function<Object, Object> idDecompositor);

    /**
     * Replace Oracle SQL with connection dialect specific SQL
     *
     * @param sqlCommand
     * @return
     */
    String prepareCommand(String sqlCommand);

    SelectPosition getLimitPosition();

    int getColumnCountForLinkTable();

    String buildClearTableSQL(String tableName);

    Class<?>[] getConnectionInterfaces(Connection connection);
}
