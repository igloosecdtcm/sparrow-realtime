package com.igloosec.realtime;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.igloosec.realtime.vo.JobInfo;

/*************************************************** 
 * <pre> 
* 업무 그룹명: Fury
* 서브 업무명: 1분단위로 파싱, 집계, 저장하는 메소드
* 설       명: db에서 job을 불러, r 파일로 job을 등록, r 스크립트가 실행되면서 job 정보를 파싱, 집계, 저장한다.
* 작   성  자: 이선구 [devleesk@igloosec.com]
* 작   성  일: 2018. 4. 27.
* Copyright ⓒIGLOO SEC. All Right Reserved
 * </pre> 
 ***************************************************/ 
public class AggregationTask extends TimerTask{
	private final Logger logger =  LoggerFactory.getLogger(AggregationTask.class);
	
	/** job 정보 */
	private List<JobInfo> jobs = new ArrayList<>();
	
	private RealtimeStatsService rss;
	
	public AggregationTask(RealtimeStatsServiceImpl rss) {
		logger.info("AggregationTask ist");
		this.rss = rss;
	}
	/***************************************************** 
	 * TimerTask 스케쥴러가 실행되면, run 메소드를 실행
	 * @see java.util.TimerTask#run()
	******************************************************/ 
	@Override
	public void run() { 
		logger.info("AggregationTask run start");
		getJobs();
	}
	/***************************************************** 
	 * jobs 리스트 초기화
	 * @param 
	 * @return void
	 * @exception    
	******************************************************/ 
	private void resetJobs(){
		this.jobs = new ArrayList<>();
	}
	
	/***************************************************** 
	 * jobs.r 스크립트 파일 삭제
	 * @param 
	 * @return void
	 * @exception    
	******************************************************/ 
	private void removeJobs() {
		File fileName = null;
		fileName = new File("./R/jobs.yaml");
		fileName.delete();
	}
	/***************************************************** 
	 * jobs에 있는 job 정보를 jobs.yaml 파일 생성
	 * @param 
	 * @return void
	 * @exception    
	******************************************************/ 
	private void printJobs(){
		
		if (!this.jobs.isEmpty()){
			ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
			Map<String, Object> m = new HashMap<>();
			try {
				mapper.writeValue(new File("./R/jobs.yaml"), this.jobs);
			} catch (JsonGenerationException e1) {
				logger.error(e1.getMessage(), e1);
			} catch (JsonMappingException e1) {
				logger.error(e1.getMessage(), e1);
			} catch (IOException e1) {
				logger.error(e1.getMessage(), e1);
			} finally {
				if (this.jobs != null) {
					resetJobs();
					runAggregation();
				}
			}
		}
	}
	/***************************************************** 
	 * db에서 job 정보를 가져와 jobs에 추가한다.
	 * 지금은 db연동이 안되있음.
	 * @param 
	 * @return void
	 * @exception    
	******************************************************/ 
	private void getJobs(){
		//db에서 job 정보 가져온다.
		/*if(jobs.isEmpty()) {
			List<Map<String, Object>> jobs = rss.getDBHandler().getNColumnList("logger", "select id, title, schedule, match, function, groupBy, having, limit, type from ? where type = 'y'");
			logger.info("job size - ",jobs);
			for (Map<String, Object> job :jobs){
				int id = Integer.parseInt(job.get("id").toString());
				String title = job.get("title").toString();
				int schedule = Integer.parseInt(job.get("schedule").toString());
				String match = job.get("match").toString();
				String function = job.get("function").toString();
				String groupBy = job.get("groupBy").toString();
				String having = job.get("having").toString();
				int limit = Integer.parseInt(job.get("limit").toString());
				char type = (char) job.get("type");
				this.jobs.add(new JobInfo(id, title, schedule, match, function, groupBy, having, limit, type));
			}
		}*/
		
		/* db 연동 전, 로컬 테스트 */
		// category == 'E007' & s_info %like% '^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$' & d_info %like% '^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$'
		// s_info,d_info,d_port,protocol
		this.jobs.add(new JobInfo(1, "커스텀 job1", 1, 
				"category == 'E002' & s_info %like% '^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$' "
				+ "& d_info %like% '^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$' & !method %in% c(\"null\",\"-\")",
				"?", "s_info.keyword", "?", 10, 'S'));
		this.jobs.add(new JobInfo(2, "커스텀 job2", 5, 
				"category == 'E007' & !method %in% c(\"null\",\"-\")", "?", "risk", "?", 10, 'S'));
		this.jobs.add(new JobInfo(3, "커스텀 job3", 10, 
				"category == 'E007' & d_info %like% '^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$' & !method %in% c(\"null\",\"-\")",
				"?", "method.keyword,risk", "?", 10, 'S'));
		this.jobs.add(new JobInfo(4, "커스텀 job4", 30, 
				"category == 'E007' & s_info %like% '^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$'", 
				"?", "s_info.keyword,d_info.keyword", "?", 10, 'S'));
		this.jobs.add(new JobInfo(5, "커스텀 job5", 60, 
				"category == 'E007' & d_info %like% '^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$' & !method %in% c(\"null\",\"-\")",
				"?", "s_info.keyword,d_info.keyword,method.keyword,risk", "?", 10, 'S'));
		
		logger.info("job size - " + this.jobs.size());
		printJobs();
	}
	
	/***************************************************** 
	 * parser.r 스크립트 실행한다.
	 * printJobs 메소드에서 만들어진 jobs.yaml 파일을 읽고, 다음 명령 실행
	 * @param 
	 * @return void
	 * @exception    
	******************************************************/ 
	private void runAggregation() {
		logger.info("AggregationTask runRScript start");
		Process proc = null;
		ProcessBuilder pb = null;
		
		try {
			
			long startTime = new Date().getTime();
			
			String rscript = "./R/parser.r";
			File rscript_file = new File(rscript);
			if (rscript_file.exists()) {
				Date date = new Date();
				logger.info("PID=RTS Status=Start Result=  Time=" + date + " Cmd=" +rscript);
				
				pb = new ProcessBuilder("/usr/bin/Rscript", rscript);
				File logFile = new File("logs/AggregationTask.log");
				pb.redirectErrorStream(true);
				pb.redirectOutput(Redirect.appendTo(logFile));
				proc = pb.start();
				
				int exitValue = proc.waitFor();
				if (exitValue == 0) {
					logger.debug("PID=RTS Status=End Result=Success ExecuteTime=" + 
							(new Date().getTime()-startTime)/1000.0f + " ExitValue="+exitValue);
				}
				else {
					logger.debug("PID=RTS Status=End Result=Fail ExecuteTime=" + 
							(new Date().getTime()-startTime)/1000.0f + " ExitValue="+exitValue);
				}
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (proc != null) {
				proc.destroy();	
			}
			if (pb != null) {
				pb = null;
			}
			//removeJobs();
		}
	}
}
