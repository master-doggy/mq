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

package qunar.tc.qmq.consumer.pull;

import java.util.concurrent.ThreadLocalRandom;

class WeightPullStrategy implements PullStrategy {
    private static final int MIN_WEIGHT = 1;
    private static final int MAX_WEIGHT = 32;

    private int currentWeight = MAX_WEIGHT;

    public boolean needPull() {
        return randomWeightThreshold() < currentWeight;
    }

    private int randomWeightThreshold() {
        return ThreadLocalRandom.current().nextInt(0, MAX_WEIGHT);
    }

    public void record(boolean status) {
        if (status) {
            int weight = currentWeight * 2;
            currentWeight = Math.min(weight, MAX_WEIGHT);
        } else {
            int weight = currentWeight / 2;
            currentWeight = Math.max(weight, MIN_WEIGHT);
        }
    }
}
