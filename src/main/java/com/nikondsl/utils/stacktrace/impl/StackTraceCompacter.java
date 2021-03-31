package com.nikondsl.utils.stacktrace.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StackTraceCompacter {
    private static final int DEFAULT_LENGTH = 512;
    private static final AtomicInteger NUMBER_ONE = new AtomicInteger(1);
    private final List<ProcessorRule> allRulesToCollapse = Arrays.asList(new ProcessorRule[] {
            new ProcessorRule("-- REFLECTION", new String[] {"java.lang.reflect.", "sun.reflect.", "jdk.internal.reflect."}),
            new ProcessorRule("-- TOMCAT", new String[]{"org.apache.catalina.", "org.apache.coyote.", "org.apache.tomcat."}),
            new ProcessorRule("-- WEBSPHERE", new String[] {"com.ibm.ws.", "com.ibm.websphere."}),
            new ProcessorRule("-- SPRING", "org.springframework."),
            new ProcessorRule("-- FREEMARKER CACHE", "freemarker.cache."),
            new ProcessorRule("-- JACKSON", "com.fasterxml.jackson."),
            new ProcessorRule("-- ACTIVEMQ", "org.apache.activemq."),
            new ProcessorRule("-- HIBERNATE", "org.hibernate."),
            new ProcessorRule("-- DB driver (MS SQL)", "com.microsoft.sqlserver."),
            new ProcessorRule("-- DB driver (MySQL)", "com.mysql."),
            new ProcessorRule("-- DB driver (Oracle)", "oracle.jdbc."),
            new ProcessorRule("-- JUnit", "org.junit."),
            new ProcessorRule("-- Mockito", "org.mockito."),
    });

    private Throwable currentException;
    private String compactedBody;
    private final Stack<StackTraceHolder> stackTraceElements = new Stack<>();
    private final List<ProcessorRule> allRulesToLeftExpanded = new ArrayList<>();
    private final ConcurrentMap<Integer, AtomicInteger> collectedExceptions = new ConcurrentHashMap<>();

    private static class StackTraceHolder {
        private StackTraceElement element;
        private int counter = 0;
        private boolean compacted = false;
        private ProcessorRule ruleWhichMet;
        private String compactName;

        StackTraceHolder(StackTraceElement element) {
            this.element = element;
        }

        void incrementCounter() {
            counter++;
        }

        int getCounter() {
            return counter;
        }

        void setCompactName(String compactName) {
            this.compactName = compactName;
            compacted = true;
        }

        boolean isCompacted() {
            return compacted;
        }

        @Override
        public String toString() {
            if (!compacted) {
                return element.toString();
            }
            if (counter > 1) {
                return "\t" + compactName + "\t<" + counter + " lines>";
            }
            return "\t" + compactName;
        }
    }

    public StackTraceCompacter() {
    }

    public StackTraceCompacter(Throwable currentException) {
        init(currentException);
    }

    public void init(Throwable throwable) {
        this.currentException = throwable;
        synchronized (this) {
            stackTraceElements.clear();
            for (StackTraceElement element : throwable.getStackTrace()) {
                StackTraceHolder last = stackTraceElements.isEmpty() ? null : stackTraceElements.peek();
                String canonicalName = element.toString();
                boolean noRuleMet = true;
                for (ProcessorRule rule : allRulesToCollapse) {
                    if (!rule.processCurrentLine(canonicalName)) {
                        continue;
                    }
                    if (last != null &&
                        last.isCompacted() &&
                        last.ruleWhichMet == rule) {
                        //already compacted
                        last.incrementCounter();
                        noRuleMet = false;
                        break;
                    }
                    //put first compacted
                    stackTraceElements.push(new StackTraceHolder(element));
                    last = stackTraceElements.peek();
                    last.ruleWhichMet = rule;
                    last.setCompactName(rule.compactedName);
                    last.incrementCounter();
                    noRuleMet = false;
                    break;
                }
                if (noRuleMet) {
                    stackTraceElements.push(new StackTraceHolder(element));
                }
            }
        }
        compactedBody = generateString(false);
        AtomicInteger counterOfExceptionNew = new AtomicInteger(1);
        AtomicInteger counterOfExceptionOld = collectedExceptions.putIfAbsent(compactedBody.hashCode(), counterOfExceptionNew);
        if (counterOfExceptionOld != null) {
            counterOfExceptionOld.incrementAndGet();
            //remove everything collected, 100 is enough
            if (collectedExceptions.size() > 100) {
                collectedExceptions.clear();
                collectedExceptions.put(compactedBody.hashCode(), counterOfExceptionNew);
            }
        }
    }

    /**
     * Allows to add user defined rule for compacting rows.
     * @param compactedName name of rule - will be visible in stacktrace.
     * @param rule in fact array of texts, you do not want to see expanded in stacktrace.
     */
    public void addRuleToCollapse(String compactedName, String[] rule) {
        allRulesToCollapse.add(new ProcessorRule(compactedName, rule));
    }

    /**
     * Allows to add rules which will always be expanded. Usually it's not necessary,
     * but it can help processing faster.
     * @param ruleName just name of rule, it's not used anywhere.
     * @param rule in fact array of texts, you want to see expanded in stacktracea.
     */
    public void addRuleToBeLeftExpanded(String ruleName, String[] rule) {
        allRulesToLeftExpanded.add(new ProcessorRule(ruleName, rule));
    }

    private static class ProcessorRule {
        private final Set<String> rules;
        private final String compactedName;

        ProcessorRule(String compactedName, String rule) {
            this.rules = new HashSet<>();
            rules.add(rule);
            this.compactedName = compactedName;
        }

        ProcessorRule(String compactedName, String[] rules) {
            this.rules = new HashSet<>();
            Arrays.asList(rules).forEach(this.rules::add);
            this.compactedName = compactedName;
        }

        boolean processCurrentLine(String className) {
            return rules.stream().anyMatch(className::contains);
        }

        @Override
        public String toString() {
            return "ProcessorRule{" +
                    "" + compactedName +
                    '}';
        }
    }

    public String generateString() {
        return generateString(true);
    }

    private synchronized String generateString(boolean generateHeader) {
        if (currentException == null) {
            return "No exception provided";
        }
        if (currentException.getStackTrace() == null) {
            return "No stacktrace provided";
        }
        if (currentException.getStackTrace().length == 0) {
            return "No any stacktrace element provided";
        }
        StringBuilder result = new StringBuilder(DEFAULT_LENGTH);
        if (generateHeader) {
            int counter = collectedExceptions.getOrDefault(compactedBody.hashCode(), NUMBER_ONE).get();
            if (counter != 1) {
                result.append("Exception ('" + compactedBody.hashCode() +
                        "') has been thrown #" + counter + " times: ");
                result.append(currentException.toString()).append("\n");
                return result.toString();
            }
            result.append("Here's a compacted exception ('" + compactedBody.hashCode() + "')");
            result.append("\n");
            result.append(compactedBody).append("\n");
            return result.toString();
        }
        result.append(currentException.toString()).append("\n");

        for (StackTraceHolder element : stackTraceElements) {
            result.append("\tat ").append(element.toString()).append("\n");
        }
        Throwable cause = currentException.getCause();
        while (cause != null) {
            StackTraceCompacter innerShortener = new StackTraceCompacter(cause);
            result.append("Caused by: \n");
            String inner = innerShortener.generateString();
            result.append(inner);
            if (cause == cause.getCause()) {
                break;
            }
            cause = cause.getCause();
        }
        return result.toString();
    }
}