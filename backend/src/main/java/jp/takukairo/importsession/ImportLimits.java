package jp.takukairo.importsession;
import java.util.*;
import jp.takukairo.common.*;
import jp.takukairo.common.ApiException.FieldError;
public final class ImportLimits {
 public static final long FILE=20_971_520,SESSION=52_428_800,TEXT=2_097_152;public static final int FILE_COUNT=20;
 public static void files(long currentBytes,int count,List<Long> added,boolean replacement,long oldBytes){var errors=new ArrayList<FieldError>();for(int i=0;i<added.size();i++)if(added.get(i)>FILE)errors.add(new FieldError(replacement?"/file":"/files/"+i,"IMPORT_FILE_TOO_LARGE","1ファイルは20MB以下にしてください。"));if(!replacement&&count+added.size()>FILE_COUNT)errors.add(new FieldError("/sources","IMPORT_FILE_COUNT_EXCEEDED","1回のインポートで保持できるファイルは20件までです。"));if(currentBytes-oldBytes+added.stream().mapToLong(Long::longValue).sum()>SESSION)errors.add(new FieldError("/sources","IMPORT_SESSION_SIZE_EXCEEDED","1回のインポートで保持できるデータ量は合計50MBまでです。"));fail(errors);}
 public static void text(long current,long old,long bytes){var errors=new ArrayList<FieldError>();if(bytes>TEXT)errors.add(new FieldError("/text","IMPORT_TEXT_TOO_LARGE","貼り付けテキストは2MB以下にしてください。"));if(current-old+bytes>SESSION)errors.add(new FieldError("/sources","IMPORT_SESSION_SIZE_EXCEEDED","1回のインポートで保持できるデータ量は合計50MBまでです。"));fail(errors);}
 private static void fail(List<FieldError> e){if(!e.isEmpty())throw new ApiException(400,"VALIDATION_ERROR","インポートするデータ量を確認してください。",e);}
}
