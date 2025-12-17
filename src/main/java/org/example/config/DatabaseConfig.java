package org.example.config;

import io.github.cdimascio.dotenv.Dotenv;

public class DatabaseConfig {

    private static final Dotenv dotenv = Dotenv.configure()
            .ignoreIfMissing()
            .load();

    public static final String DB_HOST = dotenv.get("DB_HOST");
    public static final String DB_PORT = dotenv.get("DB_PORT");
    public static final String DB_NAME = dotenv.get("DB_NAME");
    public static final String DB_USER = dotenv.get("DB_USER");
    public static final String DB_PASSWORD = dotenv.get("DB_PASSWORD");

    public static final String DB_URL =
            "jdbc:postgresql://" + DB_HOST + ":" + DB_PORT + "/" + DB_NAME;

    public static final String DB_DRIVER = "org.postgresql.Driver";

    private DatabaseConfig() {}
}
