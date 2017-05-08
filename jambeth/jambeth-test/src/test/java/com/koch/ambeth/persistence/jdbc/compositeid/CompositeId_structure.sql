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
CREATE TABLE COMPOSITE_ID_ENTITY
(
	"ID1"		NUMBER NOT NULL,
	"BUID"		VARCHAR2(50 CHAR) NOT NULL,
	"ALT_ID1"	NUMBER NOT NULL,
	"ALT_ID2"	VARCHAR2(50 CHAR) NOT NULL,
	"ALT_ID3"	NUMBER NOT NULL,
	"ALT_ID4"	VARCHAR2(50 CHAR) NOT NULL,
	"NAME"		VARCHAR2(50 CHAR) NOT NULL,
	CONSTRAINT "NO_VERSION_BACKING_PK" PRIMARY KEY ("ID1","BUID") USING INDEX,
	UNIQUE ("ALT_ID1","ALT_ID2","ALT_ID3","ALT_ID4") USING INDEX
);
CREATE SEQUENCE "COMPOSITE_ID_ENTITY_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;
