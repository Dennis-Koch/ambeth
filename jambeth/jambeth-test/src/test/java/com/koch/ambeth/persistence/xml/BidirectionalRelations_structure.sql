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

CREATE TABLE "GROUP"
  (
    "ID"   NUMBER(18,0) NOT NULL,
    "VERSION"    NUMBER(18,0) NOT NULL,
    "Name" VARCHAR2(50 BYTE) NOT NULL,
    CONSTRAINT "GROUP_PK" PRIMARY KEY ("ID") USING INDEX,
    CONSTRAINT "GROUP_AE_NAME" UNIQUE ("Name")
  );
CREATE SEQUENCE "GROUP_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;

CREATE TABLE "LINK_GROUP_GROUP"
  (
    "GROUP_ID"  NUMBER(18,0) NOT NULL,
    "CHILD_GROUP_ID" NUMBER(18,0) NOT NULL,
    CONSTRAINT "LINK_GROUP_PROJECT_PK" PRIMARY KEY ("GROUP_ID", "CHILD_GROUP_ID") USING INDEX,
    CONSTRAINT "LINK_GROUP_PROJECT_FK1" FOREIGN KEY ("GROUP_ID") REFERENCES "GROUP" ("ID") DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT "LINK_GROUP_PROJECT_FK2" FOREIGN KEY ("CHILD_GROUP_ID") REFERENCES "GROUP" ("ID") DEFERRABLE INITIALLY DEFERRED
  );
