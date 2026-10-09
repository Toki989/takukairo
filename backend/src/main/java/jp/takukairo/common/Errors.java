package jp.takukairo.common;
import java.util.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.dao.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
@RestControllerAdvice
public class Errors {
    public static Map<String,Object> body(ApiException e){return Map.of("code",e.code,"message",e.getMessage(),"fieldErrors",e.fields,"traceId",UUID.randomUUID().toString(),"context",Map.of());}
    @ExceptionHandler(ApiException.class) ResponseEntity<?> api(ApiException e){if(e.status==400&&e.fields.stream().anyMatch(f->f.path().equals("/file")))jp.takukairo.security.SecurityEvents.record("UPLOAD_REJECT");return ResponseEntity.status(e.status).body(body(e));}
    @ExceptionHandler({org.springframework.orm.ObjectOptimisticLockingFailureException.class,jakarta.persistence.OptimisticLockException.class}) ResponseEntity<?> optimistic(Exception e){return api(ApiException.conflict());}
    @ExceptionHandler(MaxUploadSizeExceededException.class) ResponseEntity<?> tooLarge(Exception e){return api(new ApiException(413,"PAYLOAD_TOO_LARGE","送信データが大きすぎます。"));}
    @ExceptionHandler({HttpMessageNotReadableException.class,org.springframework.web.method.annotation.MethodArgumentTypeMismatchException.class,org.springframework.web.bind.MissingServletRequestParameterException.class,org.springframework.web.bind.MissingPathVariableException.class,org.springframework.web.multipart.support.MissingServletRequestPartException.class}) ResponseEntity<?> malformed(Exception e){return api(new ApiException(400,"MALFORMED_REQUEST","送信内容を確認してください。"));}
    @ExceptionHandler(DataIntegrityViolationException.class) ResponseEntity<?> constraint(Exception e){return api(new ApiException(409,"CONFLICT","関連するデータを確認してください。"));}
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class) ResponseEntity<?> missing(Exception e){return api(ApiException.notFound());}
    @ExceptionHandler(Exception.class) ResponseEntity<?> internal(Exception e){org.slf4j.LoggerFactory.getLogger(Errors.class).error("Internal failure type={} location={}",e.getClass().getSimpleName(),Arrays.stream(e.getStackTrace()).filter(f->f.getClassName().startsWith("jp.takukairo")).limit(4).toList());return api(new ApiException(500,"INTERNAL_ERROR","処理を完了できませんでした。"));}
}
