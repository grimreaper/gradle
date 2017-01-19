/*
 * Copyright 2017 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.api.internal.tasks;

import org.gradle.api.internal.TaskOutputCaching;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Strings.isNullOrEmpty;

class DefaultTaskOutputCaching implements TaskOutputCaching {
    private final boolean cacheable;
    private final String disabledReason;

    static final TaskOutputCaching CACHEABLE = new DefaultTaskOutputCaching(true, null);
    static TaskOutputCaching notCacheable(String disabledReason) {
        return new DefaultTaskOutputCaching(false, disabledReason);
    }

    private DefaultTaskOutputCaching(boolean cacheable, String disabledReason) {
        if (cacheable) {
            checkArgument(disabledReason == null, "disabledReason must be null if task is cacheable");
        } else {
            checkArgument(!isNullOrEmpty(disabledReason), "disabledReason must be set if task is not cacheable");
        }
        this.cacheable = cacheable;
        this.disabledReason = disabledReason;
    }

    @Override
    public boolean isEnabled() {
        return cacheable;
    }

    @Override
    public String getDisabledReason() {
        return disabledReason;
    }
}
