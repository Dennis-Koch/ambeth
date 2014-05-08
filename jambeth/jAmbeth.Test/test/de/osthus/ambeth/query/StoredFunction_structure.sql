CREATE OR REPLACE FUNCTION getDoubled(value NUMBER) RETURN NUMBER IS result NUMBER;
BEGIN
	result := value * 2;
	return (result);
END;

CREATE OR REPLACE FUNCTION multiParams(colName VARCHAR2, hash VARCHAR2, value NUMBER) RETURN NUMBER IS result NUMBER;
BEGIN
	result := value * 2;
	return (result);
END;

CREATE TABLE "QUERY_ENTITY"
  (
    "ID"   NUMBER NOT NULL,
    "FK"   NUMBER NULL,
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 BYTE),
    "CREATED_BY" VARCHAR2(16 BYTE),
    "VERSION"    NUMBER(*,0),
    CONSTRAINT "PK_QUERY_ENTITY" PRIMARY KEY ("ID") USING INDEX,
    CONSTRAINT "LINK_QE_JQE_FK1" FOREIGN KEY ("FK") REFERENCES "JOIN_QUERY_ENTITY" ("ID") DEFERRABLE INITIALLY IMMEDIATE
);
CREATE SEQUENCE "QUERY_ENTITY_SEQ" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE;

CREATE TABLE "JOIN_QUERY_ENTITY"
  (
    "ID"   NUMBER NOT NULL,
    "PARENT"   NUMBER NULL,
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 BYTE),
    "CREATED_BY" VARCHAR2(16 BYTE),
    "VERSION"    NUMBER(*,0),
    CONSTRAINT "PK_JOIN_QUERY_ENTITY" PRIMARY KEY ("ID") USING INDEX,
    CONSTRAINT "LINK_JQE_JQE_FK1" FOREIGN KEY ("PARENT") REFERENCES "JOIN_QUERY_ENTITY" ("ID") DEFERRABLE INITIALLY IMMEDIATE
);
CREATE SEQUENCE "JOIN_QUERY_ENTITY_SEQ" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE;

CREATE TABLE "LINK_TABLE_ENTITY"
  (
    "ID"   NUMBER NOT NULL,
    "NAME" VARCHAR2(20 BYTE),
    "UPDATED_ON" DATE,
    "CREATED_ON" DATE,
    "UPDATED_BY" VARCHAR2(16 BYTE),
    "CREATED_BY" VARCHAR2(16 BYTE),
    "VERSION"    NUMBER(*,0),
    CONSTRAINT "PK_LINK_TABLE_ENTITY" PRIMARY KEY ("ID") USING INDEX
);
CREATE SEQUENCE "QUERY_ENTITY_SEQ" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE;

CREATE TABLE "LINK_QE_LTE"
  (
    "LEFT_ID"  NUMBER NOT NULL,
    "RIGHT_ID" NUMBER NOT NULL,
    CONSTRAINT "LINK_QE_LTE_PK" PRIMARY KEY ("LEFT_ID", "RIGHT_ID") USING INDEX,
    CONSTRAINT "LINK_QE_LTE_FK1" FOREIGN KEY ("LEFT_ID") REFERENCES "QUERY_ENTITY" ("ID") DEFERRABLE INITIALLY IMMEDIATE,
    CONSTRAINT "LINK_QE_LTE_FK2" FOREIGN KEY ("RIGHT_ID") REFERENCES "LINK_TABLE_ENTITY" ("ID") DEFERRABLE INITIALLY IMMEDIATE
  );
