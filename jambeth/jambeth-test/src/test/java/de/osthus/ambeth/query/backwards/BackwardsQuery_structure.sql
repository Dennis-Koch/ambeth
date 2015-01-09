CREATE TABLE "QUERY_ENTITY" (
	"ID"				NUMBER(10,0) NOT NULL,
	"NAME"				VARCHAR2(20 CHAR) NOT NULL,
	"NEXT"				NUMBER(10,0) NULL,
	"JOIN_QUERY_ENTITY"	NUMBER(10,0) NULL,
	"LINK_TABLE_ENTITY"	NUMBER(10,0) NULL,
	"UPDATED_ON"		DATE,
	"CREATED_ON"		DATE,
	"UPDATED_BY"		VARCHAR2(16 CHAR),
	"CREATED_BY"		VARCHAR2(16 CHAR),
	"VERSION"			NUMBER(5,0),
	CONSTRAINT "PK_QUERY_ENTITY" PRIMARY KEY ("ID") USING INDEX,
	CONSTRAINT "UK_QUERY_ENTITY_NAME" UNIQUE ("NAME"),
	CONSTRAINT "LINK_QE_QE_FK1" FOREIGN KEY ("NEXT") REFERENCES "QUERY_ENTITY" ("ID") DEFERRABLE INITIALLY IMMEDIATE,
	CONSTRAINT "LINK_QE_JQE_FK1" FOREIGN KEY ("JOIN_QUERY_ENTITY") REFERENCES "JOIN_QUERY_ENTITY" ("ID") DEFERRABLE INITIALLY IMMEDIATE,
	CONSTRAINT "LINK_QE_LTE_FK1" FOREIGN KEY ("LINK_TABLE_ENTITY") REFERENCES "LINK_TABLE_ENTITY" ("ID") DEFERRABLE INITIALLY IMMEDIATE
);
CREATE SEQUENCE "QUERY_ENTITY_SEQ" MINVALUE 1 MAXVALUE 2000000000 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE;

CREATE TABLE "JOIN_QUERY_ENTITY" (
	"ID"			NUMBER(10,0) NOT NULL,
	"VALUE_FIELD"	NUMBER(10,0),
    "PARENT"		NUMBER(10,0),
	"UPDATED_ON"	DATE,
	"CREATED_ON"	DATE,
	"UPDATED_BY"	VARCHAR2(16 CHAR),
	"CREATED_BY"	VARCHAR2(16 CHAR),
	"VERSION"		NUMBER(5,0),
	CONSTRAINT "PK_JOIN_QUERY_ENTITY" PRIMARY KEY ("ID") USING INDEX,
    CONSTRAINT "LINK_JQE_JQE_FK1" FOREIGN KEY ("PARENT") REFERENCES "JOIN_QUERY_ENTITY" ("ID") DEFERRABLE INITIALLY IMMEDIATE
);
CREATE SEQUENCE "JOIN_QUERY_ENTITY_SEQ" MINVALUE 1 MAXVALUE 2000000000 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE;

CREATE TABLE "LINK_TABLE_ENTITY" (
	"ID"			NUMBER(10,0) NOT NULL,
	"NAME"			VARCHAR2(20 BYTE),
	"UPDATED_ON"	DATE,
	"CREATED_ON"	DATE,
	"UPDATED_BY"	VARCHAR2(16 CHAR),
	"CREATED_BY"	VARCHAR2(16 CHAR),
	"VERSION"		NUMBER(5,0),
	CONSTRAINT "PK_LINK_TABLE_ENTITY" PRIMARY KEY ("ID") USING INDEX
);
CREATE SEQUENCE "LINK_TABLE_ENTITY_SEQ" MINVALUE 1 MAXVALUE 2000000000 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE;

CREATE TABLE "LINK_JQE_LTE" (
	"LEFT_ID"	NUMBER(10,0) NOT NULL,
	"RIGHT_ID"	NUMBER(10,0) NOT NULL,
	CONSTRAINT "LINK_JQE_LTE_PK" PRIMARY KEY ("LEFT_ID", "RIGHT_ID") USING INDEX,
	CONSTRAINT "LINK_JQE_LTE_FK1" FOREIGN KEY ("LEFT_ID") REFERENCES "JOIN_QUERY_ENTITY" ("ID") DEFERRABLE INITIALLY IMMEDIATE,
	CONSTRAINT "LINK_JQE_LTE_FK2" FOREIGN KEY ("RIGHT_ID") REFERENCES "LINK_TABLE_ENTITY" ("ID") DEFERRABLE INITIALLY IMMEDIATE
);
