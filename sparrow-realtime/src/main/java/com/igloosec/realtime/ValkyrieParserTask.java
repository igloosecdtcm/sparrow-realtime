package com.igloosec.realtime;

import java.io.File;
import java.io.IOException;
import java.lang.ProcessBuilder.Redirect;
import java.util.Date;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValkyrieParserTask extends TimerTask {
	private final Logger logger =  LoggerFactory.getLogger(ValkyrieParserTask.class);
	@Override
	public void run() {
		System.out.println("ValkyrieParserTask start");
		runRScript();
	}

	private void runRScript() {
		System.out.println("ValkyrieParserTask runRScript start");
		Process proc = null;
		ProcessBuilder pb = null;
		
		try {
			
			long startTime = new Date().getTime();
			
			String rscript = "./R/parser.r";
			File rscript_file = new File(rscript);
			if (rscript_file.exists()) {
				Date date = new Date();
				logger.debug("PID=RTS Status=Start Result=  Time=" + date + " Cmd=" +rscript);
				System.out.println("PID=RTS Status=Start Result=  Time=" + date + " Cmd=" +rscript);
				
				pb = new ProcessBuilder("/usr/bin/Rscript", rscript);
				File logFile = new File("logs/ValkyrieParserTask.log");
				pb.redirectErrorStream(true);
				pb.redirectOutput(Redirect.appendTo(logFile));
				proc = pb.start();
				
				int exitValue = proc.waitFor();
				if (exitValue == 0) {
					System.out.println("PID=RTS Status=End Result=Success ExecuteTime=" + (new Date().getTime()-startTime)/1000.0f + " ExitValue="+exitValue);
					logger.debug("PID=RTS Status=End Result=Success ExecuteTime=" + (new Date().getTime()-startTime)/1000.0f + " ExitValue="+exitValue);
				}
				else {
					System.out.println("PID=RTS Status=End Result=Fail ExecuteTime=" + (new Date().getTime()-startTime)/1000.0f + " ExitValue="+exitValue);
					logger.debug("PID=RTS Status=End Result=Fail ExecuteTime=" + (new Date().getTime()-startTime)/1000.0f + " ExitValue="+exitValue);
				}
			}
		} catch (IOException e) {
			System.out.println(e.getMessage());
			logger.error(e.getMessage(), e);
		} catch (InterruptedException e) {
			System.out.println(e.getMessage());
			logger.error(e.getMessage(), e);
		} finally {
			if (proc != null) {
				proc.destroy();	
			}
			if (pb != null) {
				pb = null;
			}
		}
	}
}
