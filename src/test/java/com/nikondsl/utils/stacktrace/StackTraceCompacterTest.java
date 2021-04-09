package com.nikondsl.utils.stacktrace;

import com.nikondsl.utils.stacktrace.factory.CompacterFactory;
import com.nikondsl.utils.stacktrace.impl.StackTraceCompacter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class StackTraceCompacterTest {
    private final StackTraceElement[] EMPTY = new StackTraceElement[0];
    private StackTraceCompacter compacter;

    private StackTraceElement[] trace3 = new StackTraceElement[] {
            new StackTraceElement("org.apache.coyote.AbstractProcessorLight", "process", "AbstractProcessorLight.java", 66),
            new StackTraceElement("org.apache.tomcat.util.net.AbstractEndpoint", "processSocket", "AbstractEndpoint.java", 1050),
            new StackTraceElement("org.apache.catalina.connector.CoyoteAdapter", "service", "CoyoteAdapter.java", 342),
    };

    private StackTraceElement[] trace2 = new StackTraceElement[] {
            new StackTraceElement("com.sdl.dxa.modelservice.service.ContentService", "loadPageContent", "ContentService.java", 175),
            new StackTraceElement("sun.reflect.GeneratedMethodAccessor341", "invoke", null, 0),
            new StackTraceElement("sun.reflect.DelegatingMethodAccessorImpl", "invoke", null, 0),
            new StackTraceElement("java.lang.reflect.Method", "invoke", null, 0),
            new StackTraceElement("org.springframework.aop.support.AopUtils", "invokeJoinpointUsingReflection", "AopUtils.java", 333),
            new StackTraceElement("org.springframework.aop.framework.ReflectiveMethodInvocation", "invokeJoinpoint", "ReflectiveMethodInvocation.java", 190),
            new StackTraceElement("org.springframework.aop.framework.CglibAopProxy$CglibMethodInvocation", "invokeJoinpoint", "CglibAopProxy.java", 723),
            new StackTraceElement("org.springframework.aop.framework.ReflectiveMethodInvocation", "proceed", "ReflectiveMethodInvocation.java", 157),
            new StackTraceElement("org.springframework.aop.framework.CglibAopProxy$DynamicAdvisedInterceptor", "intercept", "CglibAopProxy.java", 655),
            new StackTraceElement("com.sdl.dxa.modelservice.service.ContentService$$EnhancerBySpringCGLIB$$4ce1056c", "loadPageContent", null, 0),
            new StackTraceElement("com.sdl.dxa.modelservice.service.DefaultPageModelService", "_expandIncludePages", "DefaultPageModelService.java", 210),
            new StackTraceElement("com.sdl.dxa.modelservice.service.DefaultPageModelService", "_processR2PageModel", "DefaultPageModelService.java", 172),
            new StackTraceElement("com.sdl.dxa.modelservice.service.DefaultPageModelService", "loadPageModel", "DefaultPageModelService.java", 108),
            new StackTraceElement("com.sdl.dxa.modelservice.controller.PageModelController", "getPage", "PageModelController.java", 103),
            new StackTraceElement("sun.reflect.GeneratedMethodAccessor123", "invoke", null, 0),
            new StackTraceElement("sun.reflect.DelegatingMethodAccessorImpl", "invoke", null, 0),
            new StackTraceElement("sun.reflect.Method", "invoke", null, 0),
            new StackTraceElement("org.springframework.web.method.support.InvocableHandlerMethod", "doInvoke", "InvocableHandlerMethod.java", 221),
            new StackTraceElement("org.springframework.web.method.support.InvocableHandlerMethod", "invokeForRequest", "InvocableHandlerMethod.java", 135),
            new StackTraceElement("org.apache.coyote.AbstractProcessorLight", "process", "AbstractProcessorLight.java", 66),
            new StackTraceElement("org.apache.tomcat.util.net.AbstractEndpoint", "processSocket", "AbstractEndpoint.java", 1050),
            new StackTraceElement("org.apache.catalina.connector.CoyoteAdapter", "service", "CoyoteAdapter.java", 342),
    };

    private StackTraceElement[] trace1;
    private StackTraceElement[] trace1Same;

    @Before
    public void setUp() {
        List<StackTraceElement> list = new ArrayList<>(Arrays.asList(trace2));
        list.add(new StackTraceElement("java.lang.Thread", "run", null, 0));
        trace1 = list.toArray(EMPTY);
        trace1Same = list.toArray(EMPTY);
    }

    @Test
    public void testFullStacktraceStartedNotFromCompactingThing() {
        Exception cause = new Exception();
        cause.setStackTrace(trace1);
        compacter = new StackTraceCompacter(cause);
        String shortenedStacktrace = compacter.generateString();

        assertTrue(shortenedStacktrace.contains("com.sdl.dxa.modelservice."));
        assertFalse(shortenedStacktrace.contains("org.springframework"));

        assertTrue(shortenedStacktrace.contains("Spring"));
        assertTrue(shortenedStacktrace.contains("Reflection"));
        assertTrue(shortenedStacktrace.contains("Tomcat"));
    }

    @Test
    public void testFullStacktraceStartedFromCompactingThing() {
        Exception cause = new Exception();
        cause.setStackTrace(trace2);
        compacter = new StackTraceCompacter(cause);
        compacter.addRuleToBeLeftExpanded("SDL", new String[] {"com.sdl.", "org.dd4t."});
        String shortenedStacktrace = compacter.generateString();

        assertTrue(shortenedStacktrace.contains("Tomcat"));
    }

    @Test
    public void testFullStacktraceOnlyCompactingThing() {
        Exception cause = new Exception();
        cause.setStackTrace(trace3);
        compacter = new StackTraceCompacter(cause);
        String shortenedStacktrace = compacter.generateString();

        assertTrue(shortenedStacktrace.contains("Tomcat"));
    }

    @Test
    public void testExc() {
        try{
            IllegalStateException ise = new IllegalStateException("keep off this");
            throw new IllegalArgumentException("Note", ise);
        } catch (Exception ex) {
            compacter = new StackTraceCompacter(ex);
            System.err.println(compacter.generateString());
        }
    }

    @Test
    public void testSameSacktraceHeader() {
        Exception causeFirst = new Exception();
        causeFirst.setStackTrace(trace1);
        compacter = new StackTraceCompacter();
        compacter.init(causeFirst);
        String shortenedStacktrace = compacter.generateString();
        assertTrue(shortenedStacktrace.startsWith("Here's a compacted exception ('"));
        //remember id
        Matcher matcher = Pattern.compile(".*?\\('([+-]?\\d++)'\\).*").matcher(shortenedStacktrace);
        String id = "";
        if (matcher.find()) {
            id = matcher.group(1);
        }

        Exception causeSecond = new Exception();
        causeSecond.setStackTrace(trace1Same);
        compacter.init(causeSecond);

        shortenedStacktrace = compacter.generateString();
        assertTrue(shortenedStacktrace.startsWith("Exception ('" + id + "') has been thrown #2 times"));
    }
}