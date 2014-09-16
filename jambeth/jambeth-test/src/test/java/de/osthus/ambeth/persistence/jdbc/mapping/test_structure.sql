CREATE OR REPLACE TYPE "CHAR_ARRAY" AS VARRAY(4000) OF CHAR(50);

CREATE TABLE "MATERIAL"
  (
    "ID"   NUMBER NOT NULL,
    "VERSION"    NUMBER(*,0),
    "NAME" VARCHAR2(20 BYTE),
    "BUID" VARCHAR2(20 BYTE),
    "UNIT" VARCHAR2(20 BYTE),
    "MATERIAL_GROUP" VARCHAR2(20 BYTE),
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 BYTE),
    "CREATED_BY" VARCHAR2(16 BYTE),
    CONSTRAINT "MATERIAL_PK" PRIMARY KEY ("ID") USING INDEX,
    CONSTRAINT "MATERIAL_BUID" UNIQUE ("BUID"),
    CONSTRAINT "MATERIAL_UNIT_FK" FOREIGN KEY ("UNIT") REFERENCES UNIT ("BUID") DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT "MATERIAL_MATERIAL_GROUP_FK" FOREIGN KEY ("MATERIAL_GROUP") REFERENCES MATERIAL_GROUP ("BUID") DEFERRABLE INITIALLY DEFERRED
  );
CREATE SEQUENCE "MATERIAL_SEQ" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;

CREATE TABLE "MATERIAL_GROUP"
  (
    "ID"   NUMBER NOT NULL,
    "VERSION"    NUMBER(*,0),
    "NAME" VARCHAR2(20 BYTE),
    "BUID" VARCHAR2(20 BYTE),
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 BYTE),
    "CREATED_BY" VARCHAR2(16 BYTE),
    CONSTRAINT "MATERIAL_GROUP_PK" PRIMARY KEY ("ID") USING INDEX,
    CONSTRAINT "MATERIAL_GROUP_BUID" UNIQUE ("BUID")
  );
CREATE SEQUENCE "MATERIAL_GROUP_SEQ" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;

CREATE TABLE "UNIT"
  (
    "ID"   NUMBER NOT NULL,
    "VERSION"    NUMBER(*,0),
    "NAME" VARCHAR2(20 BYTE),
    "BUID" VARCHAR2(20 BYTE),
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 BYTE),
    "CREATED_BY" VARCHAR2(16 BYTE),
    CONSTRAINT "UNIT_PK" PRIMARY KEY ("ID") USING INDEX,
    CONSTRAINT "UNIT_BUID" UNIQUE ("BUID")
  );
CREATE SEQUENCE "UNIT_SEQ" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;

CREATE TABLE "SELF_REFERENCING_ENTITY"
  (
    "ID" NUMBER NOT NULL,
    "VERSION"    NUMBER(*,0),
    "NAME" VARCHAR2(20 BYTE),
    "VALUES" CHAR_ARRAY,
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 BYTE),
    "CREATED_BY" VARCHAR2(16 BYTE),
    "RELATION1" VARCHAR2(20 BYTE),
    "RELATION2" VARCHAR2(20 BYTE),
    CONSTRAINT "SELF_REFERENCING_ENTITY_PK" PRIMARY KEY ("ID") USING INDEX,
    CONSTRAINT "SELF_REFERENCING_ENTITY_NAME" UNIQUE ("NAME"),
    CONSTRAINT "LINK_SELF_REF_R1_SELF_REF_NAME" FOREIGN KEY ("RELATION1") REFERENCES SELF_REFERENCING_ENTITY ("NAME") DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT "LINK_SELF_REF_R2_SELF_REF_NAME" FOREIGN KEY ("RELATION2") REFERENCES SELF_REFERENCING_ENTITY ("NAME") DEFERRABLE INITIALLY DEFERRED
  );
CREATE SEQUENCE "SELF_REFERENCING_ENTITY_SEQ" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;

CREATE TABLE "ONE_TO_MANY_ENTITY"
  (
    "ID" NUMBER NOT NULL,
    "VERSION"    NUMBER(*,0),
    "NAME" VARCHAR2(20 BYTE),
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 BYTE),
    "CREATED_BY" VARCHAR2(16 BYTE),
    --"RELATION1" VARCHAR2(20 BYTE),
    --"RELATION2" VARCHAR2(20 BYTE),
    CONSTRAINT "ONE_TO_MANY_ENTITY_PK" PRIMARY KEY ("ID") USING INDEX,
    CONSTRAINT "ONE_TO_MANY_ENTITY_NAME" UNIQUE ("NAME")
    --CONSTRAINT "LINK_ONE_TO_MANY_ENTITY_R1_SELF_REF_NAME" FOREIGN KEY ("RELATION1") REFERENCES ONE_TO_MANY_ENTITY ("NAME") DEFERRABLE INITIALLY IMMEDIATE,
    --CONSTRAINT "LINK_ONE_TO_MANY_ENTITY_R2_SELF_REF_NAME" FOREIGN KEY ("RELATION2") REFERENCES SELF_REFERENCING_ENTITY ("NAME") DEFERRABLE INITIALLY IMMEDIATE
  );
  
CREATE TABLE "LINK_ONE_TO_MANY_ENTITY_1"
  (
    "NAME1" VARCHAR2(20 BYTE),
    "NAME2" VARCHAR2(20 BYTE),
    CONSTRAINT "LINK_ONE_TO_MANY_ENTITY_1_FROM" FOREIGN KEY ("NAME1") REFERENCES ONE_TO_MANY_ENTITY ("NAME") DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT "LINK_ONE_TO_MANY_ENTITY_1_TO" FOREIGN KEY ("NAME2") REFERENCES ONE_TO_MANY_ENTITY ("NAME") DEFERRABLE INITIALLY DEFERRED
  );
  
CREATE TABLE "LINK_ONE_TO_MANY_ENTITY_2"
  (
    "NAME_ONE_TO_MANY" VARCHAR2(20 BYTE),
    "NAME_SELF_REFERENCING_ENTITY" VARCHAR2(20 BYTE),
    CONSTRAINT "LINK_ONE_TO_MANY_ENTITY_2_FROM" FOREIGN KEY ("NAME_ONE_TO_MANY") REFERENCES ONE_TO_MANY_ENTITY ("NAME") DEFERRABLE INITIALLY DEFERRED,
    CONSTRAINT "LINK_ONE_TO_MANY_ENTITY_2__TO" FOREIGN KEY ("NAME_SELF_REFERENCING_ENTITY") REFERENCES SELF_REFERENCING_ENTITY ("NAME") DEFERRABLE INITIALLY DEFERRED
  );
  
CREATE SEQUENCE "ONE_TO_MANY_ENTITY_SEQ" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;

