/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package sabelan.packaging.objects;

import java.io.Serializable;

/**
 *
 * @author sergey
 */
public class PackagingTask implements Serializable {
    
    public final int taskId;
    public final int skuId;
    public final String skuName;
    public final String packerName;
    public final int packQty;
    public final String checkedDate;

    public PackagingTask(int taskId, int skuId, String skuName, String packerName, int packQty, String checkedDate) {
        this.taskId = taskId;
        this.skuId = skuId;
        this.skuName = skuName;
        this.packerName = packerName;
        this.packQty = packQty;
        this.checkedDate = checkedDate;
    }

    public int getTaskId() {
        return taskId;
    }

    public int getSkuId() {
        return skuId;
    }

    public String getSkuName() {
        return skuName;
    }

    public String getPackerName() {
        return packerName;
    }

    public int getPackQty() {
        return packQty;
    }

    public String getCheckedDate() {
        return checkedDate;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + this.taskId;
        hash = 37 * hash + this.skuId;
        hash = 37 * hash + this.packQty;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final PackagingTask other = (PackagingTask) obj;
        if (this.taskId != other.taskId) {
            return false;
        }
        if (this.skuId != other.skuId) {
            return false;
        }
        if (this.packQty != other.packQty) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "PackagingTask{" + "taskId=" + taskId + ", skuId=" + skuId + ", skuName=" + skuName + ", packerName=" + packerName + ", packQty=" + packQty + ", checkedDate=" + checkedDate + '}';
    }
}
