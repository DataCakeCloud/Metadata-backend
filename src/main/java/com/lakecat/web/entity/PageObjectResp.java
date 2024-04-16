package com.lakecat.web.entity;

import java.util.Collection;

import com.lakecat.web.vo.blood.SourceRead;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(description = "分页")
public class PageObjectResp extends PageBase {

    @ApiModelProperty(value = "对象类型")
    private String objectType;
    @ApiModelProperty(value = "检索关键字")
    private String keyword;
    @ApiModelProperty(value = "对象列表")
    private Collection<String> objectNames;

    private Collection<SourceRead> actors;

    private String nextMarker;
    private String previousMarker;
}
