---
-- #%L
-- jambeth-datachange-persistence
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
CREATE TABLE "DATA_CHANGE_EVENT"
(
	"ID"			NUMBER(18,0) NOT NULL,
	"CHANGE_TIME"	NUMBER(18,0) NOT NULL,
	"VERSION"		NUMBER(3,0) NOT NULL,
	CONSTRAINT "PK_DATA_CHANGE_EVENT" PRIMARY KEY ("ID") USING INDEX
);
CREATE SEQUENCE "DATA_CHANGE_EVENT_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;

CREATE TABLE "DATA_CHANGE_ENTRY"
(
	"ID"				NUMBER(18,0) NOT NULL,
	"ENTITY_TYPE"		NUMBER(9,0) NOT NULL,
	"ID_INDEX"			NUMBER(3,0) NOT NULL,
	"OBJECT_ID"			CLOB NOT NULL,
	"OBJECT_VERSION"	CLOB NOT NULL,
	"INSERT_PARENT"		NUMBER(18,0),
	"UPDATE_PARENT"		NUMBER(18,0),
	"DELETE_PARENT"		NUMBER(18,0),
	"VERSION"			NUMBER(3,0) NOT NULL,
	CONSTRAINT "PK_DATA_CHANGE_ENTRY" PRIMARY KEY ("ID") USING INDEX,
	CONSTRAINT "FK_DCEN_TO_ENTITY_TYPE" FOREIGN KEY ("ENTITY_TYPE") REFERENCES "ENTITY_TYPE" ("ID") DEFERRABLE INITIALLY DEFERRED,
	CONSTRAINT "FK_DCEN_TO_DCEV_INSERT" FOREIGN KEY ("INSERT_PARENT") REFERENCES "DATA_CHANGE_EVENT" ("ID") DEFERRABLE INITIALLY DEFERRED,
	CONSTRAINT "FK_DCEN_TO_DCEV_UPDATE" FOREIGN KEY ("UPDATE_PARENT") REFERENCES "DATA_CHANGE_EVENT" ("ID") DEFERRABLE INITIALLY DEFERRED,
	CONSTRAINT "FK_DCEN_TO_DCEV_DELETE" FOREIGN KEY ("DELETE_PARENT") REFERENCES "DATA_CHANGE_EVENT" ("ID") DEFERRABLE INITIALLY DEFERRED
);
CREATE SEQUENCE "DATA_CHANGE_ENTRY_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;

CREATE TABLE "ENTITY_TYPE"
(
	"ID"		NUMBER(9,0) NOT NULL,
	"TYPE"		VARCHAR2(254 BYTE) NOT NULL,
	"VERSION"	NUMBER(3,0) NOT NULL,
	CONSTRAINT "PK_ENTITY_TYPE" PRIMARY KEY ("ID") USING INDEX,
	CONSTRAINT "AK_ENTITY_TYPE" UNIQUE ("TYPE")
);
CREATE SEQUENCE "ENTITY_TYPE_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;
