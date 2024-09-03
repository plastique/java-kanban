package ru.yandex.java_kanban.managers.contracts;

import ru.yandex.java_kanban.models.Epic;
import ru.yandex.java_kanban.models.Subtask;
import ru.yandex.java_kanban.models.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    // Tasks
    Task createTask(Task task);

    Task updateTask(Task task);

    ArrayList<Task> getTasks();

    Task getTaskById(int id);

    void deleteTasks();

    void deleteTaskById(int id);

    // Epics
    Epic createEpic(Epic epic);

    Epic updateEpic(Epic epic);

    ArrayList<Epic> getEpics();

    Epic getEpicById(int id);

    ArrayList<Subtask> getEpicSubtasks(int epicId);

    void deleteEpics();

    void deleteEpicById(int id);

    // Subtasks
    Subtask createSubtask(Subtask subtask);

    Subtask updateSubtask(Subtask subtask);

    ArrayList<Subtask> getSubtasks();

    Subtask getSubtaskById(int id);

    void deleteSubtasks();

    void deleteSubtaskById(int id);

    List<Task> getHistory();
}
