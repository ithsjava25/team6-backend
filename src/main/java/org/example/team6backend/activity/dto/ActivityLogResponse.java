package org.example.team6backend.activity.dto;

import org.example.team6backend.activity.entity.ActivityLog;

import java.time.Instant;

public class ActivityLogResponse {

	private String action;
	private String description;
	private Instant createdAt;
	private String userName;

	public static ActivityLogResponse fromEntity(ActivityLog activityLog) {
		ActivityLogResponse response = new ActivityLogResponse();
		response.setAction(activityLog.getAction());
		response.setDescription(activityLog.getDescription());
		response.setCreatedAt(activityLog.getCreatedAt());
		response.setUserName(activityLog.getUser() != null ? activityLog.getUser().getName() : "Unknow user");
		return response;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}
}
