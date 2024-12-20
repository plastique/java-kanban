package ru.yandex.java_kanban.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.java_kanban.enums.TaskStatus;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class SubtaskTest {
    Epic epic;

    @BeforeEach
    public void setUp() {
        epic = new Epic("epic", "epic description");
        epic.setId(1);
    }

    @Test
    void sameSubtaskWithSameId() {
        Subtask subtask1 = new Subtask(
                "subtask 1",
                "descr 1",
                TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 27, 10, 0),
                Duration.ofMinutes(10),
                epic.getId()
        );
        Subtask subtask2 = new Subtask(
                "subtask 2",
                "descr 2",
                TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 27, 10, 30),
                Duration.ofMinutes(25),
                epic.getId()
        );

        subtask1.setId(1);
        subtask2.setId(subtask1.getId());

        assertEquals(subtask1, subtask2, "ID подзадач не совпадают");
        assertNotEquals(subtask1.toString(), subtask2.toString(), "Подзадачи одинаковые");
    }

    @Test
    void differentSubtaskWithDifferentId() {
        Subtask subtask1 = new Subtask(
                "task 1",
                "descr 1",
                TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 27, 10, 0),
                Duration.ofMinutes(45),
                epic.getId()
        );
        Subtask subtask2 = new Subtask(
                "task 2",
                "descr 2",
                TaskStatus.NEW,
                LocalDateTime.of(2024, 10, 27, 11, 0),
                Duration.ofMinutes(20),
                epic.getId()
        );

        subtask1.setId(1);
        subtask2.setId(2);

        assertNotEquals(subtask1, subtask2, "ID подзадач совпадают");
        assertNotEquals(subtask1.toString(), subtask2.toString(), "Подзадачи одинаковые");
    }
}
