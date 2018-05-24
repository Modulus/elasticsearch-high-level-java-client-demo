import com.fasterxml.jackson.databind.ObjectMapper;
import io.vavr.collection.List;
import io.vavr.control.Option;
import io.vavr.control.Try;
import org.apache.http.HttpHost;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;

public class Importer {

    final private ObjectMapper objectMapper;
    final private RestHighLevelClient client;

    public Importer(ObjectMapper objectMapper, RestHighLevelClient elasticClient){
        this.objectMapper = objectMapper;

        this.client = elasticClient;
    }

    public Importer(){
        this.objectMapper =  new ObjectMapper();

        this.client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("localhost", 9200, "http")));
    }

    public void run() {
        System.out.println("Starting");




        System.out.println("Searching for specific player");

        SearchRequest request = new SearchRequest();
        SearchSourceBuilder source = new SearchSourceBuilder();
        source.query(QueryBuilders.matchPhraseQuery("first", "sean"));

        request.source(source);

        List<Player> players = searchPlayers(request);

        players.forEach(player -> System.out.println(player));


        System.out.println("List all players");

        source.query(QueryBuilders.existsQuery("first"));
        request.source(source);


        players = searchPlayers(request);
        System.out.println(String.format("Players found: %s", players.length()));
        players.forEach(player -> System.out.println(player));


        System.out.println("Trying query with multiple phrases");
        source.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.matchPhraseQuery("first", "sean"))
                .must(QueryBuilders.matchPhraseQuery("last", "monohan")));

        request.source(source);
        players = searchPlayers(request);
        players.forEach(player -> System.out.println(player));


        System.out.println("Trying nested query");


        source.query(QueryBuilders.boolQuery()
                .must(QueryBuilders.nestedQuery("works", QueryBuilders.matchPhraseQuery("works.dummy", "match"), ScoreMode.Avg))
                .minimumShouldMatch(1));

        //System.out.println(source.query());


        request.source(source);
        Try.of(() -> client.search(request))
                .onFailure(exception -> System.out.println(String.format("Failed: %s", exception)))
                .onSuccess( result -> result.getHits().forEach( hit -> System.out.println(hit.getSourceAsString())));


        System.out.println("Closing connection");
        Try.run(client::close).onFailure(exception -> System.out.println(String.format("Failed to close client %s", exception)));

        System.out.println("Finished");
    }

    private List<Player> searchPlayers( SearchRequest request) {
        return Try.of(() -> client.search(request))
                    .map(result -> extractHits(result.getHits()))
                    .onFailure(exception -> System.out.println(String.format("Failed: %s", exception)))
                    .getOrElse(List.empty());
    }

    private List<Player> extractHits(SearchHits hits) {
        return List.of(hits.getHits()).map(h -> extractHit(h)).filter(Option::isDefined).map(Option::get);
    }

    private Option<Player> extractHit(SearchHit searchHit) {
        return Try.of(() -> Option.of(objectMapper.readValue(searchHit.getSourceAsString(), Player.class))).onFailure(e -> System.out.println("Failed")).getOrElse(Option.none());
    }

}