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
CREATE TABLE "ENTITY"
(
	"ID"			NUMBER(9,0) NOT NULL,
	"NAME"			VARCHAR2(80 BYTE) NOT NULL,
	"UPDATED_ON"	DATE,
	"CREATED_ON"	DATE,
	"UPDATED_BY"	VARCHAR2(16 BYTE),
	"CREATED_BY"	VARCHAR2(16 BYTE),
	"VERSION"		NUMBER(9,0),
	CONSTRAINT "ENTITY_PK" PRIMARY KEY ("ID") USING INDEX
);
CREATE SEQUENCE "ENTITY_SEQ" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;
