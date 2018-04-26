package com.igloosec.realtime;

import java.util.Calendar;
import java.util.Timer;

import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.igloosec.base.PropertyService;
import com.igloosec.jdbc.service.DBHandler;
import com.igloosec.jdbc.service.TableManagerService;
//import com.igloosec.valkyrie.client.ElasticsearchClient;

@Component(name="realtime_stats")
@Provides
public class RealtimeStatsServiceImpl implements RealtimeStatsService{
	private final Logger logger = LoggerFactory.getLogger(RealtimeStatsService.class);
	
	@Requires(optional=true, timeout=5000, nullable=false)
	PropertyService prop;

	@Requires
	DBHandler dbHandler;
	
	@Requires
	TableManagerService tms;
	
	private Timer timer;
	
	/*Local Test*/
	public static void main(String[] args) {
		new RealtimeStatsServiceImpl().start();
	}
	@Validate
	private void start() {
		logger.info("RealtimeStatsService start");
		//logger.info("prop - ", prop.getProperties());

		Calendar date = Calendar.getInstance();
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		
		// 1분단위로 집계
		timer = new Timer("AggregationTask");
		timer.scheduleAtFixedRate(new AggregationTask(this), date.getTime(), 1000 * 60);
	}
	@Invalidate
	private void stop() {
		logger.info("RealtimeStatsService stop");
		timer.cancel();
		timer = null;
	}
	
	public DBHandler getDBHandler() {
		return this.dbHandler;
	}
	
	public TableManagerService getTableManagerService() {
		return this.tms;
	}

}
