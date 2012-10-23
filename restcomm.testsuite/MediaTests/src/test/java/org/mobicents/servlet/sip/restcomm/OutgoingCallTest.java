package org.mobicents.servlet.sip.restcomm;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import javax.sip.message.Response;

import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.jboss.arquillian.container.mss.extension.SipStackTool;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mobicents.arquillian.mediaserver.api.EmbeddedMediaserver;
import org.mobicents.arquillian.mediaserver.api.EndpointType;
import org.mobicents.arquillian.mediaserver.api.annotations.GetMediaserver;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.TwilioRestException;
import com.twilio.sdk.resource.factory.CallFactory;
import com.twilio.sdk.resource.instance.Account;
import com.twilio.sdk.resource.instance.Call;

/**
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 * 
 */

public class OutgoingCallTest extends AbstractEndpointTest {

//	private Logger logger = Logger.getLogger(OutgoingCallTest.class);
	public String endpoint = "http://127.0.0.1:8888/restcomm";

	@GetMediaserver
	public EmbeddedMediaserver mediaserver;
	
	private SipStack sipStackA;
	private SipCall sipCallA;
	private SipPhone sipPhoneA;
	private static SipStackTool sipStackToolA;

	@BeforeClass
	public static void beforeClass(){
		sipStackToolA = new SipStackTool("OutgoingCallTestA");
	}

	@Before
	public void setUp() throws Exception
	{
		mediaserver.startServer();
		mediaserver.installEndpoint(EndpointType.IVR, 2);
		mediaserver.installEndpoint(EndpointType.CONFERENCE, 2);
		mediaserver.installEndpoint(EndpointType.PACKETRELAY, 2);
		
		//Create the sipCall and start listening for messages
		sipStackA = sipStackToolA.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", "5060", "127.0.0.1:5070");
		sipPhoneA = sipStackA.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:+14321@127.0.0.1:5060");
		sipCallA = sipPhoneA.createSipCall();
		sipCallA.listenForIncomingCall();
	}
	
	//Remember to change restcomm.xml at restcomm.core/target/ and point to 127.0.0.1:5060
	/*
	 * 	<outbound-proxy-user>8765</outbound-proxy-user>
	 *	<outbound-proxy-password></outbound-proxy-password>
	 *	<outbound-proxy-uri>127.0.0.1:5060</outbound-proxy-uri>
	 */
	@Test
	public void testOutgoing() throws TwilioRestException, ParseException, InterruptedException{

		final TwilioRestClient twilioClient = new TwilioRestClient("ACae6e420f425248d6a26948c17a9e2acf",
				"77f8c12cc7b8f8423e5c38b035249166", endpoint);
		final Account account = twilioClient.getAccount();
		final CallFactory callFactory = account.getCallFactory();
		
		final Map<String, String> parameters = new HashMap<String, String>();
		parameters.put("From", "+18765");
		parameters.put("To", "4321");
		parameters.put("Url", "http://restcomm-demo.appspot.com/app/voice/restcomm3.xml");
//		parameters.put("Url", "http://127.0.0.1:8888/restcomm/demo/hello-world.xml");
		parameters.put("VoiceMethod", "POST");
		
		Call outgoingCall = callFactory.create(parameters);
		
		assertNotNull(outgoingCall);
		
		assertTrue(sipCallA.waitForIncomingCall(5000));
		
		assertTrue(sipCallA.sendIncomingCallResponse(Response.RINGING, "Ringing", 0));
		assertTrue(sipCallA.sendIncomingCallResponse(Response.OK, "OK", 0));

		assertTrue(sipCallA.waitForDisconnect(60000));
		assertTrue(sipCallA.respondToDisconnect());
	}
	
}
