package di.container.oksianiuk.anton.injection;

import di.container.oksianiuk.anton.annotation.Inject;
import di.container.oksianiuk.anton.exception.BindingNotFoundException;
import di.container.oksianiuk.anton.exception.ConstructorNotFoundException;
import di.container.oksianiuk.anton.exception.TooManyConstructorsException;
import di.container.oksianiuk.anton.util.ClassFinder;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class InjectorImpl implements Injector {

    private static InjectorImpl INJECTOR_IMPL = new InjectorImpl();
    private static Map<Class, Class> singleToneBeans = new ConcurrentHashMap<>();

    public static InjectorImpl getInstance() {
        return INJECTOR_IMPL;
    }

    private static Map<Class, Class> interfaceToImplementations = new ConcurrentHashMap<>();

    @Override
    public <T> Provider<T> getProvider(Class<T> type) throws InvocationTargetException, InstantiationException, IllegalAccessException {

        Object bean = null;
        Class<? extends T> implementationClass;

        if (singleToneBeans.containsKey(type)){

            return new Provider<T>() {
                @Override
                public T getInstance() {
                    return (T) singleToneBeans.get(type);
                }
            };


        } else {
            implementationClass = type;
            if (implementationClass.isInterface()) {
                implementationClass = getImplementationClass(implementationClass);
            }

            List<Constructor> constructorList = Arrays.stream(implementationClass
                            .getDeclaredConstructors())
                    .filter(constructor -> constructor.isAnnotationPresent(Inject.class))
                    .collect(Collectors.toList());

            if (constructorList.size() > 1) {
                throw new TooManyConstructorsException("Your annotations Inject more than 1 in the class");
            } else if (constructorList.size() < 1) {
                try {
                    bean = implementationClass.getDeclaredConstructor().newInstance();
                } catch (Exception e) {
                    throw new ConstructorNotFoundException("Your default constructor was not found");
                }

            } else {

                Constructor constructor = constructorList.get(0);
                Class[] parameters = constructor.getParameterTypes();

                List<Object> completeParam = new ArrayList<>();

                for (Class parametr : parameters) {
                    completeParam.add(INJECTOR_IMPL.getProvider(parametr).getInstance());
                }
                bean = constructor.newInstance(completeParam.toArray());
            }

            T finalBean = (T) bean;
            return new Provider<T>() {
                @Override
                public T getInstance() {
                    return finalBean;
                }
            };
        }
    }

    @Override
    public <T> void bind(Class<T> intf, Class<? extends T> impl) {
        interfaceToImplementations.put(intf, impl);
    }

    @Override
    public <T> void bindSingleton(Class<T> intf, Class<? extends T> impl) {
        singleToneBeans.put(intf, impl);
    }

    private static <T> Class<? extends T> getImplementationClass(Class<T> interfaceClass) {
        return interfaceToImplementations.computeIfAbsent(interfaceClass, clazz -> {
            Set<Class<? extends T>> implementationClasses = ClassFinder.getSubTypesOf(interfaceClass);

            if (implementationClasses.size() > 1) {
                throw new RuntimeException("Interface has more than 1 implementations");
            } else if (implementationClasses.size() == 0) throw new BindingNotFoundException();

            return implementationClasses.stream().findFirst().get();
        });
    }
}