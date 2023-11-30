package com.koch.ambeth.persistence.sql;

import com.koch.ambeth.ioc.IInitializingBean;
import com.koch.ambeth.util.ParamChecker;
import com.koch.ambeth.util.collections.IList;

import java.util.Iterator;
import java.util.List;

public class CompositeResultSet implements IInitializingBean, IResultSet, Iterator<Object[]> {
    protected IList<IResultSetProvider> resultSetProviderStack;
    protected IResultSet resultSet;

    protected Iterator<Object[]> resultSetIter;

    @Override
    public void afterPropertiesSet() {
        ParamChecker.assertNotNull(resultSetProviderStack, "ResultSetProviderStack");
    }

    @Override
    public void dispose() {
        if (resultSet != null) {
            resultSet.dispose();
            resultSet = null;
        }
        if (resultSetProviderStack != null) {
            for (var resultSetProvider : resultSetProviderStack) {
                resultSetProvider.skipResultSet();
            }
            resultSetProviderStack = null;
        }
    }

    public List<IResultSetProvider> getResultSetProviderStack() {
        return resultSetProviderStack;
    }

    public void setResultSetProviderStack(IList<IResultSetProvider> resultSetProviderStack) {
        this.resultSetProviderStack = resultSetProviderStack;
    }

    protected IResultSet resolveNextResultSet() {
        var resultSetProviderStack = this.resultSetProviderStack;
        if (resultSetProviderStack == null) {
            return null;
        }
        var resultSetProvider = resultSetProviderStack.popLastElement();
        if (resultSetProvider == null) {
            this.resultSetProviderStack = null;
        }
        return resultSetProvider.acquireResultSet();
    }

    @Override
    public boolean hasNext() {
        while (true) {
            var resultSetIter = this.resultSetIter;
            if (resultSetIter != null) {
                if (resultSetIter.hasNext()) {
                    return true;
                }
                resultSet.dispose();
                resultSet = null;
                this.resultSetIter = null;
            }
            resultSet = resolveNextResultSet();
            if (resultSet == null) {
                return false;
            }
            this.resultSetIter = resultSet.iterator();
        }
    }

    @Override
    public Object[] next() {
        if (resultSetIter != null) {
            return resultSetIter.next();
        }
        return null;
    }

    @Override
    public Iterator<Object[]> iterator() {
        return this;
    }
}
