package org.mobicents.servlet.sip.restcomm.cache;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;

import org.apache.commons.configuration.BaseConfiguration;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 */

public class CacheJanitorTest {

	private static Logger logger = Logger.getLogger(CacheJanitorTest.class);
	private File temp1 ;
	private File temp2 ;
	private File temp3 ;

	private BaseConfiguration conf;
	private CacheJanitor janitor;
	private String retentionPeriod = "1";
	
	@Before
	public void setup() throws IOException{
		temp1 = File.createTempFile("temp", ".wav");
		temp2 = File.createTempFile("temp2", ".wav");
		temp3 = File.createTempFile("temp3", null);
		conf = new BaseConfiguration();
		conf.addProperty("cache-path", temp1.getParentFile().toString());
		conf.addProperty("cache-janitor-interval", "24");
		conf.addProperty("retention-period", retentionPeriod);
		
		
	}
	
	@Test
	public void testCacheJanitor() throws InterruptedException{
		Calendar cal = Calendar.getInstance();  
		cal.add(Calendar.DAY_OF_MONTH, 7 * -1);  
		long purgeTime = cal.getTimeInMillis();
		temp1.setLastModified(purgeTime - 2*86400000);
		temp3.setLastModified(purgeTime - 2*86400000);
		
		janitor = new CacheJanitor(conf);
		janitor.start();
		
		Thread.sleep(10000);
		
		assertTrue(!temp1.exists());
		assertTrue(temp2.exists());
		assertTrue(temp3.exists());
		
	}
}
