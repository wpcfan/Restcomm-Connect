/**
 * 
 */
package org.mobicents.servlet.sip.restcomm;

import static org.junit.Assert.assertTrue;

import java.text.ParseException;

import org.apache.log4j.Logger;
import org.cafesip.sipunit.SipCall;
import org.cafesip.sipunit.SipPhone;
import org.cafesip.sipunit.SipStack;
import org.jboss.arquillian.container.mobicents.api.annotations.GetDeployableContainer;
import org.jboss.arquillian.container.mss.extension.ContainerManagerTool;
import org.jboss.arquillian.container.mss.extension.SipStackTool;
import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.twilio.sdk.TwilioRestClient;
import com.twilio.sdk.resource.instance.Account;

/**
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 * 
 */
public class PresenseTest extends AbstractEndpointTest {

	private static Logger logger = Logger.getLogger(PresenseTest.class);

	private static final int TIMEOUT = 10000;
	private static final String endpoint = "http://127.0.0.1:8888/restcomm";
	@ArquillianResource
	private Deployer deployer;

	private SipStack receiver;

	private SipCall sipCall;
	private SipPhone sipPhone;

	@GetDeployableContainer
	private ContainerManagerTool containerManager = null;

	private static SipStackTool sipStackTool;
	private String testArchive = "simple";

	@BeforeClass
	public static void beforeClass(){
		sipStackTool = new SipStackTool("PresenceTest");
	}

	@Before
	public void setUp() throws Exception
	{
		//Create the sipCall and start listening for messages
		receiver = sipStackTool.initializeSipStack(SipStack.PROTOCOL_UDP, "127.0.0.1", "5080", "127.0.0.1:5070");
		sipPhone = receiver.createSipPhone("127.0.0.1", SipStack.PROTOCOL_UDP, 5070, "sip:restcommTester@there.com");
		sipCall = sipPhone.createSipCall();
		sipCall.listenForIncomingCall();
	}

	@After
	public void tearDown() throws Exception
	{
		logger.info("About to un-deploy the application");
		deployer.undeploy(super.archiveName);
		if(sipCall != null)	sipCall.disposeNoBye();
		if(sipPhone != null) sipPhone.dispose();
		if(receiver != null) receiver.dispose();
	}

	@Test
	public void testRegistration() throws ParseException 
	{
		logger.info("About to deploy the application");
		deployer.deploy(super.archiveName);
		
		//Create client with twilio-java-sdk. Endpoint must be http://127.0.0.1:8888/restcomm
		final TwilioRestClient client = new TwilioRestClient("ACae6e420f425248d6a26948c17a9e2acf",
				"77f8c12cc7b8f8423e5c38b035249166", endpoint);
		Account account = client.getAccount();
		
		//Register with sipPhone
		javax.sip.address.SipURI requestURI = receiver.getAddressFactory().createSipURI("sender","127.0.0.1:5070;transport=udp");
		assertTrue(sipPhone.register(requestURI, "no_user", "no_password", "sip:sender@127.0.0.1:5080;transport=udp;lr", TIMEOUT, TIMEOUT));

		assertTrue(true);
	}
	
}
