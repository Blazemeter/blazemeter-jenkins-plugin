/**
 * Copyright 2018 BlazeMeter Inc.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package hudson.plugins.blazemeter;

import com.blazemeter.api.explorer.test.AbstractTest;

import java.io.Serializable;
import java.util.Comparator;

public class AbstractTestComparator implements Comparator<AbstractTest>, Serializable {
    @Override
    public int compare(AbstractTest t1, AbstractTest t2) {
        return t1.getName().compareToIgnoreCase(t2.getName());
    }

}
