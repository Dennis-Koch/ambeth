

CREATE TABLE "CITY"
  (
    "ID" 				NUMBER(9,0) NOT NULL,
    "UPDATED_ON"		TIMESTAMP,
    "CREATED_ON"		TIMESTAMP,
    "UPDATED_BY"		VARCHAR2(64 CHAR),
    "CREATED_BY"		VARCHAR2(64 CHAR),
    "VERSION"			NUMBER(9,0) NOT NULL,
        "NAME"		VARCHAR2(64 CHAR),
    
    
    CONSTRAINT "CITY_PK" PRIMARY KEY ("ID") USING INDEX 
    
  );
CREATE SEQUENCE "CITY_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;


CREATE TABLE "GUIDEBOOK"
  (
    "ID" 				NUMBER(9,0) NOT NULL,
    "UPDATED_ON"		TIMESTAMP,
    "CREATED_ON"		TIMESTAMP,
    "UPDATED_BY"		VARCHAR2(64 CHAR),
    "CREATED_BY"		VARCHAR2(64 CHAR),
    "VERSION"			NUMBER(9,0) NOT NULL,
      "NAME"		VARCHAR2(64 CHAR),
    "CITY_ID" 				NUMBER(9,0) ,
    
    CONSTRAINT "GUIDEBOOK_PK" PRIMARY KEY ("ID") USING INDEX ,
    CONSTRAINT "GUIDEBOOK_FK_CITY_ID" FOREIGN KEY ("CITY_ID") REFERENCES "CITY" ("ID") DEFERRABLE INITIALLY DEFERRED    
    
  );
CREATE SEQUENCE "GUIDEBOOK_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;
CREATE INDEX "GUIDEBOOK_FK_CITY_ID_IDX" ON "GUIDEBOOK" ("CITY_ID") ;



CREATE TABLE "IMAGE"
  (
    "ID" 				NUMBER(9,0) NOT NULL,
    "UPDATED_ON"		TIMESTAMP,
    "CREATED_ON"		TIMESTAMP,
    "UPDATED_BY"		VARCHAR2(64 CHAR),
    "CREATED_BY"		VARCHAR2(64 CHAR),
    "VERSION"			NUMBER(9,0) NOT NULL,
      "NAME"		VARCHAR2(64 CHAR),
    "GUIDEBOOK_ID" 				NUMBER(9,0) ,
    
    CONSTRAINT "IMAGE_PK" PRIMARY KEY ("ID") USING INDEX ,
    CONSTRAINT "IMAGE_FK_GUIDEBOOK_ID" FOREIGN KEY ("GUIDEBOOK_ID") REFERENCES "GUIDEBOOK" ("ID") DEFERRABLE INITIALLY DEFERRED    
    
  );
CREATE SEQUENCE "IMAGE_SEQ"  MINVALUE 1 MAXVALUE 999999999999999999 INCREMENT BY 1 START WITH 10000 CACHE 20 NOORDER NOCYCLE ;
CREATE INDEX "IMAGE_FK_GUIDEBOOK_ID" ON "IMAGE" ("GUIDEBOOK_ID") ;
