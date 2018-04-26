package com.igloosec.realtime;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimerTask;

import org.apache.commons.io.output.FileWriterWithEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
		fileName = new File("./R/jobs.r");
		fileName.delete();
	}
	/***************************************************** 
	 * jobs에 있는 job 정보를 r스크립트로 만들고, jobs.r로 파일 생성
	 * @param 
	 * @return void
	 * @exception    
	******************************************************/ 
	private void printJobs(){
		StringBuffer sb = new StringBuffer();
		
		if (!this.jobs.isEmpty()){
			sb.append("all_query <- list()\n");
			
			for (JobInfo job : this.jobs) {
				//jobs 데이터를 r스크립트에 바인딩해야 함
				//sb.append("all_query <- (all_query,1)\n");
			}
		}
		
		File fileName = null;
		FileWriterWithEncoding fw = null;
		BufferedWriter bw = null;
		PrintWriter pw = null;
		try {
			fileName = new File("./R/jobs.r");
			if (fileName.getParentFile().exists()) {
				fw = new FileWriterWithEncoding(fileName, "UTF-8", false);
				bw = new BufferedWriter(fw);
				pw = new PrintWriter(bw);
				
				pw.println(sb.toString());
			}
		} catch(IOException ioe) {
			logger.error(ioe.getMessage(), ioe);
		} catch(Exception e) {
			logger.error(e.getMessage(), e);
		} finally{
			if (pw != null) {
				pw.close();
			}
			if (bw != null) {
				try {
					bw.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
			if (fw != null) {
				try {
					fw.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
			if (this.jobs != null) {
				resetJobs();
				runAggregation();
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
			List<Map<String, Object>> jobs = rss.getDBHandler().getNColumnList("logger", "select id, query, aggs, size, sort, type, status from ? where type = 'y'");
			logger.info("job size - ",jobs);
			for (Map<String, Object> job :jobs){
				String id = job.get("id").toString();
				String query = job.get("query").toString();
				String aggs = job.get("aggs").toString();
				int size = Integer.parseInt(job.get("size").toString());
				String sort = job.get("sort").toString();
				String type = job.get("type").toString();
				this.jobs.add(new JobInfo(id, query, aggs, size, sort, type));
			}
		}*/
		
		// category == 'E007' & s_info %like% '^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$' & d_info %like% '^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$'
		// s_info,d_info,d_port,protocol
		String id = "";
		String query = "category == 'E007'";
		String aggs = "s_info,d_info,d_port,protocol";
		int size = 0;
		String sort = "";
		String type = "";
		this.jobs.add(new JobInfo(id, query, aggs, size, sort, type));
		logger.info("job size - " + this.jobs.size());
		printJobs();
	}
	
	/***************************************************** 
	 * parser.r 스크립트 실행한다.
	 * printJobs 메소드에서 만들어진 jobs.r 파일을 읽고, 다음 명령 실행
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
			removeJobs();
		}
	}

}
