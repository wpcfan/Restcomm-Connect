#!/bin/bash
## Description: Configures the Media Server
## Author: Henrique Rosa (henrique.rosa@telestax.com)

configUdpManager() {
	FILE=$MMS_HOME/deploy/server-beans.xml
	MSERVER_EXTERNAL_ADDRESS="$MEDIASERVER_EXTERNAL_ADDRESS"
	jarFile=$RESTCOMM_HOME/bin/restcomm/auto-config.jar
	XPATHLIST=../xpath-list.txt
	jarFile=$RESTCOMM_HOME/bin/restcomm/auto-config.jar

	if [ "$MSERVER_EXTERNAL_ADDRESS" = "$1" ]; then
   		MSERVER_EXTERNAL_ADDRESS="\<null\/\>"
	fi

	echo "//deployment/bean[4]/property[1]==$PRIVATE_IP" $FILE >> $XPATHLIST
	echo "//deployment/bean[4]/property[2]==$PRIVATE_IP" $FILE >> $XPATHLIST
	echo "//deployment/bean[4]/property[3]==$STATIC_ADDRESS" $FILE >> $XPATHLIST
	echo "//deployment/bean[4]/property[4]==$NETWORK" $FILE >> $XPATHLIST
	echo "//deployment/bean[4]/property[5]==$SUBNET_MASK" $FILE >> $XPATHLIST
	echo "//deployment/bean[4]/property[6]==$MEDIASERVER_USE_SBC" $FILE >> $XPATHLIST
	echo "//deployment/bean[4]/property[7]==$MEDIASERVER_RTP_TIMEOUT" $FILE >> $XPATHLIST
	echo "//deployment/bean[4]/property[8]==$MEDIASERVER_LOWEST_PORT" $FILE >> $XPATHLIST
	echo "//deployment/bean[4]/property[9]==$MEDIASERVER_HIGHEST_PORT" $FILE >> $XPATHLIST

	java -jar $jarFile $XPATHLIST
	#empty content of XPATHLIST
	rm -rf $XPATHLIST
	echo 'Configured UDP Manager'
}

configJavaOptsMem() {
    FILE=$MMS_HOME/bin/run.sh

	# Find total available memory on the instance
    TOTAL_MEM=$(free -m -t | grep 'Total:' | awk '{print $2}')
    # get 20 percent of available memory
    # need to use division by 1 for scale to be read
    CHUNK_MEM=$(echo "scale=0; ($TOTAL_MEM * 0.2)/1" | bc -l)
    # divide chunk memory into units of 64mb
    MULTIPLIER=$(echo "scale=0; $CHUNK_MEM/64" | bc -l)
    # use multiples of 64mb to know effective memory
    FINAL_MEM=$(echo "$MULTIPLIER * 64" | bc -l)
    MEM_UNIT='m'

    MMS_OPTS="-Xms$FINAL_MEM$MEM_UNIT -Xmx$FINAL_MEM$MEM_UNIT -XX:MaxPermSize=256m -Dorg.jboss.resolver.warning=true -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000"

	sed -e "/# Setup MMS specific properties/ {
		N; s|JAVA_OPTS=.*|JAVA_OPTS=\"$MMS_OPTS\"|
	}" $FILE > $FILE.bak
	mv $FILE.bak $FILE
	echo "Updated Java Options for Media Server: $MMS_OPTS"
}

configLogDirectory() {
	FILE=$MMS_HOME/conf/log4j.xml
	DIRECTORY=$MMS_HOME/log

#	java -jar $jarFile "//log4j:configuration/appender[2]/param[1]['@value=']/@value==$DIRECTORY/server.log" $FILE

	sed -e "/<param name=\"File\" value=\".*server.log\" \/>/ s|value=\".*server.log\"|value=\"$DIRECTORY/server.log\"|" \
	    $FILE > $FILE.bak
	mv $FILE.bak $FILE
	echo 'Updated log configuration'
}

## MAIN


if [[ "$TRUSTSTORE_FILE" == '' ]]; then
	echo "TRUSTSTORE_FILE is not set";
	FILE=$MMS_HOME/bin/run.sh
	sed -e "/# Setup MMS specific properties/ {
		N; s|JAVA_OPTS=.*|JAVA_OPTS=\"-Dprogram\.name=\\\$PROGNAME \\\$JAVA_OPTS\"|
	}" $FILE > $FILE.bak
	mv $FILE.bak $FILE
else
	echo "TRUSTSTORE_FILE is set to '$TRUSTSTORE_FILE'";
	if [[ "$TRUSTSTORE_PASSWORD"  == '' ]]; then
		echo "TRUSTSTORE_PASSWORD is not set";
	else
		echo "TRUSTSTORE_PASSWORD is set to '$TRUSTSTORE_PASSWORD' will properly configure MMS";
		FILE=$MMS_HOME/bin/run.sh
		if [[ "$TRUSTSTORE_FILE" = /* ]]; then
			CERTIFICATION_FILE=$TRUSTSTORE_FILE
		else
			CERTIFICATION_FILE=$RESTCOMM_HOME/standalone/configuration/$TRUSTSTORE_FILE
		fi
		JAVA_OPTS_TRUSTORE="-Djavax.net.ssl.trustStore=$CERTIFICATION_FILE -Djavax.net.ssl.trustStorePassword=$TRUSTSTORE_PASSWORD"
		sed -e "/# Setup MMS specific properties/ {
		  N; s|JAVA_OPTS=.*|JAVA_OPTS=\"-Dprogram\.name=\\\$PROGNAME \\\$JAVA_OPTS $JAVA_OPTS_TRUSTORE\"|
		}" $FILE > $FILE.bak
		mv $FILE.bak $FILE
		echo "Properly configured MMS to use trustStore file $RESTCOMM_HOME/standalone/configuration/$TRUSTSTORE_FILE"
	fi
fi

echo "Configuring Mobicents Media Server... MS_ADDRESS $MS_ADDRESS BIND_ADDRESS $BIND_ADDRESS NETWORK $NETWORK SUBNET_MASK $SUBNET_MASK RTP_LOW_PORT $MEDIASERVER_LOWEST_PORT RTP_HIGH_PORT $MEDIASERVER_HIGHEST_PORT"
if [ -z "$MS_ADDRESS" ]; then
		MS_ADDRESS=$BIND_ADDRESS
fi

if [ -z "$MS_NETWORK" ]; then
      MS_NETWORK=$NETWORK
fi

if [ -z "$MS_SUBNET_MASK" ]; then
      MS_SUBNET_MASK=$SUBNET_MASK
fi

configUdpManager $MS_ADDRESS $MS_NETWORK $MS_SUBNET_MASK
#configJavaOpts
configLogDirectory
echo 'Finished configuring Mobicents Media Server!'
