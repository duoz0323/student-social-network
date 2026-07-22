package com.stu.edu.vn.backend.admin.repository.projection;

/** Projection chi tiết bổ sung JSON trước và sau thay đổi. */
public interface AdminActionDetailProjection extends AdminActionListProjection {
    String getOldData();

    String getNewData();
}
