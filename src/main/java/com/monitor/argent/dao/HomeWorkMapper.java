package com.monitor.argent.dao;

import com.monitor.argent.entity.HomeWorkRequestBean;
import com.monitor.argent.entity.HomeWorkResponseBody;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface HomeWorkMapper {
    // 添加
    int addHomeWorkTestCase(@Param("homeWorkRequestBean")HomeWorkRequestBean homeWorkRequestBean);

    // 查询
    List<HomeWorkResponseBody> getHomeWorkTestCase(@Param("caseName") String caseName, @Param("stage") Integer stage, @Param("subject") Integer subject, @Param("flag") int flag);

    // 修改需要指定ID
    int editHomeWorkTestCase(@Param("homeWorkResponseBody") HomeWorkResponseBody homeWorkResponseBody);

}
