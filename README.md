# Freefish 咸鱼社区

## 环境准备

Kibana可视化Dev Tools创建ES索引库 `bms_post`:
```
PUT bms_post
{
  "mappings": {
    "properties": {
      "title": {
        "type": "text",
        "analyzer": "ik_max_word",
        "search_analyzer": "ik_smart",
        "fields": {
          "keyword": {
            "type": "keyword",
            "ignore_above": 256
          }
        }
      },
      "content": {
        "type": "text",
        "analyzer": "ik_max_word",
        "search_analyzer": "ik_smart",
        "fields": {
          "keyword": {
            "type": "keyword",
            "ignore_above": 256
          }
        }
      }
    }
  }
}
```

### 在线体验

> http://freefish.love

### 项目主要业务及实现的功能

本项目类似一个简版的掘金这样的技术社区，实现了多个用户注册，登录，发帖，评论，关注，搜索等功能。

### 后端技术栈

Spring Boot

MySQL

Mybatis

MyBatis-Plus

Spring Security

JWT

Lombok

ElasticSearch

Kibana

Canal

### 致谢

本项目大量借鉴了[极光社区项目](https://github.com/haoyu21/aurora)
，在此感谢原作者的无私开源。