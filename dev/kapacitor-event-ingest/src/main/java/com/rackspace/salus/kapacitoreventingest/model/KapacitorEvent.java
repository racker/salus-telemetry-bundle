
/*
 * Copyright 2019 Rackspace US, Inc.
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
 * limitations under the License.
 */

package com.rackspace.salus.kapacitoreventingest.model;

import java.util.Date;

@lombok.Data
public class KapacitorEvent {

    public String id;
    public String message;
    public String details;
    public Date time;
    public Long duration;
    public String level;
    public String previousLevel;
    public Data data;
    public Boolean recoverable;

}
