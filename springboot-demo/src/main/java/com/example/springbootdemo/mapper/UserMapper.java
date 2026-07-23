package com.example.springbootdemo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springbootdemo.model.domain.UserDO;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface UserMapper extends BaseMapper<UserDO> {

    @Select({
            "<script>",
            "SELECT COUNT(1) FROM demo_user",
            "WHERE LOWER(username) = LOWER(#{username})",
            "<if test='excludeId != null'>AND id != #{excludeId}</if>",
            "</script>"
    })
    long countByUsernameIncludingDeleted(
            @Param("username") String username,
            @Param("excludeId") Long excludeId);
}
