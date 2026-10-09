package jp.takukairo.security;
public final class SecurityEvents {
 private static final org.slf4j.Logger LOG=org.slf4j.LoggerFactory.getLogger(SecurityEvents.class);
 public static void record(String event){LOG.info("security_event={}",event);}
 private SecurityEvents(){}
}
