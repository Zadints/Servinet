package org.example.servinet.application.port.out;

import org.example.servinet.domain.entities.Announcement;
import org.example.servinet.domain.entities.AnnouncementComment;

import java.util.List;

public interface SqlAnnounceRepository {
    void add(Announcement entity);
    Announcement get(String id);
    List<Announcement> getPage(String userUuid, int offset, int pageSize);
    void delete(String id);
    void update(Announcement entity);
    boolean toggleLike(String announceUuid, String userUuid);
    List<AnnouncementComment> getComments(String announceUuid);
    void deleteComment(String commentUuid);
    void updateComment(String commentUuid, String content);
    void insertComment(AnnouncementComment c);
}
