CREATE TABLE "D_MATERIAL_GROUP"
  (
    "ID"   VARCHAR2(20 BYTE) NOT NULL,
    "F_NAME" VARCHAR2(50 BYTE) NOT NULL,
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 BYTE),
    "CREATED_BY" VARCHAR2(16 BYTE),
    "VERSION"    NUMBER(*,0),
    CONSTRAINT "MATERIAL_GROUP_PK" PRIMARY KEY ("ID") USING INDEX
  );

CREATE TABLE "D_MATERIAL"
  (
    "ID"   NUMBER NOT NULL,
    "F_NAME" VARCHAR2(50 BYTE) NOT NULL,
    "F_MATERIAL_GROUP" VARCHAR2(20 BYTE) NULL,
    "UPDATED_ON" TIMESTAMP,
    "CREATED_ON" TIMESTAMP,
    "UPDATED_BY" VARCHAR2(16 BYTE),
    "CREATED_BY" VARCHAR2(16 BYTE),
    "VERSION"    NUMBER(*,0),
    CONSTRAINT "MATERIAL_PK" PRIMARY KEY ("ID") USING INDEX,
    CONSTRAINT "MATERIAL_MATERIAL_GROUP_FK1" FOREIGN KEY ("F_MATERIAL_GROUP") REFERENCES "D_MATERIAL_GROUP" ("ID") DEFERRABLE INITIALLY DEFERRED
  );
CREATE SEQUENCE "D_MATERIAL_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;

CREATE TABLE "UNIT"
  (
    "ID"   NUMBER(18,0) NOT NULL,
    "NAME" VARCHAR2(20 BYTE) NOT NULL,
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 BYTE),
    "CREATED_BY" VARCHAR2(16 BYTE),
    "VERSION"    NUMBER(9,0),
    CONSTRAINT "UNIT_PK" PRIMARY KEY ("ID") USING INDEX
  );
CREATE SEQUENCE "UNIT_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 100 CACHE 20 NOORDER NOCYCLE ;


CREATE TABLE "LINK_MATERIAL_UNIT"
  (
    "LEFT_ID"  NUMBER NOT NULL,
    "RIGHT_ID" NUMBER NOT NULL,
    CONSTRAINT "LINK_MATERIAL_UNIT_PK" PRIMARY KEY ("LEFT_ID", "RIGHT_ID") USING INDEX,
    CONSTRAINT "LINK_MATERIAL_UNIT_MATERI_FK1" FOREIGN KEY ("LEFT_ID") REFERENCES "D_MATERIAL" ("ID") DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT "LINK_MATERIAL_UNIT_UNIT_FK1" FOREIGN KEY ("RIGHT_ID") REFERENCES "UNIT" ("ID") DEFERRABLE INITIALLY DEFERRED
  );

CREATE TABLE "D_EMPLOYEE"
  (
    "ID"   NUMBER NOT NULL,
    "F_NAME" VARCHAR2(20 BYTE) NOT NULL,
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 BYTE),
    "CREATED_BY" VARCHAR2(16 BYTE),
    "VERSION"    NUMBER(*,0),
    CONSTRAINT "EMPLOYEE_PK" PRIMARY KEY ("ID") USING INDEX
  );
CREATE SEQUENCE "D_EMPLOYEE_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;

CREATE TABLE "D_PROJECT"
  (
    "ID"   NUMBER NOT NULL,
    "F_NAME" VARCHAR2(20 BYTE) NOT NULL,
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 BYTE),
    "CREATED_BY" VARCHAR2(16 BYTE),
    "VERSION"    NUMBER(*,0),
    CONSTRAINT "PROJECT_PK" PRIMARY KEY ("ID") USING INDEX
  );
CREATE SEQUENCE "D_PROJECT_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;

CREATE TABLE "LINK_EMPLOYEE_PROJECT"
  (
    "LEFT_ID"  NUMBER NOT NULL,
    "RIGHT_ID" NUMBER NOT NULL,
    CONSTRAINT "LINK_EMPLOYEE_PROJECT_PK" PRIMARY KEY ("LEFT_ID", "RIGHT_ID") USING INDEX,
    CONSTRAINT "LINK_EMPLOYEE_PROJECT_L_FK1" FOREIGN KEY ("LEFT_ID") REFERENCES "D_EMPLOYEE" ("ID") DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT "LINK_EMPLOYEE_PROJECT_R_FK1" FOREIGN KEY ("RIGHT_ID") REFERENCES "D_PROJECT" ("ID") DEFERRABLE INITIALLY DEFERRED
  );

CREATE TABLE "LINK_EMPLOYEE_EMPLOYEE"
  (
    "LEFT_ID"  NUMBER NOT NULL,
    "RIGHT_ID" NUMBER NOT NULL,
    CONSTRAINT "LINK_EMPLOYEE_EMPLOYEE_PK" PRIMARY KEY ("LEFT_ID", "RIGHT_ID") USING INDEX,
    CONSTRAINT "LINK_EMPLOYEE_EMPLOYEE_L_FK1" FOREIGN KEY ("LEFT_ID") REFERENCES "D_EMPLOYEE" ("ID") DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT "LINK_EMPLOYEE_EMPLOYEE_R_FK1" FOREIGN KEY ("RIGHT_ID") REFERENCES "D_EMPLOYEE" ("ID") DEFERRABLE INITIALLY DEFERRED
  );
