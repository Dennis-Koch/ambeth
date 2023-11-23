package com.koch.ambeth.persistence.jdbc.exception;

/*-
 * #%L
 * jambeth-persistence-jdbc
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

import jakarta.persistence.PersistenceException;

public abstract class AbstractAmbethPersistenceException extends PersistenceException {
    private static final long serialVersionUID = -6289536750887364782L;

    protected final String relatedSql;

    public AbstractAmbethPersistenceException(String message, String relatedSql, Throwable cause) {
        super(message, cause);
        this.relatedSql = relatedSql;
    }

    public AbstractAmbethPersistenceException(String message, String relatedSql) {
        super(message);
        this.relatedSql = relatedSql;
    }

    public AbstractAmbethPersistenceException(String relatedSql, Throwable cause) {
        super(cause);
        this.relatedSql = relatedSql;
    }

    public String getRelatedSql() {
        return relatedSql;
    }

    @Override
    public String getMessage() {
        if (relatedSql == null) {
            return super.getMessage();
        }
        return super.getMessage() + ". Related SQL: " + relatedSql;
    }
}
