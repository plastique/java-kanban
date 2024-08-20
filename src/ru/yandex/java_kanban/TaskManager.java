package ru.yandex.java_kanban;

import ru.yandex.java_kanban.enums.TaskStatus;
import ru.yandex.java_kanban.models.Epic;
import ru.yandex.java_kanban.models.Subtask;
import ru.yandex.java_kanban.models.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private int increment = 0;
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Epic> epics;
    private final HashMap<Integer, Subtask> subtasks;
    private boolean updateEpicStatus = true;

    public TaskManager() {
        tasks = new HashMap<>();
        epics = new HashMap<>();
        subtasks = new HashMap<>();
    }

    private int getNewIncrement() {
        return ++increment;
    }

    // Tasks
    public Task createTask(Task task) {
        task.setId(getNewIncrement());
        tasks.put(task.getId(), task);

        return task;
    }

    public Task updateTask(Task task) {
        tasks.put(task.getId(), task);

        return task;
    }

    public HashMap<Integer, Task> getTasks() {
        return tasks;
    }

    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public void deleteTasks() {
        tasks.clear();
    }

    public void deleteTaskById(int id) {
        tasks.remove(id);
    }

    // Epics
    public Epic createEpic(Epic epic) {
        epic.setId(getNewIncrement());
        epics.put(epic.getId(), epic);

        return epic;
    }

    public Epic updateEpic(Epic epic) {
        epics.put(epic.getId(), epic);

        return epic;
    }

    public HashMap<Integer, Epic> getEpics() {
        return epics;
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    public HashMap<Integer, Subtask> getEpicSubtasks(Epic epic) {
        HashMap<Integer, Subtask> res = new HashMap<>();

        for (Subtask subtask : subtasks.values()) {
            if (subtask.getEpicId() == epic.getId()) {
                res.put(subtask.getId(), subtask);
            }
        }

        return res;
    }

    public void deleteEpics() {
        epics.clear();
        deleteSubtasks();
    }

    public void deleteEpicById(int id) {
        Epic epic = epics.get(id);

        if (epic == null) {
            return;
        }

        updateEpicStatus = false;
        for (Integer subtaskId: epic.getSubtaskIds()) {
            deleteSubtaskById(subtaskId);
        }
        updateEpicStatus = true;

        epics.remove(id);
    }

    // Subtasks
    public Subtask createSubtask(Subtask subtask) {
        subtask.setId(getNewIncrement());
        subtasks.put(subtask.getId(), subtask);

        Epic epic = epics.get(subtask.getEpicId());

        if (epic != null) {
            epic.addSubtaskId(subtask.getId());
            actualEpicStatus(epic);
        }

        return subtask;
    }

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

    public HashMap<Integer, Subtask> getSubtasks() {
        return subtasks;
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public void deleteSubtasks() {
        subtasks.clear();
        actualEpics();
    }

    public void deleteSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);

        if (subtask == null) {
            return;
        }

        Epic epic = epics.get(subtask.getEpicId());

        subtasks.remove(id);

        if (epic != null) {
            actualEpicStatus(epic);
        }
    }

    // Other
    private void actualEpics() {
        for (Epic epic : epics.values()) {
            actualEpicStatus(epic);
        }
    }

    private void actualEpicStatus(Epic epic) {
        if (!updateEpicStatus) {
            return;
        }

        ArrayList<Integer> subtaskIds = epic.getSubtaskIds();

        if (subtaskIds == null || subtaskIds.isEmpty()) {
            epic.setStatus(TaskStatus.NEW);
            return;
        }

        int cnt = subtaskIds.size();
        int isDone = 0;
        int inProgress = 0;
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
            } else if (subtask.getStatus() == TaskStatus.IN_PROGRESS) {
                inProgress++;
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
