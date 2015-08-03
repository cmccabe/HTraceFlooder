HTraceFlooder
==================================

A simple test of Apache HTrace.  It sends spans to HTrace via the Java client at
a configurable rate.

USAGE
=====

Build
-----

```bash
git clone https://github.com/cmccabe/HTraceFlooder.git
cd HTraceFlooder/
mvn package
ls -l target/HTraceFlooder.jar
```

Usage
-----

If you have Hadoop installed, you can use the "hadoop jar" command to get a
CLASSPATH environment which has the jars you need.  If not, you must set up the
CLASSPATH yourself so that it contains all the htrace jar files.

```bash
~> hadoop jar target/htrace-flooder-1.0.jar
HTraceFlooder: a standalone test which floods htraced with traces.

Parameters:
-Dflooder.spans.per.sec  The number of spans per second to send.

All parameters which start with 'htrace.flooder.' will be passed
through directly as HTraceConfiguration parameters.
```

Example
-------

```bash
~> hadoop jar target/htrace-flooder-1.0.jar \
    -Dflooder.spans.per.sec=5000 \
    -Dhtrace.client.rest.batch.size=10000 \
    -Dhtrace.flooder.htraced.rest.url=http://a2402.halxg.cloudera.com:9095 \
    -Dhtrace.flooder.client.rest.period.ms=30000 \
    -Dhtrace.flooder.span.receiver=org.apache.htrace.impl.HTracedRESTReceiver
~>
```

Colin Patrick McCabe
