package in.nmaloth.maintenance.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebInputException;

import javax.validation.ConstraintViolationException;

@ControllerAdvice
@Slf4j
public class ControllerExceptionHandler {

    @ExceptionHandler({NotFoundException.class})
    public ResponseEntity<String> catchNotFoundException(NotFoundException ex){

        log.error(" The Exception is {}", ex);
        return  ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ex.getMessage());
    }

    @ExceptionHandler({AlreadyPresentException.class,InvalidInputDataException.class,InvalidEnumConversion.class})
    public ResponseEntity<String> catchAlreadyPresentException(Exception ex){

        log.error(" The Exception is {}", ex);
        return  ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }

    @ExceptionHandler(NumberCreationException.class)
    public ResponseEntity<String> numberCreationException(NumberCreationException ex){

        log.error(" The Exception is {}", ex);
        return  ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ex.getMessage());
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public ResponseEntity<String> validationException(WebExchangeBindException ex){

        StringBuilder stringBuilder = new StringBuilder();
        ex.getAllErrors().forEach(objectError -> stringBuilder.append(objectError.toString()));
        ;
        log.info(" Entered Here ");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(stringBuilder.toString());
    }

    @ExceptionHandler(ServerWebInputException.class)
    public ResponseEntity<String> constraintViolationExcepton(ServerWebInputException ex){

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ex.getMessage());
    }



    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> exceptionHandler(Exception ex){

        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ex.getMessage());
    }
}
