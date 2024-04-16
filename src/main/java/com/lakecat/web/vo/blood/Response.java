package com.lakecat.web.vo.blood;

import com.google.gson.annotations.SerializedName;
import com.lakecat.web.entity.TableUsageProfileGroupByUser;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.util.List;

@Data
public class Response {
    private int code;
    private Rdata data;
    private String msg;
    private boolean up;

    @Data
    public static class Rdata{
        @SerializedName("rootId")
        private String coreTaskId;
        @SerializedName("nodes")
        private List<Instance> instance;
        @SerializedName("links")
        private List<Relation> relation;

        @Data
        public static class Relation{
            @SerializedName("from")
            private String source;
            @SerializedName("to")
            private String target;
        }

        @Data
        public static class Instance{
            private String checkPath;
            private String dagId;
            private String downDagId;
            private String downNodeId;
            private String duration;
            private String end_date;
            private String executionDate;
            private String genie_job_id;
            private String genie_job_url;
            private String gra;
            private Boolean isExternal;
            //private Boolean is_spark_task;
            @SerializedName("text")
            private String metadataId;
            @SerializedName("id")
            private String nodeId;
            private String owner;
            private Boolean ready;
            private String start_date;
            private String  state;
            private String successDate;
            private Integer task_id;
            private Integer version;

            private TableDetail data;

            @Data
            public static class TableDetail{
                private Integer taskId;
                private String taskName;
                private List<TableUsageProfileGroupByUser> usageProfileGroupByUsers;
                private String owner;
                protected String region;
                protected String databaseName;
                protected String tableName;
                private Long tableId;
                private String tableOwner;//表owner
            }


           /* public void setMetadataId(String metadataId) {
                if (!StringUtils.isEmpty(metadataId)&&metadataId.indexOf("@")>-1){
                    metadataId=metadataId.substring(0,metadataId.indexOf("@"));
                }
                this.metadataId = metadataId;
            }*/
        }
    }

    public static void main(String[] args) {
        String a="123@ds";
        System.out.println(a.substring(0,a.indexOf("@")));
    }
}
