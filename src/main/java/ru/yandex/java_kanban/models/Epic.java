package ru.yandex.java_kanban.models;

import ru.yandex.java_kanban.enums.TaskStatus;

import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {
    private final List<Integer> subtaskIds;

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
        subtaskIds = new ArrayList<>();
    }

    public List<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void deleteSubtasks() {
        subtaskIds.clear();
    }

    public void addSubtask(Subtask subtask) {
        subtaskIds.add(subtask.getId());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        Epic epic = (Epic) obj;

        return this.getId() == epic.getId();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + getId() +
                ", description='" + getDescription() + '\'' +
                ", subtaskIds=" + subtaskIds +
                ", name='" + getName() + '\'' +
                ", status=" + getStatus() +
                '}';
    }
}
