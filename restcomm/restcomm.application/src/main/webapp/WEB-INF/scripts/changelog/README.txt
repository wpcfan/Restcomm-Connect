Using the Schema Update 


The changelog directory contains two sub-directories hsqldb and maridb

tree
.
├── hsqldb
│   ├── 20150621023940_changelog.sql
│   └── bootstrap.sql
├── mariadb
│   ├── 20150621023940_changelog.sql
│   └── bootstrap.sql
└── README.txt


The bootstrap.sql files are the initial DB schema snapshot from which further updates will be based

The 20150621023940_changelog.sql files will create the "Changelog" table inside the Restcomm Database

The "Changelog" table will hold the incremental records for each changes to the restcomm database schema


*******HOW TO UPDATE THE SCHEMA (EXAMPLE)***********

You set the DB to use in the restcomm.conf file

For example, if you choose mariadb, you will need to add your the new .sql file isde the directory
WEB-INF/scripts/changelog/mariadb/

The file name must start with the number 20150621023941_

Note that the number increased the initial number 20150621023940 by 1

You must also follow the number with a descriptive name as seen in the example below.

Full file name will be 20150621023941_newTable.sql

example content of the file 20150621023941_newTable.sql is shown below for HSQLDB


-- // Create Changelog
-- Migration SQL that makes the change goes here.
CREATE TABLE IF NOT EXISTS newTable (
ID NUMERIC(20,0) NOT NULL,
APPLIED_AT VARCHAR(25) NOT NULL,
DESCRIPNew VARCHAR(255) NOT NULL,
CONSTRAINT PK_restcomm_newTable 
PRIMARY KEY (id));

-- //@UNDO

DROP TABLE newTable ;


*****example content of the file 20150621023941_newTable.sql is shown below for MARIADB****

-- // Create Changelog
-- Migration SQL that makes the change goes here.
CREATE TABLE IF NOT EXISTS testtable (
ID NUMERIC(20,0) NOT NULL,
APPLIED_AT VARCHAR(25) NOT NULL,
DESCRIPTION VARCHAR(255) NOT NULL,
CONSTRAINT PK_restcomm_testtable
PRIMARY KEY (id));

-- //@UNDO

DROP TABLE testtable;


****example content of the file 20150621023942_AlternewTable.sql is shown below for HSQLDB***

-- // Create Changelog
-- Migration SQL that makes the change goes here.
ALTER TABLE PUBLIC.newTable ADD NEW_COL_TEST VARCHAR(25);

-- //@UNDO





****example content of the file 20150621023942_AltertestTable.sql is shown below for MARIADB***

-- // Create Changelog
-- Migration SQL that makes the change goes here.

ALTER TABLE testtable ADD Column myCol VARCHAR(255) NOT NULL;


-- //@UNDO



The "Changelog" directory will now look like the tree below


.
├── hsqldb
│   ├── 20150621023940_changelog.sql
│   ├── 20150621023941_newTable.sql
│   ├── 20150621023942_AlternewTable.sql
│   └── bootstrap.sql
└── mariadb
    ├── 20150621023940_changelog.sql
    ├── 20150621023941_testTable.sql
    ├── 20150621023942_AltertestTable.sql
    └── bootstrap.sql
    
    
*************CHECK RESULT IN THE MARIADB restcomm DATABASE**************
    
    
    
MariaDB [restcomm]> select * from CHANGELOG;
+----------------+---------------------+----------------+
| ID             | APPLIED_AT          | DESCRIPTION    |
+----------------+---------------------+----------------+
| 20150621023940 | 2015-08-29 21:01:13 | changelog      |
| 20150621023941 | 2015-08-31 08:46:16 | testTable      |
| 20150621023942 | 2015-08-31 09:12:47 | AltertestTable |
+----------------+---------------------+----------------+


*************CHECK RESULT IN THE HSQLDB restcomm.script DATABASE**************



INSERT INTO CHANGELOG VALUES(20150621023940,'2015-08-30 11:36:02','changelog')
INSERT INTO CHANGELOG VALUES(20150621023941,'2015-08-31 10:07:57','newTable')
INSERT INTO CHANGELOG VALUES(20150621023942,'2015-08-31 10:18:30','AlternewTable')



