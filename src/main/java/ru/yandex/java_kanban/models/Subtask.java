package ru.yandex.java_kanban.models;

import ru.yandex.java_kanban.enums.TaskStatus;
import ru.yandex.java_kanban.enums.TaskType;

public class Subtask extends Task {
    private final int epicId;
    private final TaskType type = TaskType.SUBTASK;

    public Subtask(String name, String description, TaskStatus status, Epic epic) {
        super(name, description, status);
        this.epicId = epic.getId();
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public TaskType getType() {
        return type;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != this.getClass()) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        Subtask task = (Subtask) obj;

        return this.getId() == task.getId();
    }

    @Override
    public String toString() {
        return "Subtask{" +
                "epicId=" + epicId +
                ", id=" + getId() +
                ", name='" + getName() + '\'' +
                ", description='" + getDescription() + '\'' +
                ", status=" + getStatus() +
                '}';
    }
}
