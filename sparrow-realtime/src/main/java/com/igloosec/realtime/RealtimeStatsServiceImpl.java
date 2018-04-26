package com.igloosec.realtime;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Map;
import java.util.Timer;

import org.apache.commons.lang3.builder.ReflectionToStringBuilder;
import org.apache.felix.ipojo.annotations.Component;
import org.apache.felix.ipojo.annotations.Invalidate;
import org.apache.felix.ipojo.annotations.Provides;
import org.apache.felix.ipojo.annotations.Requires;
import org.apache.felix.ipojo.annotations.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.igloosec.base.PropertyService;
import com.igloosec.jdbc.service.DBHandler;
import com.igloosec.jdbc.service.TableManagerService;
//import com.igloosec.valkyrie.client.ElasticsearchClient;

/*************************************************** 
 * <pre> 
* 업무 그룹명: Fury
* 서브 업무명: 실시간 stats 스케쥴러를 실행
* 설       명: 실시간 stats 스케쥴러를 실행
* 작   성  자: 이선구 [devleesk@igloosec.com]
* 작   성  일: 2018. 4. 27.
* Copyright ⓒIGLOO SEC. All Right Reserved
 * </pre> 
 ***************************************************/ 
@Component(name="realtime_stats")
@Provides
public class RealtimeStatsServiceImpl implements RealtimeStatsService{
	private final Logger logger = LoggerFactory.getLogger(RealtimeStatsService.class);
	
	/** 스케쥴러에서 사용할 디비핸틀 */
	@Requires
	DBHandler dbHandler;
	
	@Requires
	TableManagerService tms;
	
	/** 스케쥴 */
	private Timer timer;
	
	/***************************************************** 
	 * 로컬 테스트를 하기 위한 메소드
	 * @param 
	 * @return void
	 * @exception    
	******************************************************/ 
	public static void main(String[] args) {
		new RealtimeStatsServiceImpl().start();
	}
	/***************************************************** 
	 * osgi에서 @Component 어노테이션을 읽어 실행하는 메소드
	 * @param 
	 * @return void
	 * @exception    
	******************************************************/ 
	@Validate
	private void start() {
		logger.info("RealtimeStatsService start");
		// db 연동
		//   schema 생성 및 test value 입출력
		// 발키리 프로토타입
		//logger.info("prop - ", prop.getProperties());
		/*try {
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			Map<String, Object> m;
			m = mapper.readValue(new File("./config/r.yaml"), Map.class);
			System.out.println(m.get("valkyrie_ip"));
			System.out.println(m.get("valkyrie_port"));
		} catch (JsonParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}*/
		Calendar date = Calendar.getInstance();
		date.set(Calendar.SECOND, 0);
		date.set(Calendar.MILLISECOND, 0);
		
		// 1분단위로 집계
		timer = new Timer("AggregationTask");
		timer.scheduleAtFixedRate(new AggregationTask(this), date.getTime(), 1000 * 60);
	}
	/***************************************************** 
	 * osgi에서 stop 명령에 실행하는 메소드
	 * @param 
	 * @return void
	 * @exception    
	******************************************************/ 
	@Invalidate
	private void stop() {
		logger.info("RealtimeStatsService stop");
		timer.cancel();
		timer = null;
	}
	
	/***************************************************** 
	 * 메소드 설명
	 * @see com.igloosec.realtime.RealtimeStatsService#getDBHandler()
	******************************************************/ 
	public DBHandler getDBHandler() {
		return this.dbHandler;
	}
	
	/***************************************************** 
	 * 메소드 설명
	 * @see com.igloosec.realtime.RealtimeStatsService#getTableManagerService()
	******************************************************/ 
	public TableManagerService getTableManagerService() {
		return this.tms;
	}

}
