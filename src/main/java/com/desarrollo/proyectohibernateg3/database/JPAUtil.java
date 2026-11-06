package com.desarrollo.proyectohibernateg3.database;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;

import java.util.Map;

public final class JPAUtil {
    private static EntityManagerFactory entityManagerFactory;

    private JPAUtil() {
    }

    public static synchronized void iniciar() {
        if (entityManagerFactory == null || !entityManagerFactory.isOpen()) {
            String password = leerVariable("DB_PASSWORD");

            Map<String, Object> propiedades = Map.of(
                    "jakarta.persistence.jdbc.password", password
            );
            entityManagerFactory = Persistence.createEntityManagerFactory("bibliotecaPU", propiedades);
        }
    }

    public static synchronized EntityManager getEntityManager() {
        iniciar();
        return entityManagerFactory.createEntityManager();
    }

    public static synchronized void cerrar() {
        if (entityManagerFactory != null) {
            if (entityManagerFactory.isOpen()) {
                entityManagerFactory.close();
            }
            entityManagerFactory = null;
        }
    }

    private static String leerVariable(String nombre) {
        String valor = System.getenv(nombre);
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("La variable de entorno " + nombre + " está vacía o no está definida.");
        }
        return valor;
    }
}
