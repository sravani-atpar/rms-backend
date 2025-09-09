package com.RMS_Backend.Restaurant.Management.System.service;


import com.RMS_Backend.Restaurant.Management.System.config.AbstractAuditingEntity;



public interface AuditingService {
    <T, E extends AbstractAuditingEntity<T>> void setCreationAuditingFields(E entity);

    <T, E extends AbstractAuditingEntity<T>> void setUpdateAuditingFields(E entity);
}
