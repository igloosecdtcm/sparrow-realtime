print("parser r script start")
source("./R/init.r")
source("./R/init_function.r")
source("./R/jobs.r")
# 210
# category == 'E002' & s_info %like% '^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$' & d_info %like% '^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$' & !method %in% c('null','-')
# s_info,d_info,method,risk
connect()
new_query <- old_query_parser("category == 'E002' & s_info %like% '^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$' & d_info %like% '^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$' & !method %in% c('null','-')")
new_aggs <- old_aggs_parser("s_info.keyword,d_info.keyword,method.keyword,risk")
field_set <- get_field_set(new_aggs)
query_aggs <- query(new_query) + aggs(new_aggs)
result <- elastic("http://localhost:9200", "sniper", "_doc") %search% query_aggs
aggregation_insert(as.numeric(Sys.time()), field_set, result)