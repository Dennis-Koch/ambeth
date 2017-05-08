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
CREATE TABLE "ENTITY_A"
  (
    "ID"   			NUMBER(9,0) NOT NULL,
    "BUID"			VARCHAR2(20 CHAR) NOT NULL,
    "ENTITY_B"		NUMBER,
    "UPDATED_ON"	DATE,
    "CREATED_ON"	DATE,
    "UPDATED_BY"	VARCHAR2(16 CHAR),
    "CREATED_BY"	VARCHAR2(16 CHAR),
    "VERSION"   	NUMBER(9,0),
    CONSTRAINT "PK_ENTITY_A" PRIMARY KEY ("ID") USING INDEX,
    CONSTRAINT "UK_ENTITY_A_BUID" UNIQUE ("BUID"),
    CONSTRAINT "LINK_ENTITY_A_ENTITY_B_FK1" FOREIGN KEY ("ENTITY_B") REFERENCES "ENTITY_B" ("ID") DEFERRABLE INITIALLY DEFERRED
);
CREATE SEQUENCE "ENTITY_A_SEQ" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE;

CREATE TABLE "ENTITY_B"
  (
    "ID"			NUMBER NOT NULL,
    "BUID"			VARCHAR2(20 CHAR) NOT NULL,
    "UPDATED_ON"	DATE,
    "CREATED_ON"	DATE,
    "UPDATED_BY"	VARCHAR2(16 CHAR),
    "CREATED_BY"	VARCHAR2(16 CHAR),
    "VERSION"   	NUMBER(*,0),
    CONSTRAINT "UK_ENTITY_B_BUID" UNIQUE ("BUID"),
    CONSTRAINT "PK_ENTITY_B" PRIMARY KEY ("ID") USING INDEX
);
CREATE SEQUENCE "ENTITY_B_SEQ" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE;
