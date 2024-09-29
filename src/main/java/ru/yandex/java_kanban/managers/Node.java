package ru.yandex.java_kanban.managers;

import ru.yandex.java_kanban.models.Task;

public final class Node {
    private Node prev;
    private Node next;
    private final Task task;

    public Node(Task task, Node prev, Node next) {
        this.prev = prev;
        this.next = next;
        this.task = task;
    }

    public void setPrev(Node prev) {
        this.prev = prev;
    }

    public Node getPrev() {
        return prev;
    }

    public void setNext(Node next) {
        this.next = next;
    }

    public Node getNext() {
        return next;
    }

    public Task getTask() {
        return task;
    }
}
