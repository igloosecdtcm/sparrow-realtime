package com.igloosec.realtime;

import com.igloosec.jdbc.service.DBHandler;
import com.igloosec.jdbc.service.TableManagerService;

public interface RealtimeStatsService {
	public DBHandler getDBHandler();
	public TableManagerService getTableManagerService();
}
