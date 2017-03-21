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

CREATE TABLE "REF_ENTITY"
  (
    "ID"   NUMBER NOT NULL,
    "VERSION"    NUMBER(*,0) NOT NULL,
    CONSTRAINT "EMPLOYEE_PK" PRIMARY KEY ("ID") USING INDEX
  );
CREATE SEQUENCE "REF_ENTITY_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;

CREATE TABLE "LINK_REF_ENTITY_REF_ENTITY"
  (
    "LEFT_ID"  NUMBER NOT NULL,
    "RIGHT_ID" NUMBER NOT NULL,
    CONSTRAINT "LINK_REF_ENTITY_REF_ENTITY_PK" PRIMARY KEY ("LEFT_ID", "RIGHT_ID") USING INDEX,
    CONSTRAINT "LINK_REF_ENTITY_REF_ENTITY_FK1" FOREIGN KEY ("LEFT_ID") REFERENCES "REF_ENTITY" ("ID") DEFERRABLE INITIALLY IMMEDIATE,
    CONSTRAINT "LINK_REF_ENTITY_REF_ENTITY_FK2" FOREIGN KEY ("RIGHT_ID") REFERENCES "REF_ENTITY" ("ID") DEFERRABLE INITIALLY IMMEDIATE
  );
