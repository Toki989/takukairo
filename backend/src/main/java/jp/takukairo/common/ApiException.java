package jp.takukairo.common;
import java.util.*;
public class ApiException extends RuntimeException {
    public record FieldError(String path,String code,String message) {}
    public final int status;
    public final String code;
    public final List<FieldError> fields;
    public ApiException(int status,String code,String message) { this(status,code,message,List.of()); }
    public ApiException(int status,String code,String message,List<FieldError> fields) { super(message);this.status=status;this.code=code;this.fields=fields; }
    public static ApiException notFound(){return new ApiException(404,"NOT_FOUND","対象が見つかりません。");}
    public static ApiException conflict(){return new ApiException(409,"OPTIMISTIC_LOCK_CONFLICT","別の更新が行われています。最新の内容を読み込み直して確認してください。");}
    public static ApiException invalid(String path,String code,String message){return new ApiException(400,"VALIDATION_ERROR","入力内容を確認してください。",List.of(new FieldError(path,code,message)));}
}
