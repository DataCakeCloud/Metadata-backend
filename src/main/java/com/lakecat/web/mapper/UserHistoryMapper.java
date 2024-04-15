package com.lakecat.web.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lakecat.web.entity.InputInfo;
import com.lakecat.web.entity.TableInfoSearchHistory;
import com.lakecat.web.entity.TableInfoUserInput;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 *
 * @author mybatis-plus-generator
 * @since 2021-12-11
 */
@Mapper
public interface UserHistoryMapper extends BaseMapper <TableInfoUserInput> {

    /**
     * 通过用户id获取查询表名
     *
     * @param userId 用户id
     * @return List <TableInfoSearchHistory> 查询列表
     */
    @Select("SELECT table_info_id, sole as table_info_name \n" +
            "FROM table_info_search_history \n" +
            "WHERE user_id=#{userId} \n" +
            "AND table_info_name is not null \n" +
            "GROUP BY table_info_name \n" +
            "ORDER BY create_time DESC \n" +
            "limit #{limit}")
    List <TableInfoSearchHistory> getTableName(@Param("userId") String userId,Integer limit);


    /**
     * 通过用户id获取用户输入
     *
     * @param userId 用户id
     * @return List <String> 用户输入列表 长度10
     */
    @Select("SELECT id,input \n" +
            "FROM table_info_user_input \n" +
            "WHERE user_id=#{userId} and status =0  \n" +
            "AND input is not null \n" +
            "GROUP BY input\n" +
            "ORDER BY create_time DESC \n" +
            "limit #{limit}")
    List <InputInfo> getItem(@Param("userId") String userId, @Param("limit") Integer limit);


    /**
     * 通过用户id获取用户输入
     *
     * @param userId 用户id
     * @return List <String> 用户输入列表 长度10
     */
    @Select("SELECT input \n" +
            "FROM table_info_user_input \n" +
            "WHERE user_id=#{userId} and status =0 \n" +
            "AND input is not null \n" +
            "GROUP BY input\n" +
            "ORDER BY MAX(create_time) DESC \n" +
            "limit 10")
    List <String> getItemString(@Param("userId") String userId);


    /**
     * 通过用户id获取用户输入
     *
     * @return List <String> 用户输入列表 长度10
     */
    @Update("update  table_info_user_input set status = 1 where input = #{name}")
    boolean deleteItem(@Param("name") String name);


    /**
     * 获取主题域
     *
     * @return List <String> 主题域列表
     */
    @Select("select subject \n" +
            "from table_info_subject")
    List <String> getTopic();

}
