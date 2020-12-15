package io.cucumber.junit;

import android.util.Log;

import org.junit.runner.Description;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;

import java.util.List;

import cucumber.runtime.model.CucumberFeature;
import gherkin.ast.Feature;

public class AndroidFeatureRunner extends ParentRunner<AndroidPickleRunner> {

    private final List<AndroidPickleRunner> children;
    private final CucumberFeature cucumberFeature;

    public AndroidFeatureRunner(Class<?> testClass, CucumberFeature cucumberFeature, List<AndroidPickleRunner> children) throws InitializationError {
        super(testClass);
        this.cucumberFeature = cucumberFeature;
        this.children = children;
    }

    @Override
    public String getName() {
        Feature feature = cucumberFeature.getGherkinFeature().getFeature();
        return feature.getKeyword() + ": " + feature.getName();
    }

    @Override
    protected List<AndroidPickleRunner> getChildren() {
        return children;
    }

    @Override
    protected Description describeChild(AndroidPickleRunner child) {
        return child.getDescription();
    }

    @Override
    protected void runChild(AndroidPickleRunner child, RunNotifier notifier) {
        RunNotifier retryNotifier = new RunNotifier();
        RetryRunListener retryListener = new RetryRunListener(notifier);
        retryNotifier.addListener(retryListener);
        while (!retryListener.finished) {
            Log.e("000", "attempts #" + retryListener.attemptCount);
            child.run(retryNotifier);
        }
    }

    private static class RetryRunListener extends RunListener {
        final int maxAttemptCount;
        private final RunNotifier notifier;
        int attemptCount;
        boolean finished;
        boolean hasFailure;

        public RetryRunListener(RunNotifier notifier) {
            this.notifier = notifier;
            maxAttemptCount = 15;
            attemptCount = maxAttemptCount;
            finished = false;
        }

        @Override
        public void testFinished(Description description) throws Exception {
            Log.e("000", "testFinished " + finished + " " + hasFailure);
            if (!hasFailure || attemptCount <= 0) {
                finished = true;
                notifier.fireTestFinished(description);
            }
        }

        @Override
        public void testFailure(Failure failure) throws Exception {
            Log.e("000", "testFailure " + failure);
            hasFailure = true;
            if (attemptCount <= 0) {
                notifier.fireTestFailure(failure);
            } else {
                attemptCount--;
            }
        }

        @Override
        public void testStarted(Description description) throws Exception {
            hasFailure = false;
            if (maxAttemptCount == attemptCount) { // Only fire the first time
                Log.e("000", "testStarted " + description);
                notifier.fireTestStarted(description);
            }
        }

        @Override
        public void testIgnored(Description description) throws Exception {
            //TODO
        }

        @Override
        public void testAssumptionFailure(Failure failure) {
            //TODO
        }

        @Override
        public void testRunFinished(Result result) throws Exception {
            //TODO
        }

        @Override
        public void testRunStarted(Description description) throws Exception {
            //TODO
        }

        @Override
        public void testSuiteFinished(Description description) throws Exception {
            //TODO
        }

        @Override
        public void testSuiteStarted(Description description) throws Exception {
            //TODO
        }
    }
}