package in.nmaloth.maintenance.exception;

public class InvalidInputDataException extends RuntimeException {

    public InvalidInputDataException(){
        super();
    }

    public InvalidInputDataException(String message){
        super(message);
    }
}
