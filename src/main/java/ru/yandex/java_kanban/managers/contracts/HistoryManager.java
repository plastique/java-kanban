package ru.yandex.java_kanban.managers.contracts;

import ru.yandex.java_kanban.models.Task;

import java.util.List;

public interface HistoryManager {
    void add(Task task);

    List<Task> getHistory();
}
