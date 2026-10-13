package org.example.servinet.core.domain.entities;

import javafx.scene.image.Image;
import org.example.servinet.core.domain.repository.Identifiable;

import java.time.LocalDateTime;

public class Announcement implements Identifiable {
    private final String uuid;
    private final String authorUuid;
    private final String authorName;
    private String title;
    private String description;
    private String priority;
    private Image image;
    private final LocalDateTime sendAt;
    private LocalDateTime updateAt;

    private int likeCount;
    private boolean likedByMe;
    private int commentCount;

    public Announcement(String uuid, String authorUuid, String authorName, String title,
                        String description, String priority, Image image,
                        LocalDateTime sendAt, LocalDateTime updateAt,
                        int likeCount, boolean likedByMe, int commentCount) {
        this.uuid = uuid;
        this.authorUuid = authorUuid;
        this.authorName = authorName;
        this.title = title;
        this.description = description;
        this.priority = priority;
        this.image = image;
        this.sendAt = sendAt;
        this.updateAt = updateAt;
        this.likeCount = likeCount;
        this.likedByMe = likedByMe;
        this.commentCount = commentCount;
    }

    @Override public String getUuid() { return uuid; }
    public String getAuthorUuid() { return authorUuid; }
    public String getAuthorName() { return authorName; }
    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public String getPriority() { return priority; }
    public Image getImage() { return image; }
    public LocalDateTime getSendAt() { return sendAt; }
    public LocalDateTime getUpdateAt() { return updateAt; }
    public int getLikeCount() { return likeCount; }
    public boolean isLikedByMe() { return likedByMe; }
    public int getCommentCount() { return commentCount; }

    public void setLikeCount(int likeCount) { this.likeCount = likeCount; }
    public void setLikedByMe(boolean likedByMe) { this.likedByMe = likedByMe; }
    public void setCommentCount(int commentCount) { this.commentCount = commentCount; }
}
