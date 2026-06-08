package com.pathshalapro.config;

public class AcademicYearContextHolder {
    private static final ThreadLocal<String> CONTEXT = new ThreadLocal<>();

    public static void set(String academicYear) {
        CONTEXT.set(academicYear);
    }

    public static String get() {
        return CONTEXT.get();
    }

    public static void clear() {
        CONTEXT.remove();
    }
}
