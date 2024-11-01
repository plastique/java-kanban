package ru.yandex.java_kanban.models;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.java_kanban.enums.TaskStatus;
import ru.yandex.java_kanban.managers.Managers;
import ru.yandex.java_kanban.managers.contracts.TaskManager;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class EpicTest {

    private TaskManager manager;

    @BeforeEach
    public void setUp() {
        manager = Managers.getDefault(
                Managers.getDefaultHistory()
        );
    }

    @Test
    void sameEpicWithSameId() {
        Epic epic1 = new Epic("task 1", "descr 1");
        Epic epic2 = new Epic("task 2", "descr 2");

        epic1.setId(1);
        epic2.setId(epic1.getId());

        assertEquals(epic1, epic2, "ID эпиков не совпадают");
        assertNotEquals(epic1.toString(), epic2.toString(), "Эпики одинаковые");
    }

    @Test
    void differentEpicWithDifferentId() {
        Epic epic1 = new Epic("task 1", "descr 1");
        Epic epic2 = new Epic("task 2", "descr 2");

        epic1.setId(1);
        epic2.setId(2);

        assertNotEquals(epic1, epic2, "ID эпиков совпадают");
        assertNotEquals(epic1.toString(), epic2.toString(), "Эпики одинаковые");
    }

    @Test
    void epicHasCorrectStateNew() {
        Epic epic = manager.createEpic(
                new Epic("task 1", "descr 1")
        );

        Subtask subtask1 = manager.createSubtask(
                new Subtask(
                        "subtask 1",
                        "descr sub 1",
                        TaskStatus.NEW,
                        LocalDateTime.of(2024, 10, 28, 9, 0),
                        Duration.ofMinutes(10),
                        epic.getId()
                )
        );

        Subtask subtask2 = manager.createSubtask(
                new Subtask(
                        "subtask 2",
                        "descr sub 2",
                        TaskStatus.NEW,
                        LocalDateTime.of(2024, 10, 28, 10, 0),
                        Duration.ofMinutes(10),
                        epic.getId()
                )
        );

        assertEquals(TaskStatus.NEW, epic.getStatus());
    }

    @Test
    void epicHasCorrectStateDone() {
        Epic epic = manager.createEpic(
                new Epic("task 1", "descr 1")
        );

        Subtask subtask1 = manager.createSubtask(
                new Subtask(
                        "subtask 1",
                        "descr sub 1",
                        TaskStatus.DONE,
                        LocalDateTime.of(2024, 10, 28, 9, 0),
                        Duration.ofMinutes(10),
                        epic.getId()
                )
        );

        Subtask subtask2 = manager.createSubtask(
                new Subtask(
                        "subtask 2",
                        "descr sub 2",
                        TaskStatus.DONE,
                        LocalDateTime.of(2024, 10, 28, 10, 0),
                        Duration.ofMinutes(10),
                        epic.getId()
                )
        );

        assertEquals(TaskStatus.DONE, epic.getStatus());
    }

    @Test
    void epicHasCorrectState() {
        Epic epic = manager.createEpic(
                new Epic("task 1", "descr 1")
        );

        Subtask subtask1 = manager.createSubtask(
                new Subtask(
                        "subtask 1",
                        "descr sub 1",
                        TaskStatus.NEW,
                        LocalDateTime.of(2024, 10, 28, 9, 0),
                        Duration.ofMinutes(10),
                        epic.getId()
                )
        );

        Subtask subtask2 = manager.createSubtask(
                new Subtask(
                        "subtask 2",
                        "descr sub 2",
                        TaskStatus.DONE,
                        LocalDateTime.of(2024, 10, 28, 10, 0),
                        Duration.ofMinutes(10),
                        epic.getId()
                )
        );

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }

    @Test
    void epicHasCorrectStateInProgress() {
        Epic epic = manager.createEpic(
                new Epic("task 1", "descr 1")
        );

        Subtask subtask1 = manager.createSubtask(
                new Subtask(
                        "subtask 1",
                        "descr sub 1",
                        TaskStatus.IN_PROGRESS,
                        LocalDateTime.of(2024, 10, 28, 9, 0),
                        Duration.ofMinutes(10),
                        epic.getId()
                )
        );

        Subtask subtask2 = manager.createSubtask(
                new Subtask(
                        "subtask 2",
                        "descr sub 2",
                        TaskStatus.IN_PROGRESS,
                        LocalDateTime.of(2024, 10, 28, 10, 0),
                        Duration.ofMinutes(10),
                        epic.getId()
                )
        );

        assertEquals(TaskStatus.IN_PROGRESS, epic.getStatus());
    }
}
