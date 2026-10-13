package org.example.servinet.core.domain.entities;

import java.time.LocalDateTime;

public class AnnouncementComment {
    private final String uuid;
    private final String announceUuid;
    private final String authorUuid;
    private final String authorName;
    private String content;
    private final LocalDateTime createAt;

    public AnnouncementComment(String uuid, String announceUuid, String authorUuid,
                               String authorName, String content, LocalDateTime createAt) {
        this.uuid = uuid;
        this.announceUuid = announceUuid;
        this.authorUuid = authorUuid;
        this.authorName = authorName;
        this.content = content;
        this.createAt = createAt;
    }

    public String getUuid() { return uuid; }
    public String getAnnounceUuid() { return announceUuid; }
    public String getAuthorUuid() { return authorUuid; }
    public String getAuthorName() { return authorName; }
    public String getContent() { return content; }
    public LocalDateTime getCreateAt() { return createAt; }
    public void setContent(String content) { this.content = content; }
}
