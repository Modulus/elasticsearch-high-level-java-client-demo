#!/bin/sh

echo "\n --- delete index"
curl -X DELETE 'http://localhost:9200/myindex'

echo "\n --- create index and put mapping into place"
curl -X PUT 'http://localhost:9200/myindex/'  -H 'Content-Type: application/json'  -d '{
    "mappings" : {
        "person" : {
            "properties" : {
                "name" : {"type" : "text"},
                "works" : {
                    "type" : "nested",
                    "include_in_parent" : false,
                    "properties" : {
                        "title" : {"type" : "text"},
                        "current" : {"type" : "boolean"},
                        "dummy" : {"type" : "text"}
                    }
                }
            }
        }
    },
    "settings" : {
        "number_of_shards" : 1,
        "number_of_replicas" : 0
    }
}'

echo "\n --- index data"
curl -X PUT 'http://localhost:9200/myindex/person/1'  -H 'Content-Type: application/json' -d '
{
    "name" : "Lukas",
    "works" : [
        {
            "title" : "developer",
            "current" : true,
            "dummy" : "match"
        },
        {
            "title" : "dad",
            "current" : true
        },
        {
            "title" : "husband",
            "current" : true,
            "dummy" : "foo"
        },
        {
            "title" : "brother",
            "current" : true,
            "dummy" : "bar"
        }
    ]
}'

curl -X PUT 'http://localhost:9200/myindex/person/2'  -H 'Content-Type: application/json' -d '
{
    "name" : "Karel",
    "works" : [
        {
            "title" : "developer",
            "current" : true,
            "dummy" : "match"
        }
    ]
}'

curl -X PUT 'http://localhost:9200/myindex/person/3'  -H 'Content-Type: application/json' -d '
{
    "name" : "Jan",
    "works" : [
        {
            "title" : "developer",
            "current" : false,
            "dummy" : "match"
        }
    ]
}'

#echo "\n --- optimize"
#curl -X PUT 'http://localhost:9200/_optimize'

#!/bin/sh

echo "\n --- query"
curl -X GET 'http://localhost:9200/_search?pretty=true'  -H 'Content-Type: application/json' -d '
{
    "query" : {
        "nested" : {
            "path" : "works",
            "query" : {
                "bool" : {
                    "must" : [
                        { "match" : { "works.dummy" : "match"} }
                    ],
                    "should" : [
                        { "match" : { "works.title" : "developer"} },
                        { "match" : { "works.current" : true } }
                    ]
                }
            }
        }
    },
    "fields" : [
        "name"
    ]
}'

echo "\n --- done"