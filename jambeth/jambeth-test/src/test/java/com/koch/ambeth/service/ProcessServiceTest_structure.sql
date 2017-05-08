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
CREATE TABLE "MATERIAL_GROUP" (
	"ID"			VARCHAR2(20 BYTE) NOT NULL,
	"BUID"			VARCHAR2(50 BYTE) NOT NULL,
	"NAME"			VARCHAR2(50 BYTE) NOT NULL,
	"UPDATED_ON"	DATE,
	"CREATED_ON"	DATE,
	"UPDATED_BY"	VARCHAR2(16 BYTE),
	"CREATED_BY"	VARCHAR2(16 BYTE),
	"VERSION"		NUMBER(3,0),
	CONSTRAINT "MATERIAL_GROUP_PK" PRIMARY KEY ("ID") USING INDEX,
	CONSTRAINT "MATERIAL_GROUP_AK" UNIQUE ("BUID") USING INDEX
);

CREATE TABLE "MATERIAL" (
	"ID"				NUMBER(10,0) NOT NULL,
	"BUID"				VARCHAR2(50 BYTE) NOT NULL,
	"NAME"				VARCHAR2(50 BYTE) NOT NULL,
	"MATERIAL_GROUP"	VARCHAR2(20 BYTE) NULL,
	"UPDATED_ON"		TIMESTAMP,
	"CREATED_ON"		TIMESTAMP,
	"UPDATED_BY"		VARCHAR2(16 BYTE),
	"CREATED_BY"		VARCHAR2(16 BYTE),
	"VERSION"			NUMBER(3,0),
	CONSTRAINT "MATERIAL_PK" PRIMARY KEY ("ID") USING INDEX,
	CONSTRAINT "MATERIAL_AK" UNIQUE ("BUID") USING INDEX,
	CONSTRAINT "MATERIAL_MATERIAL_GROUP_FK1" FOREIGN KEY ("MATERIAL_GROUP") REFERENCES "MATERIAL_GROUP" ("ID") DEFERRABLE INITIALLY DEFERRED
);
CREATE SEQUENCE "MATERIAL_SEQ"  MINVALUE 1 MAXVALUE 2000000000 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;
