package com.lakecat.web.vo.blood;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

import java.sql.Timestamp;
import java.util.Map;

/**
 * @author fengxiao
 * @date 2022/7/22
 */
@Data
public class SourceRead {

   /* private Integer sourceDefinitionId;
    private Integer sourceId;
    private JSONObject connectionConfiguration;*/
    private String name;
    private String region;
    /*private String sourceName;
    private String sourceIcon;
    //private ReleaseStage sourceReleaseStage;
    private String createBy;
    private String updateBy;*/
    private String uuid;
   /* private Timestamp createTime;
    private Timestamp updateTime;
*/
   /* public SourceRead(Actor actor, Map<Integer, ActorDefinition> definitionMap) {
        this.sourceDefinitionId = actor.getActorDefinitionId();
        this.sourceId = actor.getId();
        this.connectionConfiguration = JSONObject.parseObject(actor.getConfiguration());
        this.name = actor.getName();
        this.region = actor.getRegion();
        this.createBy = actor.getCreateBy();
        this.updateBy = actor.getUpdateBy();
        this.createTime = actor.getCreateTime();
        this.updateTime = actor.getUpdateTime();

        ActorDefinition definition = definitionMap.get(actor.getActorDefinitionId());
        if (definition != null) {
            this.sourceName = definition.getName();
            this.sourceIcon = definition.getIcon();
            this.sourceReleaseStage = ReleaseStage.fromValue(definition.getReleaseStage());
        }
    }*/
}
