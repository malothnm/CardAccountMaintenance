package in.nmaloth.maintenance.exception;

public class NotFoundException extends RuntimeException {

    public NotFoundException(String s) {
        super(s);
    }
    public NotFoundException(){
        super();
    }
}
