package org.example.servinet.application.port.out.sql;

import org.example.servinet.application.dto.AttentionLog;
import org.example.servinet.domain.entities.AttentionStatistics;

import java.util.List;

public interface SqlSellerRepository {
    List<AttentionStatistics> getAttentionStatistics();
    void registerAttentionLog(AttentionLog log);
}
