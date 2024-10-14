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
import java.util.Map;

public class InMemoryTaskManager implements TaskManager {
    protected int increment = 0;
    protected final Map<Integer, Task> tasks;
    protected final Map<Integer, Epic> epics;
    protected final Map<Integer, Subtask> subtasks;
    protected final HistoryManager historyManager;

    public InMemoryTaskManager(HistoryManager historyManager) {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
        this.historyManager = historyManager;
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
    public List<Task> getTasks() {
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
        for (Task task : tasks.values()) {
            historyManager.remove(task.getId());
        }
        tasks.clear();
    }

    @Override
    public void deleteTaskById(int id) {
        historyManager.remove(id);
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
        Epic oldEpic = epics.get(epic.getId());
        oldEpic.setName(epic.getName());
        oldEpic.setDescription(epic.getDescription());

        return oldEpic;
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);

        historyManager.add(epic);

        return epic;
    }

    @Override
    public List<Subtask> getEpicSubtasks(int epicId) {
        List<Subtask> res = new ArrayList<>();

        Epic epic = epics.get(epicId);

        if (epic == null) {
            return res;
        }

        for (int subtaskId : epic.getSubtaskIds()) {
            Subtask subtask = subtasks.get(subtaskId);
            if (subtask != null) {
                res.add(subtask);
            }
        }

        return res;
    }

    @Override
    public void deleteEpics() {
        for (Epic epic : epics.values()) {
            historyManager.remove(epic.getId());
        }
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
        }
        epics.clear();
        subtasks.clear();
    }

    @Override
    public void deleteEpicById(int id) {
        Epic epic = epics.remove(id);

        if (epic == null) {
            return;
        }

        historyManager.remove(id);

        for (Integer subtaskId : epic.getSubtaskIds()) {
            subtasks.remove(subtaskId);
            historyManager.remove(subtaskId);
        }
    }

    // Subtasks
    @Override
    public Subtask createSubtask(Subtask subtask) {
        subtask.setId(getNewIncrement());
        subtasks.put(subtask.getId(), subtask);

        actualEpicSubtasks(subtask);

        return subtask;
    }

    @Override
    public Subtask updateSubtask(Subtask subtask) {
        Subtask currentSubtask = subtasks.get(subtask.getId());
        int currentEpicId = currentSubtask.getEpicId();

        currentSubtask.setName(subtask.getName());
        currentSubtask.setDescription(subtask.getDescription());
        currentSubtask.setStatus(subtask.getStatus());

        Epic currentEpic = epics.get(currentEpicId);
        actualEpicStatus(currentEpic);

        return currentSubtask;
    }

    @Override
    public List<Subtask> getSubtasks() {
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
        for (Subtask subtask : subtasks.values()) {
            historyManager.remove(subtask.getId());
        }
        subtasks.clear();

        for (Epic epic : epics.values()) {
            epic.deleteSubtasks();
            actualEpicStatus(epic);
        }
    }

    @Override
    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.remove(id);

        if (subtask == null) {
            return;
        }

        historyManager.remove(id);

        Epic epic = epics.get(subtask.getEpicId());

        if (epic == null) {
            return;
        }

        epic.getSubtaskIds().remove((Integer)subtask.getId());

        actualEpicStatus(epic);
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

    // Other
    private int getNewIncrement() {
        return ++increment;
    }

    protected void actualEpicSubtasks(Subtask subtask) {
        Epic epic = epics.get(subtask.getEpicId());

        if (epic == null) {
            return;
        }

        epic.addSubtask(subtask);
        actualEpicStatus(epic);
    }

    private void actualEpicStatus(Epic epic) {
        List<Integer> subtaskIds = epic.getSubtaskIds();

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
            } else {
                epic.setStatus(TaskStatus.IN_PROGRESS);
                return;
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
