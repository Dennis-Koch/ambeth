CREATE TABLE "LOCAL_ENTITY"
  (
    "ID"   NUMBER NOT NULL,
    "NAME" VARCHAR2(50 BYTE) NOT NULL,
    "VALUE" NUMBER NOT NULL,
    "PARENT" VARCHAR2(50 BYTE) NOT NULL,
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 BYTE),
    "CREATED_BY" VARCHAR2(16 BYTE),
    "VERSION"    NUMBER(*,0),
    CONSTRAINT "LOCAL_ENTITY_PK" PRIMARY KEY ("ID") USING INDEX,
    CONSTRAINT "LOCAL_ENTITY_AE_NAME" UNIQUE ("NAME")
  );
CREATE SEQUENCE "LOCAL_ENTITY_SEQU"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;

CREATE TABLE "LINK_LOC_ENTITY_EXT_ENTITY"
  (
    "LEFT_ID"  NUMBER NOT NULL,
    "RIGHT_ID" VARCHAR2(50 BYTE) NOT NULL,
    CONSTRAINT "LINK_LOC_ENTITY_EXT_ENTITY_PK" PRIMARY KEY ("LEFT_ID", "RIGHT_ID") USING INDEX,
    CONSTRAINT "LINK_LOC_ENTITY_EXT_ENTITY_FK" FOREIGN KEY ("LEFT_ID") REFERENCES "LOCAL_ENTITY" ("ID") DEFERRABLE INITIALLY IMMEDIATE
  );

CREATE TABLE "LINK_LOCAL_TO_SIBLING"
  (
    "LEFT_ID"  NUMBER NOT NULL,
    "RIGHT_ID" VARCHAR2(50 BYTE) NOT NULL,
    CONSTRAINT "LINK_LOCAL_TO_SIBLING_PK" PRIMARY KEY ("LEFT_ID", "RIGHT_ID") USING INDEX,
    CONSTRAINT "LINK_LOCAL_TO_SIBLING_FK" FOREIGN KEY ("LEFT_ID") REFERENCES "LOCAL_ENTITY" ("ID") DEFERRABLE INITIALLY IMMEDIATE
  );
