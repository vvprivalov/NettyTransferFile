package Common.Messages;

public class FileTransferMessage extends Message {
    private byte[] content;
    private long startPosition;

    public byte[] getContent() {

        return content;
    }

    public void setContent(byte[] content) {

        this.content = content;
    }

    public long getStartPosition() {

        return startPosition;
    }

    public void setStartPosition(long startPosition) {

        this.startPosition = startPosition;
    }
}
