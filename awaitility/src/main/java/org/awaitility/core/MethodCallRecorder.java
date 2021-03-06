/*
 * Copyright 2010 the original author or authors.
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
package org.awaitility.core;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;
import org.awaitility.proxy.ByteBuddyProxyCreator;
import org.awaitility.proxy.TypeUtils;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * <p>MethodCallRecorder class.</p>
 */
public class MethodCallRecorder {

    private static final String NO_METHOD_CALL_RECORDED_MESSAGE = "No method call has been recorded. Perhaps the method was final?";
    private static ThreadLocal<Object> lastTarget = new ThreadLocal<Object>();
    private static ThreadLocal<Method> lastMethod = new ThreadLocal<Method>();
    private static ThreadLocal<Object[]> lastArgs = new ThreadLocal<Object[]>();

    private static InvocationHandler invocationHandler = new InvocationHandler() {
		public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
			if (shouldBeRecorded(method)) {
				lastMethod.set(method);
				lastArgs.set(args);
			}
			return TypeUtils.getDefaultValue(method.getReturnType());
		}

		private boolean shouldBeRecorded(Method method) {
			return !(method.getDeclaringClass().equals(Object.class) && method.getName().equals("finalize"));
		}

    };

	/**
	 * Intercepts all methods executions in a proxied class feeding MethodCallRecorder.
	 */
    public static class MethodCallInterceptor {
        @RuntimeType
        public static Object interceptExecutionDetails(@Origin Method method, @AllArguments Object[] args) {
            lastMethod.set(method);
            lastArgs.set(args);
            return TypeUtils.getDefaultValue(method.getReturnType());
        }
    }

	/**
	 * <p>createProxy.</p>
	 *
	 * @param target a {@link java.lang.Object} object.
	 * @return a {@link java.lang.Object} object.
	 */
	public static Object createProxy(Object target) {
		Object proxy = ByteBuddyProxyCreator.create(target.getClass(), MethodCallInterceptor.class);
		lastTarget.set(target);
		return proxy;
	}

	/**
	 * <p>Getter for the field <code>lastTarget</code>.</p>
	 *
	 * @return a {@link java.lang.Object} object.
	 */
	public static Object getLastTarget() {
        Object target = lastTarget.get();
		if (target == null) {
			throw new IllegalStateException(NO_METHOD_CALL_RECORDED_MESSAGE);
		}
		return target;
	}

	/**
	 * <p>Getter for the field <code>lastMethod</code>.</p>
	 *
	 * @return a {@link java.lang.reflect.Method} object.
	 */
	public static Method getLastMethod() {
        Method method = lastMethod.get();
		if (method == null) {
			throw new IllegalStateException(NO_METHOD_CALL_RECORDED_MESSAGE);
		}
		return method;
	}
	
	/**
	 * <p>Getter for the field <code>lastArgs</code>.</p>
	 *
	 * @return an array of {@link java.lang.Object} objects.
	 */
	public static Object[] getLastArgs() {
        Object target = lastTarget.get();
        if (target == null) {
			throw new IllegalStateException(NO_METHOD_CALL_RECORDED_MESSAGE);
		}
		return lastArgs.get();
	}

	/**
	 * <p>reset.</p>
	 */
	public static void reset() {
		lastTarget.remove();
		lastMethod.remove();
		lastArgs.remove();
	}
	
}
