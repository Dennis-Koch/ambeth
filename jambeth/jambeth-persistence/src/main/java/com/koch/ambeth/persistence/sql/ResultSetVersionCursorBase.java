package com.koch.ambeth.persistence.sql;

import com.koch.ambeth.merge.transfer.ObjRef;
import lombok.Setter;

import java.util.function.Function;

public class ResultSetVersionCursorBase extends ResultSetPkVersionCursorBase {
    private static final Object[][] EMPTY_ALTERNATE_IDS_ARRAY = new Object[0][0];

    protected Object[][] alternateIdsArray;

    protected int idCompositePlusVersionCount;

    @Setter
    protected Function<Object[], Object[]>[] resultSetItemToAlternateIdConverter;

    @Override
    public void afterPropertiesSet() {
        super.afterPropertiesSet();
        if (resultSetItemToAlternateIdConverter != null) {
            idCompositePlusVersionCount = compositeIdCount + (versionIndex != -1 ? 1 : 0);
            alternateIdsArray = new Object[resultSetItemToAlternateIdConverter.length][];
        } else {
            alternateIdsArray = EMPTY_ALTERNATE_IDS_ARRAY;
        }
    }

    @Override
    public Object getId(int idIndex) {
        if (idIndex == ObjRef.PRIMARY_KEY_INDEX) {
            return getId();
        }
        var alternateIds = alternateIdsArray[idIndex];
        if (alternateIds != null && alternateIds.length == 1) {
            return alternateIds[0];
        }
        return alternateIds;
    }

    @Override
    public int getAlternateIdCount() {
        return alternateIdsArray.length;
    }

    protected void processResultSetItem(Object[] current) {
        super.processResultSetItem(current);
        var alternateIdsArray = this.alternateIdsArray;
        if (alternateIdsArray.length == 0) {
            return;
        }
        if (current == null) {
            if (alternateIdsArray.length == 1) {
                alternateIdsArray[0] = null;
            } else {
                for (int a = alternateIdsArray.length; a-- > 0; ) {
                    alternateIdsArray[a] = null;
                }
            }
            return;
        }
        var resultSetItemToAlternateIdConverter = this.resultSetItemToAlternateIdConverter;
        if (alternateIdsArray.length == 1) {
            alternateIdsArray[0] = resultSetItemToAlternateIdConverter[0].apply(current);
        } else {
            for (int a = alternateIdsArray.length; a-- > 0; ) {
                alternateIdsArray[a] = resultSetItemToAlternateIdConverter[a].apply(current);
            }
        }
    }
}
