package ru.yandex.java_kanban.models;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class EpicTest {
    @Test
    void sameEpicWithSameId() {
        Epic epic1 = new Epic("task 1", "descr 1");
        Task epic2 = new Epic("task 2", "descr 2");

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
}
