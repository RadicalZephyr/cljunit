package cljunit;

import junit.framework.TestCase;

public class CljUnitV3Test extends TestCase {

    public void testSum() {
        int result = 5 + 10;
        assertEquals(15, result);
    }
}
