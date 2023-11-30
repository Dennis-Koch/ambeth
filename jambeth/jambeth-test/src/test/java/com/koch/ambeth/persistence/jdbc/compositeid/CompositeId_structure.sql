CREATE TABLE COMPOSITE_ID_ENTITY
(
	"ID1"		NUMBER NOT NULL,
	"BUID"		VARCHAR2(50 CHAR) NOT NULL,
	"ALT_ID1"	NUMBER NOT NULL,
	"ALT_ID2"	VARCHAR2(50 CHAR),
	"ALT_ID3"	NUMBER NOT NULL,
	"ALT_ID4"	VARCHAR2(50 CHAR) NOT NULL,
	NAME		VARCHAR2(50 CHAR) NOT NULL,
	CONSTRAINT "NO_VERSION_BACKING_PK" PRIMARY KEY ("ID1","BUID") USING INDEX,
	UNIQUE ("ALT_ID1","ALT_ID2","ALT_ID3","ALT_ID4") USING INDEX
);
CREATE SEQUENCE "COMPOSITE_ID_ENTITY_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;
