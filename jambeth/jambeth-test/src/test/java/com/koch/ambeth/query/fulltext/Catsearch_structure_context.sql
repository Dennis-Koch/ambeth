---
-- #%L
-- jambeth-test
-- %%
-- Copyright (C) 2017 Koch Softwaredevelopment
-- %%
-- Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-- #L%
---
CREATE INDEX "FTI_QE_FTNAME1" ON "QUERY_ENTITY"("FTNAME1") INDEXTYPE IS CTXSYS.CONTEXT;
CALL CTX_DDL.SYNC_INDEX('FTI_QE_FTNAME1', '2M');
