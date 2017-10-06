CREATE TABLE "ENTITY"
  (
    "ID"   NUMBER NOT NULL,
    "ALTERNATE_ID"  VARCHAR2(8 CHAR) NOT NULL,
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 CHAR),
    "CREATED_BY" VARCHAR2(16 CHAR),
    "VERSION"    NUMBER(*,0),
    CONSTRAINT "PK_QUERY_ENTITY" PRIMARY KEY ("ID") USING INDEX,
    CONSTRAINT "AK_QUERY_ENTITY_ALTERNATE_ID" UNIQUE ("ALTERNATE_ID") USING INDEX
);
CREATE SEQUENCE "ENTITY_SEQ" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE;