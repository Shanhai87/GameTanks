module Test {
    requires javafx.controls;
    requires javafx.graphics;
    requires spring.jdbc;
    requires java.sql;
    requires spring.beans;
    requires spring.context;
    requires spring.security.crypto;
    requires org.json;
    requires com.zaxxer.hikari;
    requires spring.core;
    opens edu.school21.game.app;
    opens edu.school21.game.config;
    opens edu.school21.game.server;
    opens edu.school21.game.models;
    opens edu.school21.game.repositories;
    opens edu.school21.game.services;
}