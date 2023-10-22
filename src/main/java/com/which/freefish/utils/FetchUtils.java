package com.which.freefish.utils;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.which.freefish.model.dto.CreateTopicDTO;
import com.which.freefish.model.entity.UmsUser;
import com.which.freefish.service.IBmsPostService;
import com.which.freefish.service.IUmsUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 获取帖子，每次启动 SpringBoot 项目会执行一次 run 方法
 */
//@Component
@Slf4j
public class FetchUtils implements CommandLineRunner {

    @Resource
    private IBmsPostService iBmsPostService;

    @Resource
    private IUmsUserService umsUserService;

    @Override
    public void run(String... args) {
        // 1.获取数据
        String json = "{\"current\": 1, \"pageSize\": 8, \"sortField\": \"createTime\", \"sortOrder\": \"descend\", \"category\": \"文章\",\"reviewStatus\": 1}";
        String url = "https://www.code-nav.cn/api/post/search/page/vo";
        String result = HttpRequest
                .post(url)
                .body(json)
                .execute()
                .body();
//        System.out.println(result);
        // 2.转为json数据
        Map<String, Object> map = JSONUtil.toBean(result, Map.class);
        JSONObject data = (JSONObject) map.get("data");
        JSONArray records = (JSONArray) data.get("records");
        List<CreateTopicDTO> postList = new ArrayList<>();
        for (Object record : records) {
            JSONObject tempRecord = (JSONObject) record;
            // todo 取值过程中，需要判空
            CreateTopicDTO post = new CreateTopicDTO();
            post.setTitle(tempRecord.getStr("title"));
            post.setContent(tempRecord.getStr("content"));
            JSONArray tags = (JSONArray) tempRecord.get("tags");
            List<String> tagList = tags
                    .toList(String.class)
                    .stream()
                    .map(tag -> tag.replace(" ", ""))
                    .collect(Collectors.toList());
            tagList.remove(0);
            post.setTags(tagList);
            postList.add(post);
        }

        // 3.数据存入数据库
        List<UmsUser> userList = umsUserService.list();
        for (CreateTopicDTO dto : postList) {
            int randomIndex = new Random().nextInt(userList.size());
            iBmsPostService.create(dto, userList.get(randomIndex));
        }
        log.info("获取帖子成功, 条数 = {}", postList.size());
    }
}
