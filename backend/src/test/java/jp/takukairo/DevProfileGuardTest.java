package jp.takukairo;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import jp.takukairo.security.DevLoginController;
import jp.takukairo.common.Store;
import static org.mockito.Mockito.mock;
import static org.junit.jupiter.api.Assertions.*;
class DevProfileGuardTest {
 @Test void fixedDevLoginExistsOnlyInDevOrTest(){
  var runner=new ApplicationContextRunner().withUserConfiguration(DevLoginController.class).withBean(jakarta.persistence.EntityManagerFactory.class,()->mock(jakarta.persistence.EntityManagerFactory.class)).withBean(Store.class,()->mock(Store.class)).withBean(SecurityContextRepository.class,()->mock(SecurityContextRepository.class)).withBean(HttpSessionCsrfTokenRepository.class,()->new HttpSessionCsrfTokenRepository());
  runner.withInitializer(c->c.getEnvironment().setActiveProfiles("production-placeholder")).run(c->assertTrue(c.getBeansOfType(DevLoginController.class).isEmpty()));
  runner.withInitializer(c->c.getEnvironment().setActiveProfiles("dev")).run(c->assertEquals(1,c.getBeansOfType(DevLoginController.class).size()));
 }
}
