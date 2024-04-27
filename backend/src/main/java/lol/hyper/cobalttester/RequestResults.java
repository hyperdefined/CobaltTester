package lol.hyper.cobalttester;

public class RequestResults {

    private final String responseContent;
    private final int responseCode;

    public RequestResults(String responseContent, int responseCode) {
        this.responseContent = responseContent;
        this.responseCode = responseCode;
    }

    public String getResponseContent() {
        return responseContent;
    }

    public int getResponseCode() {
        return responseCode;
    }
}
