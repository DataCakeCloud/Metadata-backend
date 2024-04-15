package com.lakecat.web.service.impl;


import com.lakecat.web.constant.BaseResponseCodeEnum;
import com.lakecat.web.entity.LakeCatParam;
import com.lakecat.web.entity.LineageNodeInfo;
import com.lakecat.web.entity.LineageResult;
import com.lakecat.web.entity.NodeRelation;
import com.lakecat.web.exception.BusinessException;
import com.lakecat.web.exception.ServiceException;
import com.lakecat.web.service.ILakeCatClientService;
import com.lakecat.web.service.LineageService;
import io.lakecat.catalog.common.lineage.ELineageDirection;
import io.lakecat.catalog.common.lineage.LineageFact;
import io.lakecat.catalog.common.lineage.LineageNode;
import io.lakecat.catalog.common.model.LineageInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;


@Service
@Slf4j
public class LineageServiceImpl implements LineageService {


    @Autowired
    public ILakeCatClientService lakeCatClientService;

    @Override
    public LineageResult getLineageGraph(LakeCatParam lakeCatParam) {
        if (Objects.equals(lakeCatParam.getAfterDepth(), lakeCatParam.getBeforeDepth()) ||
                (lakeCatParam.getAfterDepth() == 0 || lakeCatParam.getBeforeDepth() == 0)) {
            if (lakeCatParam.getAfterDepth() == 0) {
                lakeCatParam.setDepth(lakeCatParam.getBeforeDepth());
                lakeCatParam.setDirection(ELineageDirection.UPSTREAM.toString());
            }
            if (lakeCatParam.getBeforeDepth() == 0) {
                lakeCatParam.setDepth(lakeCatParam.getAfterDepth());
                lakeCatParam.setDirection(ELineageDirection.DOWNSTREAM.toString());
            }
            LineageInfo lineageGraph = processLineageException(lakeCatParam);
            return processLineageGraphResult(lineageGraph);
        }
        //双向深度
        //取前面的
        lakeCatParam.setDepth(lakeCatParam.getBeforeDepth());
        lakeCatParam.setDirection(ELineageDirection.UPSTREAM.toString());
        LineageInfo bLineageGraph = processLineageException(lakeCatParam);


        //取后面的
        lakeCatParam.setDepth(lakeCatParam.getAfterDepth());
        lakeCatParam.setDirection(ELineageDirection.DOWNSTREAM.toString());
        LineageInfo aLineageGraph = processLineageException(lakeCatParam);

            if(bLineageGraph==null && aLineageGraph!=null){
                return processLineageGraphResult(aLineageGraph);
            }
            if(bLineageGraph!=null && aLineageGraph==null){
                return processLineageGraphResult(bLineageGraph);
            }
            if(bLineageGraph==null && aLineageGraph==null){
                return new LineageResult();
            }

        //和并结果
        LineageInfo resLineageGraph = new LineageInfo();
        resLineageGraph.setBaseNode(aLineageGraph.getBaseNode());
        resLineageGraph.setRelations(new ArrayList<>());
        resLineageGraph.setNodes(new ArrayList<>());


        if (bLineageGraph.getRelations() != null) {
            resLineageGraph.getRelations().addAll(bLineageGraph.getRelations());
        }
        if (aLineageGraph.getRelations() != null) {
            resLineageGraph.getRelations().addAll(aLineageGraph.getRelations());
        }
        if (bLineageGraph.getNodes() != null) {
            resLineageGraph.getNodes().addAll(bLineageGraph.getNodes());
        }
        if (aLineageGraph.getNodes() != null) {
            resLineageGraph.getNodes().addAll(aLineageGraph.getNodes());
        }

        //去重当前节点
        ArrayList<LineageNode> collect = resLineageGraph.getNodes().stream()
                .collect(Collectors.collectingAndThen(Collectors.toCollection(
                                () -> new TreeSet<>(Comparator.comparing(LineageNode::getId))),
                        ArrayList::new));
        resLineageGraph.setNodes(collect);
        return processLineageGraphResult(resLineageGraph);
    }

    /**
     * 处理没表的情况 是不是无法判断真的无表呢？
     * @param lakeCatParam
     * @return
     */
    public LineageInfo processLineageException(LakeCatParam lakeCatParam){
        LineageInfo lineageGraph=null;
        try {
            lineageGraph = lakeCatClientService.getLineageGraph(lakeCatParam);
        }catch (Exception e){
            if(e.getMessage().contains("errorCode=Dash.00192")){
                return null;
            }
            e.printStackTrace();
            log.error("获取血缘信息失败",e.getMessage());
            throw new ServiceException(BaseResponseCodeEnum.LINEAGE_GRAPH_GET_FAIL);
        }
        return lineageGraph;
    }

    public LineageResult processLineageGraphResult(LineageInfo lineageGraph){
        LineageResult lineageResult = new LineageResult();
        if(lineageGraph==null){
            return lineageResult;
        }
        //当前节点 rootId
        LineageNode baseNode = lineageGraph.getBaseNode();
        lineageResult.setRootId(baseNode.getId());

        //关系 links
        List<LineageInfo.LineageRs> relations = lineageGraph.getRelations();
        if(relations==null || relations.isEmpty()){
            return lineageResult;
        }
        List<NodeRelation> links = relations.stream().map(data -> {
            NodeRelation nodeRelation = new NodeRelation();
            nodeRelation.setFrom(data.getUpId());
            nodeRelation.setTo(data.getDownId());
            nodeRelation.setLineageType(data.getLineageType());
            nodeRelation.setJobStatus(data.getJobStatus());
            nodeRelation.setExecuteUser(data.getExecuteUser());
            nodeRelation.setJobFactId(data.getJobFactId());
            nodeRelation.setParams(data.getParams());
            return nodeRelation;
        }).collect(toList());
        lineageResult.setLinks(links);

        //处理线关系id
        Map<Integer, List<LineageInfo.LineageRs>> upMap = relations.
                stream().collect(Collectors.groupingBy(LineageInfo.LineageRs::getUpId));

        Map<Integer, List<LineageInfo.LineageRs>> downMap = relations.
                stream().collect(Collectors.groupingBy(LineageInfo.LineageRs::getDownId));

        List<LineageNode> nodes = lineageGraph.getNodes();
        //转变后有关系的
        List<LineageNodeInfo> nodeRes = nodes.stream().map(data -> {
            LineageNodeInfo build = LineageNodeInfo.builder()
                    .id(data.getId()).dt(data.getDt())
                    .ot(data.getOt()).qn(data.getQn())
                    .st(data.getSt()).build();
            if(data.getId().equals(lineageResult.getRootId())){
                build.setBeforeRelations(downMap.get(data.getId()));
            }
            build.setRelations(upMap.get(data.getId()));
            return build;
        }).collect(toList());
        lineageResult.setNodes(nodeRes);
        return lineageResult;
    }


    /**
     * 获取血缘关系信息
     *
     * @param lakeCatParam
     * @return
     */
    @Override
    public LineageFact getLineageFact(LakeCatParam lakeCatParam) {
        LineageFact lineageFact = lakeCatClientService.getLineageFact(lakeCatParam);
        return lineageFact;
    }


}
