-- // Create Changelog
-- Migration SQL that makes the change goes here.
CREATE TABLE changelog (
ID NUMERIC(20,0) NOT NULL,
APPLIED_AT VARCHAR(25) NOT NULL,
DESCRIPTION VARCHAR(255) NOT NULL,
CONSTRAINT PK_restcomm_changelog
PRIMARY KEY (id));

-- //@UNDO

DROP TABLE changelog;