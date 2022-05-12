package Common.Messages;

public class RequestFileMessage extends Message {
    String path;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
