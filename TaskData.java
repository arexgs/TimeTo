package todolist;

import java.time.LocalDate;

public class TaskData {
    private boolean done;
    private String title;
    private String description;
    private LocalDate deadline;

    public TaskData(boolean done, String title, String description, LocalDate deadline) { // HILANGKAN rank
        this.done = done;
        this.title = title;
        this.description = description;
        this.deadline = deadline;
    }

    public boolean isDone() { return done; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public LocalDate getDeadline() { return deadline; }

    public void setDone(boolean done) { this.done = done; }
    public void setTitle(String title) { this.title = title; }
    public void setDescription(String description) { this.description = description; }
    public void setDeadline(LocalDate deadline) { this.deadline = deadline; }

    @Override
    public String toString() {
        return title + (description != null && !description.isEmpty() ? " - " + description : "");
    }
}