package com.zjh.junsobackend.manager;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjh.junsobackend.common.ErrorCode;
import com.zjh.junsobackend.dataSource.DataSource;
import com.zjh.junsobackend.dataSource.impl.PictureDataSource;
import com.zjh.junsobackend.dataSource.impl.PostDataSource;
import com.zjh.junsobackend.dataSource.impl.UserDataSource;
import com.zjh.junsobackend.enums.SearchTypeEnum;
import com.zjh.junsobackend.exception.BusinessException;
import com.zjh.junsobackend.pojo.entity.Picture;
import com.zjh.junsobackend.pojo.vo.PostVo;
import com.zjh.junsobackend.pojo.vo.SearchVo;
import com.zjh.junsobackend.pojo.vo.UserVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Component
public class DataSourceRegister {
    @Autowired
    private PictureDataSource pictureDataSource;

    @Autowired
    private PostDataSource postDataSource;

    @Autowired
    private UserDataSource userDataSource;


    private Map<String, DataSource> dataSourceMap;

    /**
     * PostConstruct修饰方法后，此方法会在其他依赖全部注入后再执行
     */
    @PostConstruct
    public void doInit() {
        dataSourceMap = new HashMap(){{
            put(SearchTypeEnum.PICTURE.getType(), pictureDataSource);
            put(SearchTypeEnum.POST.getType(), postDataSource);
            put(SearchTypeEnum.USER.getType(), userDataSource);
        }};
    }


    public DataSource getDataSourceByType(String type) {
        return dataSourceMap.get(type);
    }

    public SearchVo executeAllDoSearch(String searchText, int current, long pageSize) {

        SearchVo searchVo = new SearchVo();

        //图片
        CompletableFuture<Page<Picture>> pictureTask = CompletableFuture.supplyAsync(() -> pictureDataSource.doSearch(searchText, current, pageSize));


        //用户
        CompletableFuture<Page<UserVo>> userTask = CompletableFuture.supplyAsync(() -> userDataSource.doSearch(searchText, current, pageSize));


        //帖子
        CompletableFuture<Page<PostVo>> postTask = CompletableFuture.supplyAsync(() -> postDataSource.doSearch(searchText, current, pageSize));

        try {
            searchVo.setPicturePage(pictureTask.get());
            searchVo.setUserVoPage(userTask.get());
            searchVo.setPostVoPage(postTask.get());
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, e.getMessage());
        }

//        //图片
//        Page<Picture> picturePage = pictureDataSource.doSearch(searchText, current, pageSize);
//        searchVo.setPicturePage(picturePage);
//
//        //用户
//        Page<UserVo> userVoPage = userDataSource.doSearch(searchText, current, pageSize);
//        searchVo.setUserVoPage(userVoPage);
//
//        //帖子
//        Page<PostVo> postVoPage = postDataSource.doSearch(searchText, current, pageSize);
//        searchVo.setPostVoPage(postVoPage);

        return searchVo;
    }
}
