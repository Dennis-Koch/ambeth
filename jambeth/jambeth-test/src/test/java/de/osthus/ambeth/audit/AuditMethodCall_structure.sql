CREATE TABLE "AUDIT_ENTRY"
  (
    "ID"		NUMBER(9,0) NOT NULL,
    "VERSION"	NUMBER(9,0) NOT NULL,
    "METHOD_NAME"	VARCHAR2(100 CHAR) NOT NULL,
	"SPENT_TIME"	NUMBER(12,0) NOT NULL,
    "CREATED_ON" TIMESTAMP,
    "CREATED_BY" VARCHAR2(16 CHAR),
    "UPDATED_ON" TIMESTAMP,
    "UPDATED_BY" VARCHAR2(16 CHAR),
    CONSTRAINT "AUDIT_ENTRY_PK" PRIMARY KEY ("ID") USING INDEX
  );
CREATE SEQUENCE "AUDIT_ENTRY_SEQ" MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;