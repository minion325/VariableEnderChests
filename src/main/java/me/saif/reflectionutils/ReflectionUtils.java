package me.saif.reflectionutils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * A class with a bunch of methods to make it easier to work with reflection.
 */
public class ReflectionUtils {

    public static Optional<Class<?>> getClass(String path) {
        try {
            return Optional.of(Class.forName(path));
        } catch (ClassNotFoundException e) {
            return Optional.empty();
        }
    }

    public static Optional<Method> getMethod(Class<?> clazz, String name, boolean ignoreAccess, Class<?>... parameters) {
        try {
            return Optional.of(clazz.getMethod(name, parameters));
        } catch (NoSuchMethodException e) {
            if (ignoreAccess) {
                try {
                    return Optional.of(clazz.getDeclaredMethod(name, parameters));
                } catch (NoSuchMethodException ex) {
                    return Optional.empty();
                }
            }
            return Optional.empty();
        }
    }

    public static Optional<Method> getPublicMethod(Class<?> clazz, String name, Class<?>... parameters) {
        return getMethod(clazz, name, false, parameters);
    }

    public static Optional<Constructor<?>> getConstructor(Class<?> clazz, boolean ignoreAccess, Class<?>... parameters) {
        try {
            return Optional.of(clazz.getConstructor(parameters));
        } catch (NoSuchMethodException e) {
            if (ignoreAccess) {
                try {
                    return Optional.of(clazz.getDeclaredConstructor(parameters));
                } catch (NoSuchMethodException ex) {
                    return Optional.empty();
                }
            }
            return Optional.empty();
        }
    }

    public static Optional<Constructor<?>> getPublicConstructor(Class<?> clazz, Class<?>... parameters) {
        return getConstructor(clazz, false, parameters);
    }

    public static Optional<Field> getField(Class<?> clazz, String name, boolean ignoreAccess) {
        try {
            return Optional.of(clazz.getField(name));
        } catch (NoSuchFieldException e) {
            if (ignoreAccess) {
                try {
                    return Optional.of(clazz.getDeclaredField(name));
                } catch (NoSuchFieldException ex) {
                    return Optional.empty();
                }
            }
            return Optional.empty();
        }
    }

    public static Optional<Field> getPublicField(Class<?> clazz, String name) {
        return getField(clazz, name, false);
    }

    public static Optional<Class<?>> getInnerClass(Class<?> clazz, String name) {
        return getClass(clazz.getName() + "$" + name);
    }

    public static Optional<Class<?>> getClassInPackage(Package pack, String simpleName) {
        return getClass(pack.getName() + "." + simpleName);
    }

    public static Set<Field> getFields(Class<?> clazz, boolean ignoreAccess) {
        Set<Field> fields = new HashSet<>(Arrays.asList(clazz.getFields()));

        if (ignoreAccess) {
            fields.addAll(Arrays.asList(clazz.getDeclaredFields()));
        }
        return fields;
    }

    public static Set<Field> getFields(Class<?> clazz, boolean ignoreAccess, Predicate<Field> fieldPredicate) {
        return getFields(clazz, ignoreAccess).stream().filter(fieldPredicate).collect(Collectors.toSet());
    }

    public static Set<Field> getPublicFields(Class<?> clazz, Predicate<Field> fieldPredicate) {
        return getFields(clazz, false, fieldPredicate);
    }

    public static Set<Field> getFieldsOfType(Class<?> clazz, Class<?> type, boolean ignoreAccess) {
        return getFields(clazz, ignoreAccess, field -> type.isAssignableFrom(field.getType()));
    }

    public static Set<Field> getFieldsOfTypeName(Class<?> clazz, String name, boolean ignoreAccess) {
        return getFields(clazz, ignoreAccess, field -> field.getType().getSimpleName().equals(name));
    }

    public static Set<Field> getPublicFieldsOfType(Class<?> clazz, Class<?> type) {
        return getFieldsOfType(clazz, type, false);
    }

    public static Set<Method> getMethods(Class<?> clazz, boolean ignoreAccess) {
        Set<Method> methods = new HashSet<>(Arrays.asList(clazz.getMethods()));

        if (ignoreAccess) {
            methods.addAll(Arrays.asList(clazz.getDeclaredMethods()));
        }
        return methods;
    }

    public static Set<Method> getMethods(Class<?> clazz, boolean ignoreAccess, Predicate<Method> methodPredicate) {
        return getMethods(clazz, ignoreAccess).stream().filter(methodPredicate).collect(Collectors.toSet());
    }

    public static Set<Method> getPublicMethods(Class<?> clazz, Predicate<Method> methodPredicate) {
        return getMethods(clazz, false, methodPredicate);
    }

    public static Set<Method> getMethodsOfType(Class<?> clazz, Class<?> type, boolean ignoreAccess) {
        return getMethods(clazz, ignoreAccess, method -> type.isAssignableFrom(method.getReturnType()));
    }

    public static Set<Method> getPublicMethodsOfType(Class<?> clazz, Class<?> type) {
        return getMethodsOfType(clazz, type, false);
    }

    public static Set<Method> getMethodsWithParameterNames(Class<?> clazz, boolean ignoreAccess, String methodName, Predicate<Method> predicate, String... types) {
        return getMethods(clazz, ignoreAccess, method -> {
            if (predicate != null && !predicate.test(method))
                return false;

            if (!method.getName().equals(methodName))
                return false;

            if (method.getParameterCount() != types.length)
                return false;

            Class<?>[] params = method.getParameterTypes();
            for (int i = 0; i < params.length; i++) {
                if (!params[i].getSimpleName().equals(types[i]))
                    return false;
            }

            return true;
        });
    }

    public static Set<Method> getMethodsWithParameterNames(Class<?> clazz, boolean ignoreAccess, String methodName, String... types) {
        return getMethodsWithParameterNames(clazz, ignoreAccess, methodName, null, types);
    }


}
