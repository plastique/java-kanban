package ru.yandex.java_kanban.managers;

import ru.yandex.java_kanban.managers.contracts.HistoryManager;
import ru.yandex.java_kanban.managers.contracts.TaskManager;

public class Managers {
    private Managers() {
    }

    public static TaskManager getDefault(HistoryManager historyManager) {
        return new InMemoryTaskManager(historyManager);
    }

    public static HistoryManager getDefaultHistory() {
        return new InMemoryHistoryManager();
    }
}
