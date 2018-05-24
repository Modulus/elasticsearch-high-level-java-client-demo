1. docker-compose up -d
2. sh player.sh
3. sh nested.sh
4. run the main program

## TO delete indices
curl -X DELETE "localhost:9200/nested"
curl -X DELETE "localhost:9200/hockey"



curl -X GET "localhost:9200/_search" -H 'Content-Type: application/json' -d'
{
    "query": {
        "nested" : {
            "path" : "obj1",
            "score_mode" : "avg",
            "query" : {
                "bool" : {
                    "must" : [
                    { "match" : {"obj1.type" : "nested"} }
                    ]
                }
            }
        }
    }
}
'
