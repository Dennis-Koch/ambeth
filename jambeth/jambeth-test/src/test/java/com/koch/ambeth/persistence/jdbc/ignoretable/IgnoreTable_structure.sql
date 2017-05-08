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
CREATE TABLE "TBL_NO_PK"
  (
    "NO_PK_ID"   NUMBER NOT NULL
);
CREATE SEQUENCE "TBL_NO_PK_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;

CREATE TABLE "TBL_NO_SEQ"
  (
    "NO_SEQ_ID"   NUMBER NOT NULL,
    CONSTRAINT "PK_NO_SEQ" PRIMARY KEY ("NO_SEQ_ID") USING INDEX
);

CREATE TABLE "TBL_MAY_BE_LINK"
  (
    "NO_LINK_LEFT_ID"   NUMBER NOT NULL,
    "NO_LINK_RIGHT_ID"   NUMBER NOT NULL,
    CONSTRAINT "PK_MAY_BE_LINK" PRIMARY KEY ("NO_LINK_LEFT_ID", "NO_LINK_RIGHT_ID") USING INDEX
);
CREATE SEQUENCE "TBL_MAY_BE_LINK_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;

CREATE TABLE "TBL_RIGHT"
  (
    "RIGHT_ID"   NUMBER NOT NULL,
    CONSTRAINT "PK_RIGHT" PRIMARY KEY ("RIGHT_ID") USING INDEX
);

CREATE TABLE "TBL_LEFT"
  (
    "LEFT_ID"   NUMBER NOT NULL,
    "FK_RIGHT_ID"   NUMBER NOT NULL,
    CONSTRAINT "TBL_LEFT_FK_RIGHT_ID" FOREIGN KEY ("FK_RIGHT_ID") REFERENCES "TBL_RIGHT" ("RIGHT_ID") DEFERRABLE INITIALLY IMMEDIATE
);
