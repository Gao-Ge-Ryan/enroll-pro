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

/**
 * <p>
 *     用于确定对象字段的值。
 *     若要进行复制操作的源或目标对象中具有需要特别处理的字段（或无法使用常规方式复制），可以实现该接口来进行特别处理。
 *     该接口 {@link #resolve(Object, String, Object)} 方法接收 源对象、源字段名、源字段值，并由具体实现决定最终返回何值以复制给目标对象。
 * </p>
 *
 * @author gaogle
 * @since 1.0.0
 * @see FieldTypeValueResolver
 * @see FieldNameValueResolver
 */
public interface FieldValueResolver {

    Object resolve(Object source, String fieldName, Object fieldValue);

}
