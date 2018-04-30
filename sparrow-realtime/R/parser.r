print("parser r script start")
source("./R/init.r")
source("./R/init_function.r")

jobs <- yaml.load_file("./R/jobs.yaml")
connect(es_host = "127.0.0.1", es_port = 9200)
for (idx in 1:length(jobs)){
  new_query <- old_query_parser(jobs[[idx]]$match)
  new_aggs <- old_aggs_parser(jobs[[idx]]$groupBy)
  field_set <- get_field_set(new_aggs)
  #print(new_query)
  query_aggs <- query(new_query) + aggs(new_aggs)
  result <- elastic("http://127.0.0.1:9200", "sniper", "_doc") %search% query_aggs
  aggregation_insert(as.numeric(Sys.time()), field_set, result)
  print(paste0(jobs[[idx]]$title, " - success"))
}
