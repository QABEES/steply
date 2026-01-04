package org.jsmart.steply.template;

import org.jsmart.zerocode.core.domain.EnvProperty;
import org.jsmart.zerocode.core.domain.TargetEnv;
import org.jsmart.zerocode.core.domain.TestPackageRoot;
import org.jsmart.zerocode.core.runner.ZeroCodePackageRunner;
import org.junit.runner.RunWith;

@TargetEnv("config/github_host.properties") // <--- "app_host_sst.properties" if running against 'sst'
@TestPackageRoot("helloworld")              // <--- Root of the all tests folder in the test/resources
@EnvProperty("_${env}")                     // <--- mvn clean install -Denv=dev or -Denv=qa or -Denv=sit
@RunWith(ZeroCodePackageRunner.class)
public class HelloWorldSuiteTest {

    // Keep this space clear \\

}
