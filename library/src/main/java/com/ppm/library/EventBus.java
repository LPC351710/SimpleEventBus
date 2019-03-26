package com.ppm.library;

import android.os.Handler;
import android.os.Looper;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class EventBus {
    private static EventBus instance = new EventBus();

    private Map<Object, List<SubscribeMethod>> cached;
    private Handler mainHandler;
    private ExecutorService executorService;

    public static EventBus getDefault() {
        return instance;
    }

    private EventBus() {
        cached = new HashMap<>();
        mainHandler = new Handler(Looper.getMainLooper());
        executorService = Executors.newCachedThreadPool();
    }

    public void register(Object target) {
        List<SubscribeMethod> list = cached.get(target);
        if (list == null) {
            list = findSubscribeMethod(target);
            cached.put(target, list);
        }
    }

    public void post(final Object event) {
        //遍历cache找到SubscribeMethod
        Set<Object> set = cached.keySet();
        Iterator iterator = set.iterator();
        while (iterator.hasNext()) {
            final Object target = iterator.next();
            List<SubscribeMethod> list = cached.get(target);
            for (final SubscribeMethod subscribeMethod : list) {
                if (subscribeMethod.getEventType().isAssignableFrom(event.getClass())) {
                    switch (subscribeMethod.getThreadMode()) {
                        case MAIN:
                            if (Looper.myLooper() == Looper.getMainLooper()) {
                                invoke(target, event, subscribeMethod);
                            } else {
                                postMain(target, event, subscribeMethod);
                            }
                            break;
                        case ASYNC:
                            if (Looper.myLooper() == Looper.getMainLooper()) {
                                postAsync(target, event, subscribeMethod);
                            } else {
                                invoke(target, event, subscribeMethod);
                            }
                            break;
                        case POST_THREAD:
                            invoke(target, event, subscribeMethod);
                            break;
                    }
                }
            }
        }
    }

    private void postAsync(final Object target, final Object event, final SubscribeMethod subscribeMethod) {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                invoke(target, event, subscribeMethod);
            }
        });
    }

    private void postMain(final Object target, final Object event, final SubscribeMethod subscribeMethod) {
        mainHandler.post(new Runnable() {
            @Override
            public void run() {
                invoke(target, event, subscribeMethod);
            }
        });
    }

    private void invoke(Object target, Object event, SubscribeMethod subscribeMethod) {
        try {
            Method method = subscribeMethod.getMethod();
            method.invoke(target, event);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private List<SubscribeMethod> findSubscribeMethod(Object target) {
        List<SubscribeMethod> list = new ArrayList<>();
        Class clazz = target.getClass();
        while (clazz != null) {
            String name = clazz.getName();
            if (name.startsWith("java.") || name.startsWith("javax.")
                    || name.startsWith("android.")) {
                break;
            }

            Method[] methods = clazz.getDeclaredMethods();
            for (Method method : methods) {
                if (!method.isAnnotationPresent(Subscribe.class)) {
                    continue;
                }
                Subscribe subscribe = method.getAnnotation(Subscribe.class);
                Class[] paramTypes = method.getParameterTypes();
                if (paramTypes.length != 1) {
                    throw new RuntimeException("subscribe Method can only accept one param");
                }

                ThreadMode threadMode = subscribe.threadMode();

                SubscribeMethod subscribeMethod = new SubscribeMethod(method, threadMode, paramTypes[0]);
                list.add(subscribeMethod);
            }

            clazz = clazz.getSuperclass();
        }
        return list;
    }


    public void unRegister(Object target) {

    }
}
