CREATE OR REPLACE TYPE "STRING_ARRAY" AS VARRAY(4000) OF VARCHAR2(4000 CHAR);

CREATE TABLE "IGNORE_ME"
  (
    "ID"   NUMBER(18,0) NOT NULL,
    "NAME" VARCHAR2(50 BYTE) NOT NULL,
    "VERSION"    NUMBER(18,0),
    CONSTRAINT "IGNORE_ME_PK" PRIMARY KEY ("ID") USING INDEX
  );

CREATE TABLE "IGNORE_ME_TWO"
  (
    "ID"   NUMBER(18,0) NOT NULL,
    "NAME" VARCHAR2(50 BYTE) NOT NULL,
    "VERSION"    NUMBER(18,0),
    CONSTRAINT "IGNORE_ME_TWO_PK" PRIMARY KEY ("ID") USING INDEX
  );

CREATE TABLE "Employee"
  (
    "ID"   NUMBER(18,0) NOT NULL,
    "Name" VARCHAR2(50 BYTE) NOT NULL,
    "Name2" VARCHAR2(50 BYTE),
    "NICKNAMES" STRING_ARRAY,
    "PRIMARY_ADDRESS_ID" NUMBER(18,0) NOT NULL,
    "SUPERVISOR"   NUMBER(18,0) NULL,
    "PRIMARY_PROJECT" NUMBER(18,0) NOT NULL,
    "SECONDARY_PROJECT" NUMBER(18,0) NULL,
    "CAR_MAKE" VARCHAR2(50 BYTE) NULL,
    "CAR_MODEL" VARCHAR2(50 BYTE) NULL,
    "BOAT" NUMBER(18,0) NULL,
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 BYTE),
    "CREATED_BY" VARCHAR2(16 BYTE),
    "VERSION"    NUMBER(18,0),
    CONSTRAINT "EMPLOYEE_PK" PRIMARY KEY ("ID") USING INDEX,
    CONSTRAINT "EMPLOYEE_AE_NAME" UNIQUE ("Name"),
    CONSTRAINT "EMPLOYEE_AE_ADDRESS" UNIQUE ("PRIMARY_ADDRESS_ID"),
    CONSTRAINT "LINK_EMPLOYEE_PRIM_ADDR_FK1" FOREIGN KEY ("PRIMARY_ADDRESS_ID") REFERENCES "ADDRESS" ("ID") DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT "LINK_EMPLOYEE_EMPLOYEE_FK1" FOREIGN KEY ("SUPERVISOR") REFERENCES "Employee" ("ID") DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT "LINK_EMPLOYEE_PROJECT_FK1" FOREIGN KEY ("PRIMARY_PROJECT") REFERENCES "PROJECT" ("ID") DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT "LINK_EMPLOYEE_PROJECT_FK2" FOREIGN KEY ("SECONDARY_PROJECT") REFERENCES "PROJECT" ("ID") DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT "LINK_EMPLOYEE_BOAT_FK1" FOREIGN KEY ("BOAT") REFERENCES "BOAT" ("ID") DEFERRABLE INITIALLY DEFERRED
  );
CREATE SEQUENCE "EMPLOYEE_SEQU"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;

CREATE TABLE "ADDRESS"
  (
    "ID"   NUMBER(18,0) NOT NULL,
    "RESIDENT"   NUMBER(18,0) NULL,
    "STREET" VARCHAR2(50 BYTE) NOT NULL,
    "CITY" VARCHAR2(50 BYTE) NOT NULL,
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 BYTE),
    "CREATED_BY" VARCHAR2(16 BYTE),
    "VERSION"    NUMBER(18,0),
    CONSTRAINT "ADDRESS_PK" PRIMARY KEY ("ID") USING INDEX
  );
CREATE SEQUENCE "ADDRESS_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;

ALTER TABLE "ADDRESS"
    ADD CONSTRAINT "LINK_ADDRESS_EMPLOYEE_FK1" FOREIGN KEY ("RESIDENT") REFERENCES "Employee" ("ID") DEFERRABLE INITIALLY DEFERRED;

CREATE TABLE "PROJECT"
  (
    "ID"   NUMBER(18,0) NOT NULL,
    "NAME" VARCHAR2(50 BYTE) NOT NULL,
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 BYTE),
    "CREATED_BY" VARCHAR2(16 BYTE),
    "VERSION"    NUMBER(18,0),
    CONSTRAINT "PROJECT_PK" PRIMARY KEY ("ID") USING INDEX
  );
CREATE SEQUENCE "PROJECT_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;

CREATE TABLE "LINK_EMPLOYEE_PROJECT"
  (
    "EMPLOYEE_ID"  NUMBER(18,0) NOT NULL,
    "PROJECT_ID" NUMBER(18,0) NOT NULL,
    CONSTRAINT "LINK_EMPLOYEE_PROJECT_PK" PRIMARY KEY ("EMPLOYEE_ID", "PROJECT_ID") USING INDEX,
    CONSTRAINT "LINK_EMPLOYEE_PROJECT_FK3" FOREIGN KEY ("EMPLOYEE_ID") REFERENCES "Employee" ("ID") DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT "LINK_EMPLOYEE_PROJECT_FK4" FOREIGN KEY ("PROJECT_ID") REFERENCES "PROJECT" ("ID") DEFERRABLE INITIALLY DEFERRED
  );

CREATE TABLE "BOAT"
  (
    "ID"   NUMBER(18,0) NOT NULL,
    "NAME" VARCHAR2(50 BYTE) NOT NULL,
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 BYTE),
    "CREATED_BY" VARCHAR2(16 BYTE),
    "VERSION"    NUMBER(18,0),
    CONSTRAINT "BOAT_PK" PRIMARY KEY ("ID") USING INDEX
  );
CREATE SEQUENCE "BOAT_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;
