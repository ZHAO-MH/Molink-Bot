package com.zhaomh;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context = new ApplicationContext(getWsuRrl());

        Runtime.getRuntime().addShutdownHook(new Thread(context::shutdown));

        context.start();
    }

    private static String getWsuRrl() {
        return "ws://" + System.getenv().getOrDefault("HOST", "0.0.0.0")
                + ":"
                + System.getenv().getOrDefault("PORT", "3001")
                + "?access_token=" + System.getenv().getOrDefault("TOKEN", "java_zhaomh");
    }
}