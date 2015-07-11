package failing.cljunit;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FailingTest {
    @Test
    public void runTestOne() {
        assertEquals(true, false);
    }
}
