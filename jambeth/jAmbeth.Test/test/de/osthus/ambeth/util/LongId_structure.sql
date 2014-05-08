CREATE TABLE "LONG_ID_ENTITY"
  (
    "ID"   NUMBER NOT NULL,
    "NAME" VARCHAR2(50 BYTE) NOT NULL,
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 BYTE),
    "CREATED_BY" VARCHAR2(16 BYTE),
    "VERSION"    NUMBER(*,0),
    CONSTRAINT "LONG_ID_ENTITY_PK" PRIMARY KEY ("ID") USING INDEX
  );
CREATE SEQUENCE "LONG_ID_ENTITY_SEQU"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;
