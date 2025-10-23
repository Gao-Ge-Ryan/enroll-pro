/*
 * Copyright the original author or authors.
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

package top.gaogle.framework.util;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;

import java.util.*;

/**
 * @author gaogle
 * @since 1.0.0
 */
public class CopyOption {

    public static Builder builder() {
        return new Builder();
    }

    private boolean ignoreSourceNullValue;
    private Set<String> ignoredSourceFields;
    private Collection<FieldValueResolver> fieldValueResolvers;

    public boolean isIgnoreSourceNullValue() {
        return ignoreSourceNullValue;
    }

    public void setIgnoreSourceNullValue(boolean ignoreSourceNullValue) {
        this.ignoreSourceNullValue = ignoreSourceNullValue;
    }

    public Set<String> getIgnoredSourceFields() {
        return ignoredSourceFields;
    }

    public void setIgnoredSourceFields(Set<String> ignoredSourceFields) {
        this.ignoredSourceFields = ignoredSourceFields;
    }

    public Collection<FieldValueResolver> getFieldValueResolvers() {
        return fieldValueResolvers;
    }

    public void setFieldValueResolvers(Collection<FieldValueResolver> fieldValueResolvers) {
        this.fieldValueResolvers = fieldValueResolvers;
    }

    public static class Builder {
        private boolean ignoreSourceNullValue;
        private Set<String> ignoredSourceFields;
        private Collection<FieldValueResolver> fieldValueResolvers;

        public Builder ignoreSourceNullValue(boolean ignoreSourceNullValue) {
            this.ignoreSourceNullValue = ignoreSourceNullValue;
            return this;
        }

        public Builder ignoredSourceFields(String... ignoredSourceFields) {
            this.ignoredSourceFields = new HashSet<>();
            if (ArrayUtils.isNotEmpty(ignoredSourceFields)) {
                this.ignoredSourceFields.addAll(Arrays.asList(ignoredSourceFields));
            }
            return this;
        }

        public Builder fieldValueResolvers(FieldValueResolver... fieldValueResolvers) {
            if (ArrayUtils.isNotEmpty(fieldValueResolvers)) {
                this.fieldValueResolvers = Arrays.asList(fieldValueResolvers);
            } else {
                this.fieldValueResolvers = new ArrayList<>();
            }
            return this;
        }

        public Builder fieldValueResolvers(Collection<FieldValueResolver> fieldValueResolvers) {
            if (CollectionUtils.isNotEmpty(fieldValueResolvers)) {
                this.fieldValueResolvers = fieldValueResolvers;
            } else {
                this.fieldValueResolvers = new ArrayList<>();
            }
            return this;
        }

        public Builder addFieldValueResolvers(FieldValueResolver... fieldValueResolvers) {
            if (this.fieldValueResolvers == null) {
                this.fieldValueResolvers = new ArrayList<>();
            }
            if (ArrayUtils.isNotEmpty(fieldValueResolvers)) {
                this.fieldValueResolvers.addAll(Arrays.asList(fieldValueResolvers));
            }
            return this;
        }

        public Builder addFieldValueResolvers(Collection<FieldValueResolver> fieldValueResolvers) {
            if (this.fieldValueResolvers == null) {
                this.fieldValueResolvers = new ArrayList<>();
            }
            if (CollectionUtils.isNotEmpty(fieldValueResolvers)) {
                this.fieldValueResolvers.addAll(fieldValueResolvers);
            }
            return this;
        }

        public Builder addFieldValueResolver(FieldValueResolver fieldValueResolver) {
            if (this.fieldValueResolvers == null) {
                this.fieldValueResolvers = new ArrayList<>();
            }
            if (fieldValueResolver != null) {
                this.fieldValueResolvers.add(fieldValueResolver);
            }
            return this;
        }

        public CopyOption build() {
            CopyOption option = new CopyOption();
            option.setIgnoreSourceNullValue(this.ignoreSourceNullValue);
            option.setIgnoredSourceFields(this.ignoredSourceFields);
            option.setFieldValueResolvers(this.fieldValueResolvers);
            return option;
        }
    }
}
