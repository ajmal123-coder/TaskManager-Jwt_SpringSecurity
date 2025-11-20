package com.Task.Manager.controller;

import com.Task.Manager.dto.TaskRequest;
import com.Task.Manager.model.Task;
import com.Task.Manager.model.User;
import com.Task.Manager.repository.TaskRepository;
import com.Task.Manager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    // Helper method to get the currently authenticated user
    private User getAuthenticatedUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Authenticated user not found"));
    }

    @GetMapping
    public ResponseEntity<List<Task>> getMyTasks() {
        User user = getAuthenticatedUser();
        List<Task> tasks = taskRepository.findByUser(user);
        return ResponseEntity.ok(tasks);
    }

    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody TaskRequest request) {
        User user = getAuthenticatedUser();
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .completed(false)
                .user(user)
                .build();
        taskRepository.save(task);
        return ResponseEntity.ok(task);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        User user = getAuthenticatedUser();
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Task not found"));

        // Security Check: Make sure the task belongs to the authenticated user
        if (!task.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body("You can only delete your own tasks");
        }

        taskRepository.delete(task);
        return ResponseEntity.ok("Task deleted successfully");
    }
}