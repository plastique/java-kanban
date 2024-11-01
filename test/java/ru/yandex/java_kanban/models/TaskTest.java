package ru.yandex.java_kanban.models;

import org.junit.jupiter.api.Test;
import ru.yandex.java_kanban.enums.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class TaskTest {
    @Test
    void sameTaskWithSameId() {
        Task task1 = new Task(
                "task 1",
                "descr 1",
                TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 27, 10, 0),
                Duration.ofMinutes(10)
        );
        Task task2 = new Task(
                "task 2",
                "descr 2",
                TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 27, 14, 0),
                Duration.ofMinutes(25)
        );

        task1.setId(1);
        task2.setId(task1.getId());

        assertEquals(task1, task2, "ID задач не совпадают");
        assertNotEquals(task1.toString(), task2.toString(), "Задачи одинаковые");
    }

    @Test
    void differentTaskWithDifferentId() {
        Task task1 = new Task(
                "task 1",
                "descr 1",
                TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 27, 10, 0),
                Duration.ofMinutes(20)
        );
        Task task2 = new Task(
                "task 2",
                "descr 2",
                TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 27, 11, 0),
                Duration.ofMinutes(25)
        );

        task1.setId(1);
        task2.setId(2);

        assertNotEquals(task1, task2, "ID задач совпадают");
        assertNotEquals(task1.toString(), task2.toString(), "Задачи одинаковые");
    }
}
