package com.sparksys.authorization.interfaces.dto.user;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.sparksys.database.entity.RemoteData;
import com.sparksys.authorization.infrastructure.entity.CoreOrg;
import com.sparksys.authorization.infrastructure.entity.CoreStation;
import com.sparksys.authorization.infrastructure.enums.SexEnum;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;


/**
 * description: 用户信息
 *
 * @author zhouxinlei
 * @date 2020-05-24 12:24:03
 */
@Data
@Accessors(chain = true)
@ApiModel(value = "AuthUserDTO查询对象", description = "AuthUserDTO查询对象")
public class AuthUserDTO {


    @ApiModelProperty(value = "id")
    private Long id;

    @ApiModelProperty(value = "账号")
    private String account;

    @ApiModelProperty(value = "姓名")
    private String name;

    @ApiModelProperty(value = "组织")
    private RemoteData<Long,CoreOrg> org;


    @ApiModelProperty(value = "岗位")
    private RemoteData<Long, CoreStation> station;

    @ApiModelProperty(value = "邮箱")
    private String email;

    @ApiModelProperty(value = "手机")
    private String mobile;

    @ApiModelProperty(value = "性别")
    private String sex;

    @ApiModelProperty(value = "状态 1启用 0禁用")
    private Boolean status;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @ApiModelProperty(value = "民族")
    private String nation;

    @ApiModelProperty(value = "学历")
    private String education;

    @ApiModelProperty(value = "职位状态")
    private String positionStatus;

    @ApiModelProperty(value = "工作描述比如：市长、管理员、局长等等   用于登陆展示")
    private String workDescribe;

    @ApiModelProperty(value = "最后登录时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime lastLoginTime;

    @ApiModelProperty(value = "创建人id")
    private Long createUser;

    @ApiModelProperty(value = "创建时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime createTime;

    @ApiModelProperty(value = "更新人id")
    private Long updateUser;

    @ApiModelProperty(value = "更新时间")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime updateTime;


    public void setSex(SexEnum sex) {
        this.sex = sex.getDesc();
    }

}
