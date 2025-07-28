package com.prtech.spiapp.db.entity;

import androidx.room.ColumnInfo;
import androidx.room.PrimaryKey;

public abstract class BaseEntity {

    @PrimaryKey(autoGenerate = true)
    public Long id;


    @ColumnInfo(name = "display_order")
    public Integer displayOrder;

    @ColumnInfo(name = "created_at")
    public Long createdAt;

    @ColumnInfo(name = "updated_at")
    public Long updatedAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public Long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Long createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Long updatedAt) {
        this.updatedAt = updatedAt;
    }
}

