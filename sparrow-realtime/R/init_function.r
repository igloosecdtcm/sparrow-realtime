print("inti_function r script start")
preprocessing <- function(str){
  #regexp_enc <- URLencode("^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$")
  #el_regexp_enc <- URLencode("<0-255>.<0-255>.<0-255>.<0-255>")
  str <- gsub("\\'", "", str)
  str <- gsub("\\c\\(", "", str)
  str <- gsub("\\)", "", str)
  str_arr <- strsplit(str, fixed=TRUE, split= "^[0-9]+\\.[0-9]+\\.[0-9]+\\.[0-9]+$")[[1]]
  if (length(str_arr) == 1 & substr(str_arr[1], nchar(str_arr[1])-6, nchar(str_arr[1])-1) == "%like%"){
    str <- paste0(str_arr[1], "<0-255>.<0-255>.<0-255>.<0-255>")
  } else if (length(str_arr) > 1) {
    str <- ''
    for (idx in 1:length(str_arr)){
      if (substr(str_arr[idx], nchar(str_arr[idx])-6, nchar(str_arr[idx])-1) == "%like%") {
        str <- paste0(str, str_arr[idx], "<0-255>.<0-255>.<0-255>.<0-255>")
      }
    }
  }
  return(str)
}

old_aggs_parser <- function(str){
  return(generate_aggs(strsplit(str, fixed=TRUE, split= ",")[[1]]))
}

generate_aggs <- function(str_arr){
  #return(aggs(recursive_genrate_sub_aggs(str_arr, 1)))
  return(recursive_genrate_sub_aggs(str_arr, 1))
}

recursive_genrate_sub_aggs <- function(str_arr, idx){
  if(length(str_arr) > idx) {
    result_json <- '{'
    result_json <- paste0(result_json,'\"',str_arr[[idx]],'_aggs\" : {')
    result_json <- paste0(result_json,'\"terms\" : {')
    result_json <- paste0(result_json, '\"field\" : \"',str_arr[[idx]],'\"')
    result_json <- paste0(result_json, '},')
    result_json <- paste0(result_json, '\"aggs\": ', recursive_genrate_sub_aggs(str_arr, idx+1))
    result_json <- paste0(result_json, '}')
    result_json <- paste0(result_json, '}')
    return(result_json)
  } else {
    return(generate_last_aggs(str_arr[[idx]]))
  }
}
generate_last_aggs <- function(str){
  result_json <- '{'
  result_json <- paste0(result_json,'\"',str,'_aggs\" : {')
  result_json <- paste0(result_json,'\"terms\" : {')
  result_json <- paste0(result_json, '\"field\" : \"',str,'\"')
  result_json <- paste0(result_json, '}')
  result_json <- paste0(result_json, '}')
  result_json <- paste0(result_json, '}')
  return(result_json)
}


old_query_parser <- function(str){
  str <- preprocessing(str)
  types <- split_query(str)
  distributed_result <- distributed_must(types)
  return(generate_query(distributed_result))
}

split_query <- function(str){
  str_arr <- strsplit(str, fixed=TRUE, split= " & ")[[1]]
  types <- list()
  for (idx in 1 : length(str_arr)){
    types[[length(types) + 1]] <- split_type(str_arr[idx])
  }
  return(types)
}

split_type <- function(str){
  str_arr <- strsplit(str, fixed=TRUE, split= " ")[[1]]
  return(list(
    field_name = str_arr[1],
    operator = str_arr[2],
    value = str_arr[3]
  ))
}

distributed_must <- function(str){
  must <- list()
  must_not <- list()
  for(idx in 1:length(str)){
    if (substr(str[[idx]]$field_name,1,1) == "!") {
      #must_not[[length(must_not) + 1]] <- s[[idx]]
      must_not[[length(must_not) + 1]] <- 
        list(
          field_name = substr(str[[idx]]$field_name,2,nchar(str[[idx]]$field_name)),
          operator = str[[idx]]$operator,
          value = str[[idx]]$value
        )
    } else {
      must[[length(must) + 1]] <- str[[idx]]
    }
  }
  return(list(
    must = must,
    must_not = must_not
  ))
}

# generate_query
generate_query <- function(distributed_result){
  #return(query(paste0('{', generate_bool_query(distributed_result), '}')))
  return(paste0('{', generate_bool_query(distributed_result), '}'))
}
generate_bool_query <- function(str){
  result_json <- '\"bool\" : { ';
  must <- '';
  must_not <- '';
  
  if (length(str$must) > 0) {
    must <- generate_must_query(str$must, "must")
  }
  
  if (length(str$must_not) > 0) {
    must_not <- generate_must_query(str$must_not, "must_not")
  }
  
  if (nchar(must) > 0 & nchar(must_not) > 0){
    result_json <- paste0(result_json, must, ',', must_not)
  } else {
    result_json <- paste0(result_json, must, must_not)
  }
  
  result_json <- paste0(result_json, '}')
  return(result_json)
}
generate_must_query <- function(str, type){
  result_json <- paste0('\"', type, '\" : [')
  for(idx in 1 : length(str)) {
    if (idx == 1){
      result_json <- paste0(result_json, generate_must_sub_query(str[[idx]]))
    } else {
      result_json <- paste0(result_json, ",", generate_must_sub_query(str[[idx]]))
    }
  }
  result_json <- paste0(result_json, ']')
  return(result_json)
}

generate_must_sub_query <- function(str){
  if (str$operator == "==") {
    return(paste0('{\"match\" : { \"',str[1],'\" : \"',str[3],'\"}}'))
  } else if (str$operator == "%in%") {
    return(paste0('{\"terms\" : { \"',str[1],'\" : [',str[3],']}}'))
  } else if (str$operator == "%like%") {
    return(paste0('{\"regexp\" : { \"',str[1],'" : "',str[3],'\"}}'))
  } else {
    return ()
  }
}


get_field_set <- function(aggs){
  field_set <- list()
  field_set$field_name <- list()
  field_set$aggs_name <- list()
  aggs_ <- fromJSON(aggs)
  return(recursive_find_aggs_info(field_set, aggs_))
}

recursive_find_aggs_info <- function(field_set, aggs){
  if (!is.null(aggs)) {
    field_set$field_name <- c(field_set$field_name, strsplit(aggs[[names(aggs)]]$terms$field, ".keyword"))
    field_set$aggs_name <- c(field_set$aggs_name, names(aggs))
    recursive_find_aggs_info(field_set, aggs[[names(aggs)]]$aggs) 
  } else {
    return(field_set)
  }
}

aggregation_insert <- function(rule_id, field_set, d){
  idx = 1
  aggregation_body <- list()
  reqursive_find_aggregation_info(d, 1, field_set, aggregation_body, rule_id, idx)
}

reqursive_find_aggregation_info <- function(d, field_idx, field_set, aggregation_body, rule_id, idx){
  if (field_idx == 1) {
    for (key_idx in 1 : length(d$key)) {
      aggregation_body[[paste0(field_set$field_name[[field_idx]], "_name")]] <- d$key[[key_idx]]
      aggregation_body[[paste0(field_set$field_name[[field_idx]], "_count")]] <- d$doc_count[[key_idx]]
      if (length(field_set$field_name) == 1) {
        docs_create(index='aggregations3', type='_doc', id=rule_id, body=aggregation_body)
      } else {
        reqursive_find_aggregation_info(d[[paste0(field_set$aggs_name[[field_idx+1]], ".buckets")]][[key_idx]], field_idx+1, field_set, aggregation_body, rule_id, idx)
      }
    }
  } else {
    if (field_idx < length(field_set$field_name)) {
      for (key_idx in 1 : length(d$key)) {
        aggregation_body[[paste0(field_set$field_name[[field_idx]], "_name")]] <- d$key[[key_idx]]
        aggregation_body[[paste0(field_set$field_name[[field_idx]], "_count")]] <- d$doc_count[[key_idx]]
        reqursive_find_aggregation_info(d[[paste0(field_set$aggs_name[[field_idx+1]])]]$buckets[[key_idx]], field_idx+1, field_set, aggregation_body, rule_id, idx)
      }
    } else {
      for (key_idx in 1 : length(d$key)) {
        aggregation_body[[paste0(field_set$field_name[[field_idx]], "_name")]] <- d$key[[key_idx]]
        aggregation_body[[paste0(field_set$field_name[[field_idx]], "_count")]] <- d$doc_count[[key_idx]]
        docs_create(index='aggregations4', type='_doc', id=rule_id, body=aggregation_body)
      }
    }
  }
}
