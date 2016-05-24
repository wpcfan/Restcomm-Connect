#! /bin/bash
##
## Description: Configures JBoss AS
## Author     : Henrique Rosa (henrique.rosa@telestax.com)
##Author     : Charles Roufay (@telestax.com)
##

## FUNCTIONS
disableSplashScreen() {
	FILE="$RESTCOMM_HOME/standalone/configuration/standalone-sip.xml"
	XPATHLIST=../xpath-list.txt
	jarFile=$RESTCOMM_HOME/bin/restcomm/auto-config.jar

	echo "//server/profile/subsystem['22']/virtual-server/@enable-welcome-root==false" $FILE >> $XPATHLIST

	java -jar $jarFile $XPATHLIST
	rm -rf $XPATHLIST

}

## MAIN
echo 'Configuring JBoss AS...'
disableSplashScreen
echo '...disabled JBoss splash screen...'
echo 'Finished configuring JBoss AS!'
