package ru.yandex.java_kanban.exceptions;

public class TaskIntersectionException extends RuntimeException {
    public TaskIntersectionException(String message) {
        super(message);
    }
}
