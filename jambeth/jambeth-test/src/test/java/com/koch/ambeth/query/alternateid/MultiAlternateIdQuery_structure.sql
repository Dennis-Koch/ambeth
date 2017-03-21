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
CREATE TABLE "MULTI_ALTERNATE_ID_ENTITY"
  (
    "ID"			NUMBER(9,0) NOT NULL,
    "ALTERNATE_ID1"	VARCHAR2(20 CHAR) NOT NULL,
    "ALTERNATE_ID2"	VARCHAR2(20 CHAR) NOT NULL,
    "UPDATED_ON"	DATE,
    "CREATED_ON"	DATE,
    "UPDATED_BY"	VARCHAR2(16 CHAR),
    "CREATED_BY"	VARCHAR2(16 CHAR),
    "VERSION"		NUMBER(9,0),
    CONSTRAINT "PK_MAID_ENTITY" PRIMARY KEY ("ID") USING INDEX,
    CONSTRAINT "UK_MAID_ENTITY_NAME1" UNIQUE ("ALTERNATE_ID1"),
    CONSTRAINT "UK_MAID_ENTITY_NAME2" UNIQUE ("ALTERNATE_ID2")
);
CREATE SEQUENCE "MULTI_ALTERNATE_ID_ENTITY_SEQ" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE;
