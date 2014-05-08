CREATE TABLE "PARENT"
  (
    "ID"   NUMBER(*,0) NOT NULL,
    "NAME" VARCHAR2(50 CHAR) NOT NULL,
    "VALUE" NUMBER NOT NULL,
    "UPDATED_ON" TIMESTAMP(3),
    "CREATED_ON" TIMESTAMP(3),
    "UPDATED_BY" VARCHAR2(16 CHAR),
    "CREATED_BY" VARCHAR2(16 CHAR),
    "VERSION"    NUMBER(*,0),
    CONSTRAINT "PARENT_PK" PRIMARY KEY ("ID") USING INDEX
  );
CREATE SEQUENCE "PARENT_SEQU"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;