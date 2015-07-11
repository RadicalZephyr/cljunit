package failing.cljunit;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import static org.junit.Assert.assertEquals;

@RunWith(Suite.class)
@Suite.SuiteClasses({FailingTest.class})
public class CljUnitFailingSuite {

}
