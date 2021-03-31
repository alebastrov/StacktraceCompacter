# StacktraceCompacter
Build status: ![Java CI with Maven](https://github.com/alebastrov/StacktraceCompacter/workflows/Java%20CI%20with%20Maven/badge.svg)

This utility class provides a way to have your stacktraces compacted (for instance all rows from hibernate will be packet into single one) so instead of this
~~~
java.lang.IllegalArgumentException: Note
	at com.nikondsl.utils.stacketrace.StackTraceCompacterTest.testExc(StacktraceCompacterTest.java:100)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:567)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.internal.runners.statements.RunBefores.evaluate(RunBefores.java:26)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)
	at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
	at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
	at org.mockito.internal.runners.DefaultInternalRunner$1.run(DefaultInternalRunner.java:79)
	at org.mockito.internal.runners.DefaultInternalRunner.run(DefaultInternalRunner.java:85)
	at org.mockito.internal.runners.StrictRunner.run(StrictRunner.java:39)
	at org.mockito.junit.MockitoJUnitRunner.run(MockitoJUnitRunner.java:163)
	at org.junit.runner.JUnitCore.run(JUnitCore.java:137)
	at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:68)
	at com.intellij.rt.execution.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:47)
	at com.intellij.rt.execution.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:242)
	at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:70)
java.lang.IllegalStateException: keep off this
	at com.nikondsl.utils.stacketrace.StackTraceCompacterTest.testExc(StacktraceCompacterTest.java:99)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at java.base/jdk.internal.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at java.base/jdk.internal.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.base/java.lang.reflect.Method.invoke(Method.java:567)
	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
	at org.junit.internal.runners.statements.RunBefores.evaluate(RunBefores.java:26)
	at org.junit.runners.ParentRunner.runLeaf(ParentRunner.java:325)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:78)
	at org.junit.runners.BlockJUnit4ClassRunner.runChild(BlockJUnit4ClassRunner.java:57)
	at org.junit.runners.ParentRunner$3.run(ParentRunner.java:290)
	at org.junit.runners.ParentRunner$1.schedule(ParentRunner.java:71)
	at org.junit.runners.ParentRunner.runChildren(ParentRunner.java:288)
	at org.junit.runners.ParentRunner.access$000(ParentRunner.java:58)
	at org.junit.runners.ParentRunner$2.evaluate(ParentRunner.java:268)
	at org.junit.runners.ParentRunner.run(ParentRunner.java:363)
	at org.mockito.internal.runners.DefaultInternalRunner$1.run(DefaultInternalRunner.java:79)
	at org.mockito.internal.runners.DefaultInternalRunner.run(DefaultInternalRunner.java:85)
	at org.mockito.internal.runners.StrictRunner.run(StrictRunner.java:39)
	at org.mockito.junit.MockitoJUnitRunner.run(MockitoJUnitRunner.java:163)
  ~~~
  you will see in logs
  ~~~
Here's a compacted exception('636226618')
  java.lang.IllegalArgumentException: Note
	at com.nikondsl.utils.stacketrace.StackTraceCompacterTest.testExc(StacktraceCompacterTest.java:100)
	   -- REFLECTION
	   -- JUnit
	   -- Mockito
	   -- JUnit
	at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:68)
	at com.intellij.rt.execution.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:47)
	at com.intellij.rt.execution.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:242)
	at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:70)
Caused by: java.lang.IllegalStateException: keep off this
	at com.nikondsl.utils.stacketrace.StackTraceCompacterTest.testExc(StacktraceCompacterTest.java:99)
	   -- REFLECTION
	   -- JUnit
	   -- Mockito
	   -- JUnit
	at com.intellij.junit4.JUnit4IdeaTestRunner.startRunnerWithArgs(JUnit4IdeaTestRunner.java:68)
	at com.intellij.rt.execution.junit.IdeaTestRunner$Repeater.startRunnerWithArgs(IdeaTestRunner.java:47)
	at com.intellij.rt.execution.junit.JUnitStarter.prepareStreamsAndStart(JUnitStarter.java:242)
	at com.intellij.rt.execution.junit.JUnitStarter.main(JUnitStarter.java:70)
  ~~~


Moreover if your exception is met in stacktrace several times, you may get it even more compacted!
See (in case previous exception had been thrown before that one) below
~~~
Exception ('636226618') has been thrown #2 times: java.lang.IllegalArgumentException: Note
~~~

How to deal with it:

You can compact an exception by doing
~~~
//catch an exception
Exception ex = ...
//create a compacter and pass the exception to it
StacktraceCompacter shortener = CompacterFactory.create();
shortener.init(ex);
//get stacktrace compacted and pass it to a logger
log.warn("This opeation did not finish", shortener.generateString());
~~~

or (in this case it also is able to compact the same exceptions with the single line)
~~~
//catch an exception
Exception ex = ...
//reuse a compacter and pass the exception to it
StacktraceCompacter shortener = CompacterFactory.getInstance();
shortener.init(ex);
//get stacktrace compacted and pass it to a logger
log.warn("This opeation did not finish", shortener.generateString());
~~~
