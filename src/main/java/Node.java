public class Node {
    private String _nextNodeUrl;
    private String _previousNodeUrl;
    private String _url;

    public Node(String url) {
        _url = url;
    }

    public String getUrl() {
        return _url;
    }

    public String getNextNodeUrl() {
        return _nextNodeUrl;
    }

    public String getPreviousNodeUrl() {
        return _previousNodeUrl;
    }

    public void setNextNodeUrl(String nextNodeUrl) {
        _nextNodeUrl = nextNodeUrl;
    }

    public void setPreviousNodeUrl(String previousNodeUrl) {
        _previousNodeUrl = previousNodeUrl;
    }
}
