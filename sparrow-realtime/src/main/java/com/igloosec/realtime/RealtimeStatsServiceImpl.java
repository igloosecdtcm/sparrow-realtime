package com.igloosec.realtime;

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

		// db에서 query 정보 가져온다.
		//timer = new Timer("AggregationTask");
		
		// 해당 쿼리 발키리 쿼리로 파싱
		timer = new Timer("ValkyrieParserTask");
		timer.scheduleAtFixedRate(new ValkyrieParserTask(), 1000, 10000);
		
		// 해당 query로 발키리에 집계
		timer = new Timer("AggregationTask");
		timer.scheduleAtFixedRate(new AggregationTask(), 1000, 10000);

		// 집계 결과 데이터 발키리에 저장
		timer = new Timer("ValkyrieClientTask");
		timer.scheduleAtFixedRate(new ValkyrieClientTask(), 1000, 10000);
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
