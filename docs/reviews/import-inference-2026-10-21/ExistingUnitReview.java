import org.junit.platform.launcher.core.*;
import org.junit.platform.launcher.listeners.*;
import static org.junit.platform.engine.discovery.DiscoverySelectors.*;
import static jp.takukairo.common.Json.*;
public class ExistingUnitReview {
 public static void main(String[]args)throws Exception {
  var request=LauncherDiscoveryRequestBuilder.request().selectors(
   selectClass("jp.takukairo.ImportInferenceTest"),selectClass("jp.takukairo.ImportBoundaryTest"),
   selectClass("jp.takukairo.ImportReviewRegressionTest"),selectClass("jp.takukairo.ImportCurrentDefectsTest"),
   selectClass("jp.takukairo.ImportFiveDefectsTest")).build();
  var summary=new SummaryGeneratingListener();LauncherFactory.create().execute(request,summary);var s=summary.getSummary();
  var result=map("method","JUnit Platform launcher; current sources; five parser/boundary suites; no DB", "found",s.getTestsFoundCount(),"succeeded",s.getTestsSucceededCount(),"failed",s.getTestsFailedCount(),"skipped",s.getTestsSkippedCount(),"failures",s.getFailures().stream().map(f->map("test",f.getTestIdentifier().getDisplayName(),"error",f.getException().toString())).toList());
  java.nio.file.Files.writeString(java.nio.file.Path.of(args[0]),write(result),java.nio.charset.StandardCharsets.UTF_8);System.out.println(write(result));
  if(s.getTestsFailedCount()>0)System.exit(1);
 }
}
