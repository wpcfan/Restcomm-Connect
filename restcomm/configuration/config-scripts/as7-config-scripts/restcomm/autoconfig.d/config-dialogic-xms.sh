#!/bin/bash
## Description: Configures Dialogic XMS
## Author     : Henrique Rosa (henrique.rosa@telestax.com)

RESTCOMM_STANDALONE=$RESTCOMM_HOME/standalone
RESTCOMM_DEPLOY=$RESTCOMM_STANDALONE/deployments/restcomm.war

## Description: Elects Dialogic XMS as the active Media Server for RestComm
activateXMS() {
	FILE=$RESTCOMM_DEPLOY/WEB-INF/conf/restcomm.xml
	XPATHLIST=../xpath-list.txt
	jarFile=$RESTCOMM_HOME/bin/restcomm/auto-config.jar

	echo "//restcomm/mscontrol/compatibility==$MS_COMPATIBILITY_MODE" $FILE >> $XPATHLIST
	echo "//restcomm/mscontrol/media-server/@class==com.dialogic.dlg309" $FILE >> $XPATHLIST
	echo "//restcomm/mscontrol/media-server/@name==Dialogic XMS" $FILE >> $XPATHLIST
	echo "//restcomm/mscontrol/media-server/address==$MS_ADDRESS" $FILE >> $XPATHLIST
	echo "//restcomm/mscontrol/media-server/port==5060" $FILE >> $XPATHLIST
	echo "//restcomm/mscontrol/media-server/transport==udp" $FILE >> $XPATHLIST
	echo "//restcomm/mscontrol/media-server/timeout==5" $FILE >> $XPATHLIST

	java -jar $jarFile $XPATHLIST
	#empty content of XPATHLIST
	rm -rf $XPATHLIST

}

#MAIN
echo "Configuring Dialogic XMS...MS_MODE: $MS_COMPATIBILITY_MODE"
activateXMS 
echo '...finished configuring Dialogic XMS!'
