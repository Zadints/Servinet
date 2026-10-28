package org.example.servinet.application.port.out.sql;

import org.example.servinet.domain.entities.Announcement;
import org.example.servinet.domain.entities.AnnouncementComment;

import java.util.List;

public interface AnnounceRepository extends LongDataRepository<Announcement,List<Announcement>> {

    boolean toggleLike(String announceUuid, String userUuid);
    List<AnnouncementComment> getComments(String announceUuid);
    void deleteComment(String commentUuid);
    void updateComment(String commentUuid, String content);
    void insertComment(AnnouncementComment c);
}
