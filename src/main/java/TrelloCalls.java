
    import org.json.JSONArray;
    import org.json.JSONObject;

    import java.io.IOException;
    import java.net.URI;
    import java.net.http.HttpClient;
    import java.net.http.HttpRequest;
    import java.net.http.HttpResponse;

public class TrelloCalls {

    private final String baseUrl = "https://api.trello.com/1/";
    private final HttpClient client;
    private final String urlEndpoint;
    private String parameters="";
    private final String key = "90fb4c3f6615067b94535f130c0d7b4f";
    private final String token = "c95f8154db55a4f2297c9ab6d431b1d3d5dfcac19bc3bafb3bce4b35ab9fcf31";


    public TrelloCalls(HttpClient client, String trelloUrlEndPoint, String parameters){
        this.client = client;
        this.urlEndpoint = trelloUrlEndPoint;
        this.parameters = parameters;
    }

    public TrelloCalls(HttpClient client, String trelloUrlEndPoint){
        this.client = client;
        this.urlEndpoint = trelloUrlEndPoint;
    }

    //TODO under construction
    public JSONObject getTrelloAPICallObject() {

        System.out.println("- GET Call to Trello -");
        String uri = String.format("%s%s?%s&key=%s&token=%s", this.baseUrl, this.urlEndpoint, this.parameters, this.key, this.token);

        System.out.println("URL -- "+ uri);

        var request = HttpRequest.newBuilder(
                URI.create(uri))
                .header("accept", "application/json")
                .GET()
                .build();

        APICaller apiCaller = new APICaller(client, request);

        return new JSONObject(apiCaller.makeAPICall().body());
    }

    public JSONArray getTrelloAPICallArray() {

        System.out.println("- GET Call to Trello -");
        String uri = String.format("%s%s?%s&key=%s&token=%s", this.baseUrl, this.urlEndpoint, this.parameters, this.key, this.token);

        System.out.println("URL -- "+ uri);

        var request = HttpRequest.newBuilder(
                URI.create(uri))
                .header("accept", "application/json")
                .GET()
                .build();

        APICaller apiCaller = new APICaller(client, request);

        return new JSONArray(apiCaller.makeAPICall().body());
    }

    public JSONObject postTrelloAPICall() {

        System.out.println("- POST Call to Trello -");
        String uri = String.format("%s%s?%s&key=%s&token=%s", this.baseUrl, this.urlEndpoint, this.parameters, this.key, this.token);
        System.out.println("URL -- "+ uri);
        var request = HttpRequest.newBuilder(
                URI.create(uri))
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();

        APICaller apiCaller = new APICaller(client, request);

        return new JSONObject(apiCaller.makeAPICall().body());
    }

    public JSONObject putTrelloAPICall(JSONObject innerRequestBody) {

        System.out.println("- PUT Call to Trello -");
        String uri = String.format("%s%s?%s&key=%s&token=%s",
                this.baseUrl, this.urlEndpoint, this.parameters, this.key, this.token);

        System.out.println("URL -- "+ uri);
        System.out.println("Body -- "+ innerRequestBody);

        var request = HttpRequest.newBuilder(
                URI.create(uri))
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .PUT(buildRequest(innerRequestBody))
                .build();

        APICaller apiCaller = new APICaller(client, request);

        return new JSONObject(apiCaller.makeAPICall().body());
    }

    public JSONObject deleteTrelloAPICall(String cardId) {

        System.out.println("- Delete Call to Trello -");
        String uri = String.format("%s%s%s?&key=%s&token=%s",
                this.baseUrl, this.urlEndpoint, cardId, this.key, this.token);

        System.out.println("URL -- "+ uri);

        var request = HttpRequest.newBuilder(
                URI.create(uri))
                .header("accept", "application/json")
                .header("Content-Type", "application/json")
                .DELETE()
                .build();

        System.out.println("- Delete Call to Trello -");
        APICaller apiCaller = new APICaller(client, request);

        return new JSONObject(apiCaller.makeAPICall().body());
    }

    public HttpRequest.BodyPublisher buildRequest(JSONObject innerRequest){
        JSONObject requestBody = new JSONObject();
        requestBody.put("value", innerRequest);
        return HttpRequest.BodyPublishers.ofString(requestBody.toString());
    }
}
