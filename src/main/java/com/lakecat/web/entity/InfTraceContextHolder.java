package com.lakecat.web.entity;

/**
 * @author wuyan
 * @date 2020/05/11
 **/
public class InfTraceContextHolder {

    private static final String GCP = "cloud_sgt";


    private static ThreadLocal <InfWebContext> INF_WEB_CONTEXT = new ThreadLocal <>();

    static {
        INF_WEB_CONTEXT.set(new InfWebContext());
    }

    public static synchronized InfWebContext get() {
        if (INF_WEB_CONTEXT.get() == null) {
            INF_WEB_CONTEXT.set(new InfWebContext());
        }
        return INF_WEB_CONTEXT.get();
    }

    public static void set(InfWebContext infWebContext) {
        INF_WEB_CONTEXT.set(infWebContext);
    }

    public static void remove() {
        INF_WEB_CONTEXT.remove();
    }

    public static boolean isEmpty() {
        InfWebContext infWebContext = INF_WEB_CONTEXT.get();
        return infWebContext == null;
    }

    public static boolean gcp(){
        return true;//GCP.equalsIgnoreCase(get().getEnv());
    }
}
