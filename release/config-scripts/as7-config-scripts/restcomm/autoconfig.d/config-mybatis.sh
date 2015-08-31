#!/bin/bash
##
## Description: Configures Database for mybatis.xml and Dao for restcomm.xml
## Author: Charles Roufay (charles.roufay@telestax.com)
##

# VARIABLES

RESTCOMM_DEPLOY=$RESTCOMM_HOME/standalone/deployments/restcomm.war

## FUNCTIONS

## Description: Configures Java Options for Application Server
## Parameters : none
configDatabase() {
	FILE=$RESTCOMM_DEPLOY/WEB-INF/conf/mybatis.xml
	active_database="$1"
	active_database_username="$2"
	active_database_password="$3"
	active_database_ip="$4"

	if [ "$ACTIVE_DATABASE" == "mariadb" ]; then

		echo 'Updating MariaDB as Active Database in Mybatis.xml file'

		sed  -e "/<environment id=\"mariadb\">/ {
			N
			N
			N
			N
			s|<property name=\"url\" value=\"jdbc:mariadb://.*:3306/restcomm\"/>|<property name=\"url\" value=\"jdbc:mariadb://$4:3306/restcomm\"\/>|
			N
			s|<property name=\"username\" value=\".*\"/>|<property name=\"username\" value=\"$2\"\/>|
			N
			s|<property name=\"password\" value=\".*\"/>|<property name=\"password\" value=\"$3\"\/>|		
		}" -e "s|<environments default=.*|<environments default=\"$1\">|"  $FILE > $FILE.bak
		mv $FILE.bak $FILE
		echo 'Working on the mybatis file'

	fi


# Use the default HSQLDB binary if Active database is empty or set to production

	if [ "$ACTIVE_DATABASE" == "production" ] || [ "$ACTIVE_DATABASE" == "" ]; then
		echo 'Using the HSQLDB default as Active Database in Mybatis.xml file'
		sed  -e "s|<environments default=.*|<environments default=\"production\">|" $FILE > $FILE.bak
		mv $FILE.bak $FILE
		echo '.... updating mybatis file .......'
	fi


}



## Description: Configures RestComm DAO manager to use MariaDB
## Params: "$ACTIVE_DATABASE"
configDaoManager() {

	FILE=$RESTCOMM_DEPLOY/WEB-INF/conf/restcomm.xml

	if [ "$ACTIVE_DATABASE" == "mariadb" ]; then 
		sed -e "/<dao-manager class=\"org.mobicents.servlet.restcomm.dao.mybatis.MybatisDaoManager\">/ {
			N
			N
			s|<data-files>.*</data-files>|<data-files></data-files>|
			N
			s|<sql-files>.*</sql-files>|<sql-files>\${restcomm:home}/WEB-INF/scripts/mariadb/sql</sql-files>|
		}" $FILE > $FILE.bak
		mv $FILE.bak $FILE
		echo 'Configured myatis Dao Manager for MariaDB'
	fi

	if [ "$ACTIVE_DATABASE" == "production" ] || [ "$ACTIVE_DATABASE" == "" ]; then
		sed -e "/<dao-manager class=\"org.mobicents.servlet.restcomm.dao.mybatis.MybatisDaoManager\">/ {
			N
			N
			s|<data-files>.*</data-files>|<data-files>\${restcomm:home}/WEB-INF/data/hsql</data-files>|
			N
			s|<sql-files>.*</sql-files>|<sql-files>\${restcomm:home}/WEB-INF/sql</sql-files>|
		}" $FILE > $FILE.bak
		mv $FILE.bak $FILE
		echo 'Configured Default HSQLDB for Dao Manager '
	fi
}




# MAIN
echo 'Configuring Restcomm Active Database'
configDatabase "$ACTIVE_DATABASE" "$ACTIVE_DATABASE_USERNAME" "$ACTIVE_DATABASE_PASSWORD" "$ACTIVE_DATABASE_IP"
configDaoManager "$ACTIVE_DATABASE"
echo 'Finished configurting Restcomm Active Database!'

