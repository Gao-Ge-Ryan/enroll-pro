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

package top.gaogle.framework.interceptor;


import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;
import top.gaogle.framework.pojo.QueryingArgument;
import top.gaogle.framework.pojo.SuperQuerying;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author gaogle
 * @since 2.0.0
 */
public class QueryingArgumentInterceptor implements MethodInterceptor {

    private static final Set<Method> QUERYING_METHOD = new HashSet<>();

    static {
        QUERYING_METHOD.addAll(Arrays.asList(SuperQuerying.class.getMethods()));
    }

    private final QueryingArgument querying;

    public QueryingArgumentInterceptor(QueryingArgument querying) {
        this.querying = querying;
    }

    @Override
    public Object intercept(Object target, Method method, Object[] args, MethodProxy methodProxy) throws Throwable {
        Object result;
        if (QUERYING_METHOD.contains(method)) {
            result = method.invoke(querying, args);
        } else {
            result = methodProxy.invokeSuper(target, args);
        }
        return result;
    }
}
