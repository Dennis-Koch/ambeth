package com.koch.ambeth.persistence.sql;

import com.koch.ambeth.merge.compositeid.CompositeIdMember;
import com.koch.ambeth.persistence.api.IFieldMetaData;
import com.koch.ambeth.service.merge.IEntityMetaDataProvider;
import com.koch.ambeth.service.merge.model.IObjRef;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.function.Function;

@RequiredArgsConstructor
@Getter
public class IdContainerImpl implements ISqlConnection.IdContainer {

    public static final Function[] SINGLE_NO_OP_DECOMPOSITOR = new Function[1];

    public static ISqlConnection.IdContainer ofIdIndex(IFieldMetaData idField, List<?> ids) {
        if (idField.getIdIndex() == IObjRef.UNDEFINED_KEY_INDEX) {
            throw new IllegalStateException("Field must be from an idIndex: " + idField);
        }
        return new IdContainerImpl(ids.size(), 1, idField.getIdIndex(), new String[] { idField.getName() }, new Class<?>[] { idField.getFieldType() }, ids, SINGLE_NO_OP_DECOMPOSITOR);
    }

    public static ISqlConnection.IdContainer ofIdIndex(IFieldMetaData[] idIndexFields, List<?> ids, Class<?> entityType, IEntityMetaDataProvider entityMetaDataProvider) {
        int idIndex = IObjRef.UNDEFINED_KEY_INDEX;
        var fieldNames = new String[idIndexFields.length];
        var fieldTypes = new Class<?>[idIndexFields.length];

        for (int a = idIndexFields.length; a-- > 0; ) {
            var field = idIndexFields[a];
            var fieldIdIndex = field.getIdIndex();
            if (fieldIdIndex == IObjRef.UNDEFINED_KEY_INDEX) {
                throw new IllegalStateException("Field must be from an idIndex: " + field);
            }
            if (idIndex == IObjRef.UNDEFINED_KEY_INDEX) {
                idIndex = fieldIdIndex;
            } else if (idIndex != fieldIdIndex) {
                throw new IllegalStateException("Not from same idIndex: " + idIndex + " vs. " + fieldIdIndex);
            }
            fieldNames[a] = field.getName();
            fieldTypes[a] = field.getFieldType();
        }
        Function<Object, Object>[] idDecompositors;
        if (idIndexFields.length > 1) {
            idDecompositors = new Function[idIndexFields.length];
            var metaData = entityMetaDataProvider.getMetaData(entityType);
            var idMember = (CompositeIdMember) metaData.getIdMember();
            for (int compositeMemberIndex = idIndexFields.length; compositeMemberIndex-- > 0; ) {
                var fCompositeMemberIndex = compositeMemberIndex;
                idDecompositors[compositeMemberIndex] = id -> idMember.getDecompositedValue(id, fCompositeMemberIndex);
            }
        } else {
            idDecompositors = SINGLE_NO_OP_DECOMPOSITOR;
        }
        return new IdContainerImpl(ids.size(), idIndexFields.length, idIndex, fieldNames, fieldTypes, ids, idDecompositors);
    }

    public static ISqlConnection.IdContainer of(String fieldName, Class<?> fieldType, List<?> ids) {
        return new IdContainerImpl(ids.size(), 1, IObjRef.UNDEFINED_KEY_INDEX, new String[] { fieldName }, new Class<?>[] { fieldType }, ids, SINGLE_NO_OP_DECOMPOSITOR);
    }

    public static ISqlConnection.IdContainer ofChunk(ISqlConnection.IdContainer parent, List<?> chunkedIds) {
        return new IdContainerImpl(chunkedIds.size(), parent.getDecomposedIdCount(), parent.getIdIndex(), parent.getFieldNames(), parent.getFieldTypes(), chunkedIds, parent.getIdDecompositors());
    }

    final int amountOfIds;

    final int decomposedIdCount;

    final int idIndex;

    final String[] fieldNames;

    final Class<?>[] fieldTypes;

    final List<?> values;

    final Function<Object, Object>[] idDecompositors;
}
