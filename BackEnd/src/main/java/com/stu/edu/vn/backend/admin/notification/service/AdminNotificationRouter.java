package com.stu.edu.vn.backend.admin.notification.service;

import java.util.Collection;

public interface AdminNotificationRouter {
    void notifyByPermission(String permissionCode, Long actorAdminId, AdminNotificationEvent event);
    void notifyByAnyPermission(Collection<String> permissionCodes, Long actorAdminId, AdminNotificationEvent event);
    void notifyDirectAdmin(Long recipientAdminId, Long actorAdminId, AdminNotificationEvent event);
    void notifyRoleHolders(Long roleId, Long actorAdminId, AdminNotificationEvent event);
}
