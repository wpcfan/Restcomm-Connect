#! /bin/bash
##
## Description: Configures SIP Load Balancer
## Author     : Henrique Rosa (henrique.rosa@telestax.com)
## Author     : Pavel Slegr (pavel.slegr@telestax.com)
## Author     : Charles Roufay (charles.roufay@telestax.com)
##
## Last update: 22/03/2016
## Change Log: Move away from Telestax Proxy and configure LB from restcomm.conf
## FUNCTIONS
##
##
##
##



configSipStack() {

	lb_sipstack_file="$RESTCOMM_HOME/standalone/configuration/mss-sip-stack.properties"

        if [ "$ACTIVATE_LB" == "true" ] || [ "$ACTIVATE_LB" == "TRUE" ]; then
		sed -e 's|^#org.mobicents.ha.javax.sip.BALANCERS=|org.mobicents.ha.javax.sip.BALANCERS=|' \
		    -e "s|org.mobicents.ha.javax.sip.BALANCERS=.*|org.mobicents.ha.javax.sip.BALANCERS=$LB_ADDRESS:$LB_INTERNAL_PORT|" \
   		    -e 's|^#org.mobicents.ha.javax.sip.REACHABLE_CHECK=|org.mobicents.ha.javax.sip.REACHABLE_CHECK=|' \
		    -e "s|org.mobicents.ha.javax.sip.REACHABLE_CHECK=.*|org.mobicents.ha.javax.sip.REACHABLE_CHECK=false|" \
		    $lb_sipstack_file > $lb_sipstack_file.bak

		echo 'Load Balancer has been activated and mss-sip-stack.properties file updated'
	else
			sed -e 's|^org.mobicents.ha.javax.sip.BALANCERS=|#org.mobicents.ha.javax.sip.BALANCERS=|' \
			    -e 's|^org.mobicents.ha.javax.sip.REACHABLE_CHECK=|#org.mobicents.ha.javax.sip.REACHABLE_CHECK=|' \
				$lb_sipstack_file > $lb_sipstack_file.bak
			echo 'Deactivated Load Balancer on SIP stack configuration file'

	fi
	mv $lb_sipstack_file.bak $lb_sipstack_file


#
# This has been commented out because of issues: http://stackoverflow.com/questions/5198428/saving-to-properties-file-escapes
# We shall be using sed until issue can be resolved
#
#	FILE="$RESTCOMM_HOME/standalone/configuration/mss-sip-stack.properties"
#	XPATHLIST=../xpath-list.txt
#	jarFile=$RESTCOMM_HOME/bin/restcomm/auto-config.jar
#
#        if [ "$ACTIVATE_LB" == "true" ] || [ "$ACTIVATE_LB" == "TRUE" ]; then
#		echo "org.mobicents.ha.javax.sip.BALANCERS==$LB_ADDRESS/:$LB_INTERNAL_PORT" $FILE >> $XPATHLIST
#		echo "org.mobicents.ha.javax.sip.REACHABLE_CHECK==false" $FILE >> $XPATHLIST
#	else
#		echo "org.mobicents.ha.javax.sip.BALANCERS== " $FILE >> $XPATHLIST
#		echo "org.mobicents.ha.javax.sip.REACHABLE_CHECK== " $FILE >> $XPATHLIST
#
#	fi
#
#	java -jar $jarFile $XPATHLIST
#	#empty content of XPATHLIST
#	#echo "" > $XPATHLIST
#	echo 'Load Balancer has been activated and mss-sip-stack.properties file updated'
}


configStandalone() {
	FILE="$RESTCOMM_HOME/standalone/configuration/standalone-sip.xml"
	XPATHLIST=../xpath-list.txt
	jarFile=$RESTCOMM_HOME/bin/restcomm/auto-config.jar

	path_name='org.mobicents.ext'
	if [[ "$RUN_MODE" == *"-lb" ]]; then
		path_name="org.mobicents.ha.balancing.only"
	fi
	echo "//server/profile/subsystem['25']/@path-name==$path_name" $FILE >> $XPATHLIST

	java -jar $jarFile $XPATHLIST
	#empty content of XPATHLIST
	rm -rf $XPATHLIST
}



## MAIN
configSipStack 
configStandalone


