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

package qunar.tc.qmq.common;

import io.netty.util.HashedWheelTimer;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;
import qunar.tc.qmq.concurrent.NamedThreadFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author yiqun.fan create on 17-11-3.
 */
public class TimerUtil {
    private static final HashedWheelTimer TIMER = new HashedWheelTimer(new NamedThreadFactory("qmq-timer"));

    public static Timeout newTimeout(TimerTask task, long delay, TimeUnit unit) {
        return TIMER.newTimeout(task, delay, unit);
    }
}
