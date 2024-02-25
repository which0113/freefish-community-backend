<p align="center">
    <img src=https://img.freefish.love/logo.png width=188/>
</p>
<h1 align="center">FreeFish-咸鱼社区</h1>
<p align="center"><strong>咸鱼社区是一个类似掘金的技术社区</strong></p>
<div align="center">
<a target="_blank" href="https://github.com/which0113/api-backend">
    <img alt="" src="https://github.com/which0113/api-backend/badge/star.svg?theme=gvp"/>
</a>
    <img alt="Maven" src="https://raster.shields.io/badge/Maven-3.8.1-red.svg"/>
<a target="_blank" href="https://www.oracle.com/technetwork/java/javase/downloads/index.html">
        <img alt="" src="https://img.shields.io/badge/JDK-1.8+-green.svg"/>
</a>
    <img alt="SpringBoot" src="https://raster.shields.io/badge/SpringBoot-2.7+-green.svg"/>
</div>

## 项目介绍

本项目类似一个简版的掘金这样的技术社区，实现了多个用户注册，登录，发帖，评论，关注，搜索等功能。

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

## 技术栈

- Spring Boot
- Spring MVC
- MySQL 数据库
- Spring Security （JWT 安全校验）
- Spring Boot Starter（SDK 开发）
- Swagger + Knife4j 接口文档
- ElasticSearch 全文搜索
- Redis 数据缓存
- Kibana + Canal （MySQL 和 ES 数据同步）
- MyBatis-Plus 及 MyBatis X 自动生成
- Hutool、Apache Common Utils、Gson 等工具库