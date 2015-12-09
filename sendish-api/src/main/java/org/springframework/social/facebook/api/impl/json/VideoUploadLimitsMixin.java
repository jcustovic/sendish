/*
 * Copyright 2015 the original author or authors.
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
package org.springframework.social.facebook.api.impl.json;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Annotated mixin to add Jackson annotations to VideoUploadLimits. 
 * @author Craig Walls
 *
 * FIXME: This is placed here until they release spring-social-facebook 2.0.3.RELEASE. After new realse delete this class!
 * Bug: https://github.com/spring-projects/spring-social-facebook/issues/181
 *      https://github.com/spring-projects/spring-social-facebook/pull/183
 */
@JsonIgnoreProperties(ignoreUnknown = true)
abstract class VideoUploadLimitsMixin extends FacebookObjectMixin {

    @JsonCreator
    VideoUploadLimitsMixin(
            @JsonProperty("length") long length,
            @JsonProperty("size") long size) {}

}