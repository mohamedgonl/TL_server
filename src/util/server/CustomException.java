package util.server;

public class CustomException extends Exception{
    private short errorCode;

    public CustomException(String message, short errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public CustomException(short errorCode) {
        super();
        this.errorCode = errorCode;
    }

    public short getErrorCode() {
        return errorCode;
    }
}
