package in.nmaloth.maintenance.exception;

public class InvalidEnumConversion extends RuntimeException {

    public InvalidEnumConversion(){
        super();
    }

    public InvalidEnumConversion(String message){
        super(message);
    }
}
