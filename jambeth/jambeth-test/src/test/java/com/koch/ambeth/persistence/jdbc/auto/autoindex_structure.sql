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
CREATE TABLE "TBL_AE"
  (
    "ID"   NUMBER NOT NULL,
    "NAME" VARCHAR2(20 BYTE) NOT NULL,
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 BYTE),
    "CREATED_BY" VARCHAR2(16 BYTE),
    "VERSION"    NUMBER(*,0),
    CONSTRAINT "PK_AE" PRIMARY KEY ("ID") USING INDEX,
    CONSTRAINT "UK_AE_NAME" UNIQUE ("NAME")
);
CREATE SEQUENCE "TBL_AE_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;

CREATE TABLE "TBL_BASE"
  (
    "ID"   NUMBER NOT NULL,
    "NAME" VARCHAR2(20 BYTE) NOT NULL,
    "AE_NAME" VARCHAR2(20 BYTE),
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 BYTE),
    "CREATED_BY" VARCHAR2(16 BYTE),
    "VERSION"    NUMBER(*,0),
    CONSTRAINT "PK_BASE" PRIMARY KEY ("ID") USING INDEX,
    CONSTRAINT "UK_BASE_NAME" UNIQUE ("NAME"),
    CONSTRAINT "FK_BASE_AE_NAME_TO_AE_NAME30" FOREIGN KEY ("AE_NAME") REFERENCES "TBL_AE" ("NAME") DEFERRABLE INITIALLY DEFERRED
);
CREATE SEQUENCE "TBL_BASE_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;

CREATE TABLE "TBL_BASE2"
  (
    "ID"   NUMBER NOT NULL,
    "NAME" VARCHAR2(20 BYTE) NOT NULL,
    "AE_NAME" VARCHAR2(20 BYTE),
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 BYTE),
    "CREATED_BY" VARCHAR2(16 BYTE),
    "VERSION"    NUMBER(*,0),
    CONSTRAINT "PK_BASE2" PRIMARY KEY ("ID") USING INDEX,
    CONSTRAINT "UK_BASE2_NAME" UNIQUE ("NAME"),
    CONSTRAINT "UK_BASE2_AE_NAME" UNIQUE ("AE_NAME"),
    CONSTRAINT "FK_BASE2_AE_NAME_TO_AE_NAME" FOREIGN KEY ("AE_NAME") REFERENCES "TBL_AE" ("NAME") DEFERRABLE INITIALLY DEFERRED
);
