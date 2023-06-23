package com.psjava.ssyx.acl.utils;

import com.psjava.ssyx.model.acl.Permission;

import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {
    public static List<Permission> buildPermission(List<Permission> allList) {
        List<Permission> trees = new ArrayList<>();
        for(Permission treeNode: allList) {
            if (treeNode.getPid() == 0) {
                treeNode.setLevel(1);
                trees.add(findChildren(treeNode, allList));
            }
        }
        return trees;
    }

    /**
     * 递归查找子节点
     */
    public static Permission findChildren(Permission treeNode, List<Permission> allList) {
        treeNode.setChildren(new ArrayList<Permission>());

        for (Permission it : allList) {
            if(treeNode.getId().longValue() == it.getPid().longValue()) {
                int level = treeNode.getLevel() + 1;
                it.setLevel(level);
                if (treeNode.getChildren() == null) {
                    treeNode.setChildren(new ArrayList<>());
                }
                treeNode.getChildren().add(findChildren(it, allList));
            }
        }
        return treeNode;
    }
}
