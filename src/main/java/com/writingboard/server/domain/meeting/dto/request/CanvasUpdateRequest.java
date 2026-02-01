package com.writingboard.server.domain.meeting.dto.request;

import com.writingboard.server.domain.meeting.entity.enums.SnapshotTriggerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Deprecated
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CanvasUpdateRequest {

    private String rawData;

    private boolean createSnapshot;

    private SnapshotTriggerType snapshotTriggerType;
}
