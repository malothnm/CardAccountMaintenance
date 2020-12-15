package in.nmaloth.maintenance.exception;

public class AlreadyPresentException extends RuntimeException {
    public AlreadyPresentException(){
        super();
    }

    public AlreadyPresentException(String msg){
        super(msg);
    }
}
