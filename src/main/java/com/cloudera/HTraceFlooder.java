/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.cloudera;

import org.apache.htrace.HTraceConfiguration;
import org.apache.htrace.Sampler;
import org.apache.htrace.SpanReceiver;
import org.apache.htrace.SpanReceiverBuilder;
import org.apache.htrace.Trace;
import org.apache.htrace.TraceScope;
import org.apache.htrace.impl.AlwaysSampler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * TracerFlooder is a standalone test process which floods the system with
 * traces.
 */
public class HTraceFlooder {
  private static void usage(int exitCode) {
    System.out.println(
      "HTraceFlooder: a standalone test which floods htraced with traces.\n" +
      "\n" +
      "Parameters:\n" +
      "-Dflooder.spans.per.sec  The number of spans per second to send.\n" +
      "\n" + 
      "All parameters which start with 'htrace.flooder.' will be passed\n" +
      "through directly as HTraceConfiguration parameters.\n"
    );
    System.exit(exitCode);
  }

  static int getPositiveIntOrDie(String key) {
    String val = System.getProperty(key);
    if (val == null) {
      System.err.println("You must set " + key + " to a positive 32-bit number.");
      usage(1);
    }
    int ret = Integer.parseInt(val);
    if (ret <= 0) {
      System.err.println("You must set " + key + " to a positive 32-bit number.");
      usage(1);
    }
    return ret;
  }

  private static class FlooderHTraceConfiguration extends HTraceConfiguration {
    public String get(String key) {
      return System.getProperty("htrace.flooder." + key);
    }

    public String get(String key, String defaultValue) {
      String val = get(key);
      if (val == null) {
        return defaultValue;
      }
      return val;
    }
  }

  private static void generateSpans(int spansPerSec, Sampler sampler)
      throws Exception {
    long prevSecond = 0;
    long curSpansPerSec = 0;
    long curNs = System.nanoTime();
    long prevMinute = TimeUnit.MINUTES.
            convert(curNs, TimeUnit.NANOSECONDS);
    long spansSinceLastLogMessage = 0;
    while (true) {
      curNs = System.nanoTime();
      long curMinute = TimeUnit.MINUTES.
            convert(curNs, TimeUnit.NANOSECONDS);
      if (curMinute != prevMinute) {
        prevMinute = curMinute;
        System.out.println("Created " + spansSinceLastLogMessage + " spans.");
        spansSinceLastLogMessage = 0;
      }
      while (curSpansPerSec >= spansPerSec) {
        long curSecond = TimeUnit.SECONDS.
            convert(curNs, TimeUnit.NANOSECONDS);
        if (curSecond != prevSecond) {
          curSpansPerSec = 0;
          prevSecond = curSecond;
        } else {
          long futureNs = TimeUnit.NANOSECONDS.
              convert(curSecond + 1, TimeUnit.SECONDS);
          long durationNs = futureNs - curNs;
          long durationMs = TimeUnit.MILLISECONDS.
              convert(durationNs, TimeUnit.NANOSECONDS);
          Thread.sleep(durationMs);
          curNs = System.nanoTime();
        }
      }
      String randomName = UUID.randomUUID().toString();
      TraceScope scope = Trace.startSpan(randomName, sampler);
      scope.close();
      curSpansPerSec++;
      spansSinceLastLogMessage++;
    }
  }

  public static void main(String[] args) throws Exception {
    int numSpansPerSec = getPositiveIntOrDie("flooder.spans.per.sec");
    FlooderHTraceConfiguration conf = new FlooderHTraceConfiguration();
    SpanReceiver receiver = new SpanReceiverBuilder(conf).build();
    if (receiver == null) {
      System.out.println("failed to create SpanReceiver.");
      System.exit(1);
    }
    Trace.addReceiver(receiver);
    generateSpans(numSpansPerSec, AlwaysSampler.INSTANCE);
  }
}
