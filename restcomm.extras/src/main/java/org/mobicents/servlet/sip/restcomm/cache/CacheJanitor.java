package org.mobicents.servlet.sip.restcomm.cache;

import java.io.File;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;


/**
 * @author <a href="mailto:gvagenas@gmail.com">George Vagenas</a>
 */

public class CacheJanitor {
	private static final Logger logger = Logger.getLogger(CacheJanitor.class);
//	private Configuration configuration;
	private String location;
//	private final Map<String, ReentrantLock> locks;
	private Long period;
	private final Timer timer;
	private final TimerTask task;
	private Integer retentionPeriod;

	public CacheJanitor(Configuration configuration) {
//		this.configuration = configuration;

		String temp = configuration.getString("cache-path");
		if(!temp.endsWith("/")) {
			temp += "/";
		}
		final File path = new File(temp);
		if(!path.exists() || !path.isDirectory()) {
			throw new IllegalArgumentException(location + " is not a valid cache location.");
		}
		this.location = temp;
//		this.locks = new ConcurrentHashMap<String, ReentrantLock>();
		period = Long.parseLong(configuration.getString("cache-janitor-interval"))*60L*60L*1000L;
		timer = new Timer();
		task = new Janitor();
		retentionPeriod = Integer.parseInt(configuration.getString("retention-period"));
	}

	public void start() {
		logger.info("CacheJanitor starts");
		timer.scheduleAtFixedRate(task, 1000, period);
	}

	public void stop() {
		logger.info("CacheJanitor stops");
		timer.cancel();
	}

	private class Janitor extends TimerTask {

		@Override
		public void run() {
			logger.info("CacheJanitor runs at: "+new Date());
			final File directory = new File(location);
			if(directory.exists()){
				logger.debug(" Directory Exists: "+location);
				final File[] listFiles = directory.listFiles();
				Calendar cal = Calendar.getInstance();  
				cal.add(Calendar.DAY_OF_MONTH, retentionPeriod * -1);  
				long purgeTime = cal.getTimeInMillis(); 
				logger.debug("System.currentTimeMillis " + System.currentTimeMillis());
				logger.debug("purgeTime " + purgeTime);

				for(File listFile : listFiles) {
					logger.debug("Length : "+ listFiles.length);
					logger.debug("listFile.getName() : " +listFile.getName());
					logger.debug("listFile.lastModified() :"+listFile.lastModified());
					if(listFile.getAbsoluteFile().toString().contains("wav") && listFile.lastModified() < purgeTime) {
						if(!listFile.delete()) {
							logger.error("Unable to delete file: " + listFile);
						}
						logger.info("File "+listFile.getAbsolutePath()+" deleted!");
					}
				}
			} else {
				logger.error("Directory "+directory.getAbsolutePath()+" doesn't exist");      
			}
		}
	}
}
