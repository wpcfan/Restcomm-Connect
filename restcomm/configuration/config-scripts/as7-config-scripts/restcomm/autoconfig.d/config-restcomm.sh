#!/bin/bash
##
## Description: Configures RestComm
## Author: Henrique Rosa (henrique.rosa@telestax.com)
## Author: Pavel Slegr (pavel.slegr@telestax.com)
## Author: Charles Roufay (@telestax.com)

# VARIABLES
RESTCOMM_BIN=$RESTCOMM_HOME/bin
RESTCOMM_DARS=$RESTCOMM_HOME/standalone/configuration/dars
RESTCOMM_DEPLOY=$RESTCOMM_HOME/standalone/deployments/restcomm.war
XPATHLIST=../xpath-list.txt
jarFile=$RESTCOMM_HOME/bin/restcomm/auto-config.jar


## FUNCTIONS

## Description: Configures Java Options for Application Server
## Parameters : none
configJavaOpts() {
	FILE=$RESTCOMM_BIN/standalone.conf

	# Find total available memory on the instance
    TOTAL_MEM=$(free -m -t | grep 'Total:' | awk '{print $2}')
    # get 70 percent of available memory
    # need to use division by 1 for scale to be read
    CHUNK_MEM=$(echo "scale=0; ($TOTAL_MEM * 0.7)/1" | bc -l)
    # divide chunk memory into units of 64mb
    MULTIPLIER=$(echo "scale=0; $CHUNK_MEM/64" | bc -l)
    # use multiples of 64mb to know effective memory
    FINAL_MEM=$(echo "$MULTIPLIER * 64" | bc -l)
    MEM_UNIT='m'

    RESTCOMM_OPTS="-Xms$FINAL_MEM$MEM_UNIT -Xmx$FINAL_MEM$MEM_UNIT -XX:MaxPermSize=256m -Dorg.jboss.resolver.warning=true -Dsun.rmi.dgc.client.gcInterval=3600000 -Dsun.rmi.dgc.server.gcInterval=3600000"

	sed -e "/if \[ \"x\$JAVA_OPTS\" = \"x\" \]; then/ {
		N; s|JAVA_OPTS=.*|JAVA_OPTS=\"$RESTCOMM_OPTS\"|
	}" $FILE > $FILE.bak
	mv $FILE.bak $FILE
	echo "Configured JVM for RestComm: $RESTCOMM_OPTS"
}

## Description: Updates RestComm configuration file

configRestcomm() {
	FILE=$RESTCOMM_DEPLOY/WEB-INF/conf/restcomm.xml


	if [ "$ACTIVE_PROXY" == "true" ] || [ "$ACTIVE_PROXY" == "TRUE" ]; then

		echo "//restcomm/runtime-settings/external-ip==$PRIVATE_IP" $FILE
		echo "//restcomm/runtime-settings/outbound-proxy/outbound-proxy-user==$OUTBOUND_PROXY_USERNAME" $FILE >> $XPATHLIST
		echo "//restcomm/runtime-settings/outbound-proxy/outbound-proxy-password==$OUTBOUND_PROXY_PASSWORD" $FILE >> $XPATHLIST
		echo "//restcomm/runtime-settings/outbound-proxy/outbound-proxy-uri==$OUTBOUND_PROXY_URI" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/@class==org.mobicents.servlet.restcomm.mgcp.MediaGateway" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/@name==Mobicents Media Server" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/local-address==$PRIVATE_IP" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/local-port==2727" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/remote-address==$PRIVATE_IP" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/response-timeout==500" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/external-address/null==$PRIVATE_IP" $FILE >> $XPATHLIST


	echo 'Updated Active Proxy configuration'

	else
		if [ -n "$static_address" ]; then

		echo "//restcomm/runtime-settings/external-ip==$STATIC_ADDRESS" $FILE >> $XPATHLIST
		echo "//restcomm/runtime-settings/outbound-proxy/outbound-proxy-user==$OUTBOUND_PROXY_USERNAME" $FILE >> $XPATHLIST
		echo "//restcomm/runtime-settings/outbound-proxy/outbound-proxy-password==$OUTBOUND_PROXY_PASSWORD" $FILE >> $XPATHLIST
		echo "//restcomm/runtime-settings/outbound-proxy/outbound-proxy-uri==$OUTBOUND_PROXY_URI" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/@class==org.mobicents.servlet.restcomm.mgcp.MediaGateway" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/@name==Mobicents Media Server" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/local-address==$PRIVATE_IP" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/local-port==2727" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/remote-address==$PRIVATE_IP" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/response-timeout==500" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/external-address/null==$STATIC_ADDRESS" $FILE >> $XPATHLIST


		else
		echo "//restcomm/runtime-settings/external-ip==$STATIC_ADDRESS" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/local-address==$PRIVATE_IP" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/remote-address==$PRIVATE_IP" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/external-address/null==$STATIC_ADDRESS" $FILE >> $XPATHLIST
		echo "//restcomm/runtime-settings/outbound-proxy/outbound-proxy-user==$OUTBOUND_PROXY_USERNAME" $FILE >> $XPATHLIST
		echo "//restcomm/runtime-settings/outbound-proxy/outbound-proxy-password==$OUTBOUND_PROXY_PASSWORD" $FILE >> $XPATHLIST
		echo "//restcomm/runtime-settings/outbound-proxy/outbound-proxy-uri==$OUTBOUND_PROXY_URI" $FILE >> $XPATHLIST

		fi
	fi



	if [ "$SSL_MODE" == "strict" ] || [ "$SSL_MODE" == "STRICT" ]; then

		echo "//restcomm/http-client/ssl-mode==$SSL_MODE" $FILE >> $XPATHLIST

	echo 'Updated SSL mode : $SSL_MODE'
	else
		#the if could be remove and use the SSL_MODE specified in the restcomm.conf
		echo "//restcomm/http-client/ssl-mode==allowall" $FILE >> $XPATHLIST
	fi

	java -jar $jarFile $XPATHLIST
	#empty content of XPATHLIST
	echo "" > $XPATHLIST
}

## Description: Configures Voip Innovations Credentials

configVoipInnovations() {
	FILE=$RESTCOMM_DEPLOY/WEB-INF/conf/restcomm.xml
	jarFile=$RESTCOMM_HOME/bin/restcomm/auto-config.jar

		echo "//restcomm/phone-number-provisioning/@class==org.mobicents.servlet.restcomm.provisioning.number.vi.VoIPInnovationsNumberProvisioningManager" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/voip-innovations/login==$VI_LOGIN" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/voip-innovations/passoword==$VI_PASSWORD" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/voip-innovations/endpoint==$VI_ENDPOINT" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/voip-innovations/uri==https://backoffice.voipinnovations.com/api2.pl" $FILE >> $XPATHLIST

	java -jar $jarFile $XPATHLIST
	#empty content of XPATHLIST
	echo "" > $XPATHLIST
	echo 'Configured Voip Innovation credentials'
}

configDidProvisionManager() {
	FILE=$RESTCOMM_DEPLOY/WEB-INF/conf/restcomm.xml
	jarFile=$RESTCOMM_HOME/bin/restcomm/auto-config.jar

		if [[ "$PROVISION_PROVIDER" == "VI" || "$PROVISION_PROVIDER" == "vi" ]]; then

		echo "//restcomm/phone-number-provisioning/@class==org.mobicents.servlet.restcomm.provisioning.number.vi.VoIPInnovationsNumberProvisioningManager" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/voip-innovations/login==$DID_LOGIN" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/voip-innovations/passoword==$DID_PASSWORD" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/voip-innovations/endpoint==$DID_ENDPOINT" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/voip-innovations/uri==https://backoffice.voipinnovations.com/api2.pl" $FILE >> $XPATHLIST
		echo "//restcomm/runtime-settings/outbound-proxy/outboudproxy-user-at-from-header==false" $FILE >> $XPATHLIST

		echo 'Configured Voip Innovation credentials'
		else
			if [[ "$PROVISION_PROVIDER" == "BW" || "$PROVISION_PROVIDER" == "bw" ]]; then
		echo "BANDWIDTH PROVISION_PROVIDER"
		echo "//restcomm/phone-number-provisioning/@class==org.mobicents.servlet.restcomm.provisioning.number.bandwidth.BandwidthNumberProvisioningManager" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/bandwidth/username==$DID_LOGIN" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/bandwidth/password==$DID_PASSWORD" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/bandwidth/accountId==$DID_ACCOUNT_ID" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/bandwidth/siteId==$DID_SITEID" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/bandwidth/uri==https://api.inetwork.com/v1.0" $FILE >> $XPATHLIST
		echo "//restcomm/runtime-settings/outbound-proxy/outboudproxy-user-at-from-header==false" $FILE >> $XPATHLIST

			echo 'Configured Bandwidth credentials'
		else
			if [[ "$PROVISION_PROVIDER" == "NX" || "$PROVISION_PROVIDER" == "nx" ]]; then
				echo "Nexmo PROVISION_PROVIDER"
		echo "//restcomm/phone-number-provisioning/@class==org.mobicents.servlet.restcomm.provisioning.number.nexmo.NexmoPhoneNumberProvisioningManager" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/nexmo/api-key==$DID_LOGIN" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/nexmo/api-secret==$DID_PASSWORD" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/nexmo/uri==https://rest.nexmo.com/" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/nexmo/smpp-system-type==$SMPP_SYSTEM_TYPE" $FILE >> $XPATHLIST
		echo "//restcomm/runtime-settings/outbound-proxy/outboudproxy-user-at-from-header==true" $FILE >> $XPATHLIST
		#callback status
		echo "//restcomm/phone-number-provisioning/callback-urls/voice/@method==SIP" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/callback-urls/voice/@url==$STATIC_ADDRESS:5080" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/callback-urls/sms/@method==null" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/callback-urls/sms/@url==null" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/callback-urls/fax/@method==null" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/callback-urls/fax/@url==null" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/callback-urls/ussd/@method==null" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/callback-urls/ussd/@url==null" $FILE >> $XPATHLIST

			echo 'Configured NEXMO credentials'
		else
			if [[ "$PROVISION_PROVIDER" == "VB" || "$PROVISION_PROVIDER" == "vb" ]]; then
				echo "Voxbone PROVISION_PROVIDER"

		echo "//restcomm/phone-number-provisioning/@class==org.mobicents.servlet.restcomm.provisioning.number.voxbone.VoxbonePhoneNumberProvisioningManager" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/voxbone/username==$DID_LOGIN" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/voxbone/password==$DID_PASSWORD" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/voxbone/uri==https://api.voxbone.com/ws-voxbone/services/rest" $FILE >> $XPATHLIST
		echo "//restcomm/runtime-settings/outbound-proxy/outboudproxy-user-at-from-header==false" $FILE >> $XPATHLIST
		#callback status
		echo "//restcomm/phone-number-provisioning/callback-urls/voice/@method==SIP" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/callback-urls/voice/@url=={E164}@$STATIC_ADDRESS:5080" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/callback-urls/sms/@method==SIP" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/callback-urls/sms/@url=={E164}@$STATIC_ADDRESS:5080" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/callback-urls/fax/@method==SIP" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/callback-urls/fax/@url=={E164}@$STATIC_ADDRESS:5080" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/callback-urls/ussd/@method==SIP" $FILE >> $XPATHLIST
		echo "//restcomm/phone-number-provisioning/callback-urls/ussd/@url=={E164}@$STATIC_ADDRESS:5080" $FILE >> $XPATHLIST
			echo 'Configured VOXBONE credentials'

		fi
		fi
		fi
		fi

	java -jar $jarFile $XPATHLIST
	#empty content of XPATHLIST
	echo "" > $XPATHLIST
}

## Description: Configures Fax Service Credentials

configFaxService() {
	FILE=$RESTCOMM_DEPLOY/WEB-INF/conf/restcomm.xml
	jarFile=$RESTCOMM_HOME/bin/restcomm/auto-config.jar

		echo "//restcomm/fax-service/@class==org.mobicents.servlet.restcomm.fax.InterfaxService" $FILE >> $XPATHLIST
		echo "//restcomm/fax-service/user==$INTERFAX_USER" $FILE >> $XPATHLIST
		echo "//restcomm/fax-service/password==$INTERFAX_PASSWORD" $FILE >> $XPATHLIST

	java -jar $jarFile $XPATHLIST
	#empty content of XPATHLIST
	echo "" > $XPATHLIST
	echo 'Configured Fax Service credentials'
}

## Description: Configures Sms Aggregator
##
configSmsAggregator() {
	FILE=$RESTCOMM_DEPLOY/WEB-INF/conf/restcomm.xml
	jarFile=$RESTCOMM_HOME/bin/restcomm/auto-config.jar

		echo "//restcomm/sms-aggregator/@class==org.mobicents.servlet.restcomm.sms.SmsService" $FILE >> $XPATHLIST
		echo "//restcomm/sms-aggregator/outbound-prefix==$SMS_PREFIX" $FILE >> $XPATHLIST
		echo "//restcomm/sms-aggregator/outbound-endpoint==$SMS_OUTBOUND_PROXY" $FILE >> $XPATHLIST

	java -jar $jarFile $XPATHLIST
	#empty content of XPATHLIST
	echo "" > $XPATHLIST
	echo "Configured Sms Aggregator using OUTBOUND PROXY "
}

## Description: Configures Speech Recognizer
configSpeechRecognizer() {
	FILE=$RESTCOMM_DEPLOY/WEB-INF/conf/restcomm.xml


	sed -e "/<speech-recognizer.*>/ {
		N; s|<api-key.*></api-key>|<api-key production=\"true\">$1</api-key>|
	}" $FILE > $FILE.bak

	mv $FILE.bak $FILE
	echo 'Configured the Speech Recognizer'
}

## Description: Configures available speech synthesizers
## Parameters : none
configSpeechSynthesizers() {
	configAcapela $ACAPELA_APPLICATION $ACAPELA_LOGIN $ACAPELA_PASSWORD
	configVoiceRSS $VOICERSS_KEY
}

## Description: Configures Acapela Speech Synthesizer
## Parameters : 1.Application Code
## 				2.Login
## 				3.Password
configAcapela() {
	FILE=$RESTCOMM_DEPLOY/WEB-INF/conf/restcomm.xml
	jarFile=$RESTCOMM_HOME/bin/restcomm/auto-config.jar

		#echo "//restcomm/speech-recognizer/@class==org.mobicents.servlet.restcomm.asr.ISpeechAsr" $FILE >> $XPATHLIST
		#echo "//restcomm/speech-synthesizer/api-key/@production==true" $FILE >> $XPATHLIST

	java -jar $jarFile $XPATHLIST
	#empty content of XPATHLIST
	echo "" > $XPATHLIST
	echo 'Configured Acapela Speech Synthesizer'
}

## Description: Configures VoiceRSS Speech Synthesizer

configVoiceRSS() {
	FILE=$RESTCOMM_DEPLOY/WEB-INF/conf/restcomm.xml
	jarFile=$RESTCOMM_HOME/bin/restcomm/auto-config.jar

		echo "//restcomm/speech-synthesizer/@class==org.mobicents.servlet.restcomm.tts.VoiceRSSSpeechSynthesizer" $FILE >> $XPATHLIST
		echo "//restcomm/speech-synthesizer/service-root==http://api.voicerss.org" $FILE >> $XPATHLIST
		echo "//restcomm/speech-synthesizer/apikey==$VOICERSS_KEY" $FILE >> $XPATHLIST

	java -jar $jarFile $XPATHLIST
	#empty content of XPATHLIST
	echo "" > $XPATHLIST
	echo 'Configured VoiceRSS Speech Synthesizer'
}

## Description: Updates Mobicents properties for RestComm

configMobicentsProperties() {
	FILE=$RESTCOMM_DARS/mobicents-dar.properties
	sed -e 's|^ALL=.*|ALL=("RestComm", "DAR\:From", "NEUTRAL", "", "NO_ROUTE", "0")|' $FILE > $FILE.bak
	mv $FILE.bak $FILE
	echo "Updated mobicents-dar properties"
}

## Description: Configures TeleStax Proxy

configTelestaxProxy() {
## This function is deprecated

	FILE=$RESTCOMM_DEPLOY/WEB-INF/conf/restcomm.xml
	jarFile=$RESTCOMM_HOME/bin/restcomm/auto-config.jar

	#if [ "$enabled" == "true" ] || [ "$enabled" == "TRUE" ]; then
		#echo "//restcomm/telestax-proxy/enabled==ACTIVE_PROXY" $FILE >> $XPATHLIST
		#echo "//restcomm/telestax-proxy/login==TP_LOGIN" $FILE >> $XPATHLIST
		#echo "//restcomm/telestax-proxy/password==TP_PASSWORD" $FILE >> $XPATHLIST
		#echo "//restcomm/telestax-proxy/endpoint==INSTANCE_ID" $FILE >> $XPATHLIST
		#echo "//restcomm/telestax-proxy/siteId==SITE_ID" $FILE >> $XPATHLIST
		#echo "//restcomm/telestax-proxy/uri==PROXY_IP" $FILE >> $XPATHLIST
		#echo "//restcomm/telestax-proxy/uri==PROXY_PRIVATE_IP" $FILE >> $XPATHLIST
		#echo 'Enabled TeleStax Proxy'
	#else

		#echo "//restcomm/telestax-proxy/enabled==ACTIVE_PROXY" $FILE >> $XPATHLIST
		#echo "//restcomm/telestax-proxy/login==TP_LOGIN" $FILE >> $XPATHLIST
		#echo "//restcomm/telestax-proxy/password==TP_PASSWORD" $FILE >> $XPATHLIST
		#echo "//restcomm/telestax-proxy/endpoint==INSTANCE_ID" $FILE >> $XPATHLIST
		#echo "//restcomm/telestax-proxy/siteId==SITE_ID" $FILE >> $XPATHLIST
		#echo "//restcomm/telestax-proxy/uri==PROXY_IP" $FILE >> $XPATHLIST
		#echo "//restcomm/telestax-proxy/uri==http://127.0.0.1:2080" $FILE >> $XPATHLIST
		#echo 'Enabled TeleStax Proxy'
		#echo 'Disabled TeleStax Proxy'
	#fi
	java -jar $jarFile $XPATHLIST
	#empty content of XPATHLIST
	echo "" > $XPATHLIST
	echo "Configured : TeleStax Proxy"
}


## Description: Configures Media Server Manager


configMediaServerManager() {
	FILE=$RESTCOMM_DEPLOY/WEB-INF/conf/restcomm.xml
	jarFile=$RESTCOMM_HOME/bin/restcomm/auto-config.jar

	if [ "$enabled" == "true" ] || [ "$enabled" == "TRUE" ]; then
		echo "//restcomm/media-server-manager/mgcp-server/@class==org.mobicents.servlet.restcomm.mgcp.MediaGateway" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/@name==Mobicents Media Server" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/local-address==$PRIVATE_IP" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/local-port==2727" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/remote-address==$PRIVATE_IP" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/response-timeout==500" $FILE >> $XPATHLIST
		echo "//restcomm/media-server-manager/mgcp-server/external-address/null==$STATIC_ADDRESS" $FILE >> $XPATHLIST
		echo 'Configured Media Server Manager'
	fi

	java -jar $jarFile $XPATHLIST
	#empty content of XPATHLIST
	echo "" > $XPATHLIST
}

## Description: Configures SMPP Account Details


configSMPPAccount() {
	FILE=$RESTCOMM_DEPLOY/WEB-INF/conf/restcomm.xml

	if [ "$activate" == "true" ] || [ "$activate" == "TRUE" ]; then
		echo "//restcomm/smpp/@activateSmppConnection==$SMPP_ACTIVATE" $FILE >> $XPATHLIST
		echo "//restcomm/smpp/connections/connection['1']/systemid==$SMPP_SYSTEM_ID" $FILE >> $XPATHLIST
		echo "//restcomm/smpp/connections/connection['1']/peerip==$SMPP_PEER_IP" $FILE >> $XPATHLIST
		echo "//restcomm/smpp/connections/connection['1']/peerpoort==$SMPP_PEER_PORT" $FILE >> $XPATHLIST
		echo "//restcomm/smpp/connections/connection['1']/password==$SMPP_PASSWORD" $FILE >> $XPATHLIST
		echo "//restcomm/smpp/connections/connection['1']/systemtype==$SMPP_SYSTEM_TYPE" $FILE >> $XPATHLIST
		echo 'Configured SMPP Account Details'

	else
		echo "//restcomm/smpp/@activateSmppConnection==$SMPP_ACTIVATE" $FILE >> $XPATHLIST
		echo 'Configured SMPP Account Details Deactivated'


	fi
	java -jar $jarFile $XPATHLIST
	#empty content of XPATHLIST
	echo "" > $XPATHLIST
}
configMediaServerMSaddress() {
	FILE=$RESTCOMM_DEPLOY/WEB-INF/conf/restcomm.xml

	if [ -n "$MS_ADDRESS" ]; then
		echo "//restcomm/media-server-manager/mgcp-server/remote-address==$MS_ADDRESS" $FILE >> $XPATHLIST
		echo "Updated MSaddress"
	fi

	java -jar $jarFile $XPATHLIST
	#empty content of XPATHLIST
	echo "" > $XPATHLIST
}



# MAIN
echo 'Configuring RestComm...'
#configJavaOpts
configMobicentsProperties
configRestcomm 
#configVoipInnovations 
configDidProvisionManager 
configFaxService 
configSmsAggregator 
configSpeechRecognizer 
configSpeechSynthesizers
#configTelestaxProxy 
configMediaServerManager 
configSMPPAccount 
configMediaServerMSaddress 
echo 'Configured RestComm!'
