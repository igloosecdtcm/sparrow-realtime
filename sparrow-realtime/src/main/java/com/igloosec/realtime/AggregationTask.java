package com.igloosec.realtime;

import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AggregationTask extends TimerTask{
	private final Logger logger =  LoggerFactory.getLogger(ValkyrieClientTask.class);

	@Override
	public void run() { 
		logger.info("AggregationTask start");
	}

}
