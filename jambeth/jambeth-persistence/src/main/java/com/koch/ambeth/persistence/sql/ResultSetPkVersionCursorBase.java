package com.koch.ambeth.persistence.sql;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.merge.transfer.ObjRef;
import com.koch.ambeth.query.persistence.IVersionItem;
import com.koch.ambeth.util.ParamChecker;
import lombok.Setter;

import java.util.Iterator;

public class ResultSetPkVersionCursorBase implements IInitializingBean, IVersionItem {

    @Setter
    protected IResultSet resultSet;

    protected Iterator<Object[]> resultSetIter;

    protected Object[] ids;

    protected Object version;

    @Setter
    protected int compositeIdCount = 1;

    @Setter
    protected int versionIndex;

    @Override
    public void afterPropertiesSet() {
        ParamChecker.assertNotNull(resultSet, "ResultSet");
        ids = new Object[compositeIdCount];
    }

    @Override
    public Object getId() {
        var ids = this.ids;
        if (ids.length == 1) {
            return ids[0];
        }
        return ids;
    }

    @Override
    public Object getId(int idIndex) {
        if (idIndex == ObjRef.PRIMARY_KEY_INDEX) {
            return getId();
        }
        throw new UnsupportedOperationException("No alternate ids have been fetched");
    }

    @Override
    public Object getVersion() {
        return version;
    }

    @Override
    public int getAlternateIdCount() {
        return 0;
    }

    public boolean hasNext() {
        if (resultSetIter == null) {
            resultSetIter = resultSet.iterator();
        }
        return resultSetIter.hasNext();
    }

    public IVersionItem next() {
        var resultSetIter = this.resultSetIter;
        if (resultSetIter == null) {
            resultSetIter = resultSet.iterator();
            this.resultSetIter = resultSetIter;
        }
        var current = resultSetIter.next();
        processResultSetItem(current);
        return this;
    }

    protected void processResultSetItem(Object[] current) {
        var ids = this.ids;
        if (current != null) {
            if (ids.length == 1) {
                ids[0] = current[0];
            } else {
                for (int a = ids.length; a-- > 0; ) {
                    ids[a] = current[a];
                }
            }
            version = versionIndex != -1 ? current[versionIndex] : null;
        } else {
            if (ids.length == 1) {
                ids[0] = null;
            } else {
                for (int a = ids.length; a-- > 0; ) {
                    ids[a] = null;
                }
            }
            version = null;
        }
    }

    public void dispose() {
        if (resultSet != null) {
            resultSetIter = null;
            resultSet.dispose();
            resultSet = null;
        }
    }
}
