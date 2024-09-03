package ru.yandex.java_kanban.managers;

import ru.yandex.java_kanban.enums.TaskStatus;
import ru.yandex.java_kanban.managers.contracts.HistoryManager;
import ru.yandex.java_kanban.managers.contracts.TaskManager;
import ru.yandex.java_kanban.models.Epic;
import ru.yandex.java_kanban.models.Subtask;
import ru.yandex.java_kanban.models.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private int increment = 0;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, Subtask> subtasks;
    private final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        this.historyManager = historyManager;
    }

    private int getNewIncrement() {
        return ++increment;
    }

    // Tasks
    @Override
    public Task createTask(Task task) {
        task.setId(getNewIncrement());
        tasks.put(task.getId(), task);

        return task;
    }

    @Override
    public Task updateTask(Task task) {
        tasks.put(task.getId(), task);

        return task;
    }

    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public Task getTaskById(int id) {
        Task task = tasks.get(id);

        historyManager.add(task);

        return task;
    }

    @Override
    public void deleteTasks() {
        tasks.clear();
    }

    @Override
    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    // Epics
    @Override
    public Epic createEpic(Epic epic) {
        epic.setId(getNewIncrement());
        epics.put(epic.getId(), epic);

        return epic;
    }

    @Override
    public Epic updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);

        return epic;
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        historyManager.add(epic);

        return epic;
    }

    @Override
    public ArrayList<Subtask> getEpicSubtasks(int epicId) {
        ArrayList<Subtask> res = new ArrayList<>();

        Epic epic = epics.get(epicId);

        if (epic == null) {
            return res;
        }

        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEpicId() == epic.getId()) {
                res.add(subtask);
            }
        }

        return res;
    }

    @Override
    public void deleteEpics() {
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);

        if (epic == null) {
            return;
        }

        for (Integer subtaskId : epic.getSubtaskIds()) {
            subtasks.remove(subtaskId);
        }
    }

    // Subtasks
    @Override
    public Subtask createSubtask(Subtask subtask) {
        subtask.setId(getNewIncrement());
        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());

        if (epic != null) {
            epic.addSubtask(subtask);
            actualEpicStatus(epic);
        }

        return subtask;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask currentSubtask = subtasks.get(subtask.getId());
        int currentEpicId = currentSubtask.getEpicId();

        subtasks.put(subtask.getId(), subtask);

        Epic epic = getEpicById(subtask.getEpicId());

        if (epic != null) {
            actualEpicStatus(epic);
        }

        if (currentEpicId != subtask.getEpicId()) {
            // Если сменился эпик у подзадачи, то обновляем эпик.
            Epic currentEpic = getEpicById(currentEpicId);
            actualEpicStatus(currentEpic);
        }

        return subtask;
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        historyManager.add(subtask);

        return subtask;
    }

    @Override
    public void deleteSubtasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.setSubtasksIds(new ArrayList<>());
        }
        actualEpics();
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);

        if (subtask == null) {
            return;
        }

        Epic epic = epics.get(subtask.getEpicId());

        if (epic != null) {
            actualEpicStatus(epic);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Other
    private void actualEpics() {
        for (Epic epic : epics.values()) {
            actualEpicStatus(epic);
        }
    }

    private void actualEpicStatus(Epic epic) {
        ArrayList<Integer> subtaskIds = epic.getSubtaskIds();

        if (subtaskIds == null || subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        int cnt = subtaskIds.size();
        int isDone = 0;
        int isNew = 0;

        for (Integer subtaskId : subtaskIds) {
            Subtask subtask = subtasks.get(subtaskId);

            if (subtask == null) {
                continue;
            }

            if (subtask.getStatus() == TaskStatus.NEW) {
                isNew++;
            } else if (subtask.getStatus() == TaskStatus.DONE) {
                isDone++;
            }
        }

        if (cnt == isDone) {
            epic.setStatus(TaskStatus.DONE);
            return;
        }

        if (cnt == isNew) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        epic.setStatus(TaskStatus.IN_PROGRESS);
    }
}
