/*
 * Copyright 2018 Qunar
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.com.qunar.pay.trade.api.card.service.usercard.UserCardQueryFacade
 */

package qunar.tc.qmq.metrics.prometheus;

import com.google.common.base.Supplier;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import io.prometheus.client.Collector;
import io.prometheus.client.Gauge;
import io.prometheus.client.SimpleCollector;
import io.prometheus.client.Summary;
import qunar.tc.qmq.metrics.QmqCounter;
import qunar.tc.qmq.metrics.QmqMeter;
import qunar.tc.qmq.metrics.QmqMetricRegistry;
import qunar.tc.qmq.metrics.QmqTimer;

import java.util.Arrays;

/**
 * @author keli.wang
 * @since 2018/11/22
 */
public class PrometheusQmqMetricRegistry implements QmqMetricRegistry {
    private static final LoadingCache<Key, Collector> CACHE = CacheBuilder.newBuilder()
            .build(new CacheLoader<Key, Collector>() {
                @Override
                public Collector load(Key key) {
                    return key.create();
                }
            });

    @SuppressWarnings("unchecked")
    private static <M extends Collector> M cacheFor(Key<M> key) {
        return (M) CACHE.getUnchecked(key);
    }

    @Override
    public void newGauge(final String name, final String[] tags, final String[] values, final Supplier<Double> supplier) {
        final PrometheusQmqGauge gauge = cacheFor(new GuageKey(name, tags));
        gauge.labels(values).setSupplier(supplier);
    }

    @Override
    public QmqCounter newCounter(final String name, final String[] tags, final String[] values) {
        final Gauge gauge = cacheFor(new CounterKey(name, tags));
        return new PrometheusQmqCounter(gauge, values);
    }

    @Override
    public QmqMeter newMeter(final String name, final String[] tags, final String[] values) {
        final Summary summary = cacheFor(new MeterKey(name, tags));
        return new PrometheusQmqMeter(summary, values);
    }

    @Override
    public QmqTimer newTimer(final String name, final String[] tags, final String[] values) {
        final Summary summary = cacheFor(new TimerKey(name, tags));
        return new PrometheusQmqTimer(summary, values);
    }

    @Override
    public void remove(final String name, final String[] tags, final String[] values) {
        // TODO(keli.wang): only remove child collectors for now, may we should remove whole metric in the future
        final SimpleCollector collector = cacheFor(new SimpleCollectorKey(name, tags));
        collector.remove(values);
    }

    private static abstract class Key<M extends Collector> {
        final String name;
        final String[] tags;

        Key(String name, String[] tags) {
            this.name = name;
            this.tags = tags;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o)
                return true;
            if (o == null || getClass() != o.getClass())
                return false;

            Key key = (Key) o;

            if (name != null ? !name.equals(key.name) : key.name != null)
                return false;
            return Arrays.equals(tags, key.tags);
        }

        @Override
        public int hashCode() {
            int result = name != null ? name.hashCode() : 0;
            result = 31 * result + (tags != null ? Arrays.hashCode(tags) : 0);
            return result;
        }

        public abstract M create();
    }

    private static class SimpleCollectorKey extends Key<SimpleCollector> {

        SimpleCollectorKey(final String name, final String[] tags) {
            super(name, tags);
        }

        @Override
        public SimpleCollector create() {
            throw new UnsupportedOperationException("cannot create simple collector");
        }
    }

    private static class GuageKey extends Key<PrometheusQmqGauge> {
        GuageKey(final String name, final String[] tags) {
            super(name, tags);
        }

        @Override
        public PrometheusQmqGauge create() {
            return PrometheusQmqGauge.build().name(name).labelNames(tags).create().register();
        }
    }

    private static class CounterKey extends Key<Gauge> {
        CounterKey(final String name, final String[] tags) {
            super(name, tags);
        }

        @Override
        public Gauge create() {
            return Gauge.build().name(name).labelNames(tags).create().register();
        }
    }

    private static class MeterKey extends Key<Summary> {

        MeterKey(final String name, final String[] tags) {
            super(name, tags);
        }

        @Override
        public Summary create() {
            return Summary.build().name(name).labelNames(tags).create().register();
        }
    }

    private static class TimerKey extends Key<Summary> {

        TimerKey(final String name, final String[] tags) {
            super(name, tags);
        }

        @Override
        public Summary create() {
            return Summary.build()
                    .name(name)
                    .labelNames(tags)
                    .quantile(0.5, 0.05)
                    .quantile(0.75, 0.05)
                    .quantile(0.99, 0.05)
                    .create()
                    .register();
        }
    }
}
