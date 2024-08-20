package ru.yandex.java_kanban.models;

import ru.yandex.java_kanban.enums.TaskStatus;

import java.util.ArrayList;
import java.util.Objects;

public class Epic extends Task {
    private ArrayList<Integer> subtaskIds;

    public Epic(String name, String description) {
        super(name, description, TaskStatus.NEW);
        subtaskIds = new ArrayList<>();
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public void setSubtasksIds(ArrayList<Integer> subtaskIds) {
        this.subtaskIds = subtaskIds;
    }

    public void addSubtaskId(int subtaskId) {
        subtaskIds.add(subtaskId);
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

        return this.id == epic.getId();
    }

    @Override
    public String toString() {
        return "Epic{" +
                "id=" + id +
                ", description='" + description + '\'' +
                ", subtaskIds=" + subtaskIds +
                ", name='" + name + '\'' +
                ", status=" + status +
                '}';
    }
}
