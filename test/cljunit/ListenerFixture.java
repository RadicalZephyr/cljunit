package cljunit;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.RunListener;

public class ListenerFixture extends RunListener {
    public static boolean runStartedCalled = false;
    public static boolean runFinishedCalled = false;

    @Override
    public void testRunStarted(Description description) {
        runStartedCalled = true;
    }

    @Override
    public void testRunFinished(Result result) {
        runFinishedCalled = true;
    }

    public static void reset() {
        runStartedCalled = false;
        runFinishedCalled = false;
    }
}
