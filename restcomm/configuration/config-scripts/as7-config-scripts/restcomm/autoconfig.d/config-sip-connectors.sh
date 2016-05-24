#!/bin/bash
## Description: Configures SIP connectors
## Author     : Henrique Rosa (henrique.rosa@telestax.com)
## Author     : Pavel Slegr (pavel.slegr@telestax.com)
## Author     : Charles Roufay (pavel.slegr@telestax.com)

## Description: Configures the connectors for RestComm & configures Proxy if enabled

configConnectors() {
	FILE=$RESTCOMM_HOME/standalone/configuration/standalone-sip.xml
	XPATHLIST=../xpath-list.txt
	jarFile=$RESTCOMM_HOME/bin/restcomm/auto-config.jar

	if [ "$ACTIVATE_LB" == "true" ] || [ "$ACTIVATE_LB" == "TRUE" ]; then


	echo "//server/profile/subsystem['25']/connector[1][@name='sip-udp']/@static-server-address==$LB_ADDRESS" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[2][@name='sip-tcp']/@static-server-address==$LB_ADDRESS" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[3][@name='sip-tls']/@static-server-address==$LB_ADDRESS" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[4][@name='sip-ws']/@static-server-address==$LB_ADDRESS" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[5][@name='sip-wss']/@static-server-address==$LB_ADDRESS" $FILE >> $XPATHLIST

	echo "//server/profile/subsystem['25']/connector[1][@name='sip-udp']/@static-server-port==$LB_SIP_PORT_UDP" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[2][@name='sip-tcp']/@static-server-port==$LB_SIP_PORT_TCP" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[3][@name='sip-tls']/@static-server-port==$LB_SIP_PORT_TLS" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[4][@name='sip-ws']/@static-server-port==$LB_SIP_PORT_WS" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[5][@name='sip-wss']/@static-server-port==$LB_SIP_PORT_WSS" $FILE >> $XPATHLIST



	echo 'Configured SIP Connectors and Bindings'

	else

		if [ -n "$STATIC_ADDRESS" ]; then

	echo "//server/profile/subsystem['25']/connector[1][@name='sip-udp']/@static-server-address==$STATIC_ADDRESS" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[2][@name='sip-tcp']/@static-server-address==$STATIC_ADDRESS" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[3][@name='sip-tls']/@static-server-address==$STATIC_ADDRESS" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[4][@name='sip-ws']/@static-server-address==$STATIC_ADDRESS" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[5][@name='sip-wss']/@static-server-address==$STATIC_ADDRESS" $FILE >> $XPATHLIST

	echo "//server/profile/subsystem['25']/connector[1][@name='sip-udp']/@static-server-port==5080" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[2][@name='sip-tcp']/@static-server-port==5080" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[3][@name='sip-tls']/@static-server-port==5081" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[4][@name='sip-ws']/@static-server-port==5082" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[5][@name='sip-wss']/@static-server-port==5083" $FILE >> $XPATHLIST
		else
	echo "//server/profile/subsystem['25']/connector[1][@name='sip-udp']/@static-server-address==''" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[2][@name='sip-tcp']/@static-server-address==''" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[3][@name='sip-tls']/@static-server-address==''" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[4][@name='sip-ws']/@static-server-address==''" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[5][@name='sip-wss']/@static-server-address==''" $FILE >> $XPATHLIST

	echo "//server/profile/subsystem['25']/connector[1][@name='sip-udp']/@static-server-port==5080" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[2][@name='sip-tcp']/@static-server-port==5080" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[3][@name='sip-tls']/@static-server-port==5081" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[4][@name='sip-ws']/@static-server-port==5082" $FILE >> $XPATHLIST
	echo "//server/profile/subsystem['25']/connector[5][@name='sip-wss']/@static-server-port==5083" $FILE >> $XPATHLIST
		fi
	fi

	echo 'Configured SIP Connectors and Bindings'


	#Enable SipServlet statistics

	echo "//server/profile/subsystem['25']/@gather-statistics==true" $FILE >> $XPATHLIST



##
##This section adds
##

	if [[ "$TRUSTSTORE_FILE" == '' ]]; then
		echo "TRUSTSTORE_FILE is not set";
		sed -e "s/<connector name=\"https\" \(.*\)>/<\!--connector name=\"https\" \1>/" \
		 -e "s/<\/connector>/<\/connector-->/" $FILE > $FILE.bak
		 mv $FILE.bak $FILE
		 sed -e "s/<\!--connector name=\"http\" \(.*\)-->/<connector name=\"http\" \1\/>/" $FILE > $FILE.bak
		 mv $FILE.bak $FILE
	else
		if [[ "$TRUSTSTORE_PASSWORD"  == '' ]]; then
			echo "TRUSTSTORE_PASSWORD is not set";
		else
			if [[ "$TRUSTSTORE_ALIAS" == '' ]]; then
				echo "TRUSTSTORE_ALIAS is not set";
			else
				echo "TRUSTORE_FILE is set to '$TRUSTSTORE_FILE' ";
				echo "TRUSTSTORE_PASSWORD is set to '$TRUSTSTORE_PASSWORD' ";
				echo "TRUSTSTORE_ALIAS is set to '$TRUSTSTORE_ALIAS' ";
				echo "Will properly configure HTTPS Connector ";
				if [ "$DISABLE_HTTP" == "true" ] || [ "$DISABLE_HTTP" == "TRUE" ]; then
					echo "DISABLE_HTTP is '$DISABLE_HTTP'. Will disable HTTP Connector"
					sed -e "s/<connector name=\"http\" \(.*\)\/>/<\!--connector name=\"http\" \1-->/" $FILE > $FILE.bak
					mv $FILE.bak $FILE
				else
					sed -e "s/<\!--connector name=\"http\" \(.*\)-->/<connector name=\"http\" \1\/>/" $FILE > $FILE.bak
					mv $FILE.bak $FILE
				fi
				if [[ "$TRUSTSTORE_FILE" = /* ]]; then
					CERTIFICATION_FILE=$TRUSTSTORE_FILE
				else
					CERTIFICATION_FILE="\\\${jboss.server.config.dir}/$TRUSTSTORE_FILE"
				fi
				echo "Will use trust store at location: $CERTIFICATION_FILE"
				sed -e "s/<\!--connector name=\"https\" \(.*\)>/<connector name=\"https\" \1>/" \
				-e "s|<ssl name=\"https\" key-alias=\".*\" password=\".*\" certificate-key-file=\".*\" \(.*\)\/>|<ssl name=\"https\" key-alias=\"$TRUSTSTORE_ALIAS\" password=\"$TRUSTSTORE_PASSWORD\" certificate-key-file=\"$CERTIFICATION_FILE\" cipher-suite=\"TLS_RSA_WITH_3DES_EDE_CBC_SHA,TLS_ECDHE_RSA_WITH_3DES_EDE_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA256,TLS_ECDHE_RSA_WITH_AES_128_CBC_SHA,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,TLS_RSA_WITH_AES_128_CBC_SHA256,TLS_RSA_WITH_AES_128_CBC_SHA,TLS_RSA_WITH_AES_256_CBC_SHA256,TLS_RSA_WITH_AES_256_CBC_SHA\" verify-client=\"false\" \1\/>|" \
				-e "s/<\/connector-->/<\/connector>/" $FILE > $FILE.bak
				mv $FILE.bak $FILE
				echo "Properly configured HTTPS Connector to use trustStore file $CERTIFICATION_FILE"
			fi
		fi
	fi

	java -jar $jarFile $XPATHLIST
	#empty content of XPATHLIST
	rm -rf $XPATHLIST

}

#MAIN
echo 'Configuring Application Server...'
configConnectors "$STATIC_ADDRESS" "$PROXY_IP"
echo 'Finished configuring Application Server!'
