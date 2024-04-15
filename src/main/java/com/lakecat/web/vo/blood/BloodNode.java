package com.lakecat.web.vo.blood;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BloodNode {
    private int count;//目前有多少个数目
    private String nodeName;
    private List<BloodNode> leftNodes;
    private List<BloodNode> rightNodes;
    private int depth;//数的深度;
    private Map<String,BloodNode> allNodes;

}
