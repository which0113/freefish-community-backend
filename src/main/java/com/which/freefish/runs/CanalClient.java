package com.which.freefish.runs;

import com.alibaba.otter.canal.client.CanalConnector;
import com.alibaba.otter.canal.client.CanalConnectors;
import com.alibaba.otter.canal.common.utils.AddressUtils;
import com.alibaba.otter.canal.protocol.CanalEntry;
import com.alibaba.otter.canal.protocol.Message;
import com.which.freefish.mapper.BmsPostMapper;
import com.which.freefish.mapper.PostEsMapper;
import com.which.freefish.model.dto.PostEsDTO;
import com.which.freefish.model.entity.BmsPost;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.List;
import java.util.Set;

/**
 * 项目启动，执行canal客户端监听
 */
@Component
@Slf4j
public class CanalClient implements CommandLineRunner {

    @Resource
    private BmsPostMapper bmsPostMapper;

    @Resource
    private PostEsMapper postEsMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void run(String... args) {
        // 创建链接
        InetSocketAddress inetSocketAddress = new InetSocketAddress(AddressUtils.getHostIp(), 11111);
        if (!portIsOpen(inetSocketAddress)) {
            return;
        }
        CanalConnector connector = CanalConnectors.newSingleConnector(inetSocketAddress, "example", "", "");

        int batchSize = 1000;
        int emptyCount = 0;
        try {
            connector.connect();
            // 过滤设置，只同步 freefish 数据库的 bms_post 表
            connector.subscribe("freefish.bms_post");
            connector.rollback();
            while (true) {
                // 获取指定数量的数据
                Message message = connector.getWithoutAck(batchSize);
                long batchId = message.getId();
                int size = message.getEntries().size();
                if (batchId == -1 || size == 0) {
                    emptyCount++;
                    System.out.println("empty count : " + emptyCount);
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                } else {
                    emptyCount = 0;
                    // 清空redis缓存
                    Set<String> keys = redisTemplate.keys("*");
                    if (keys != null) {
                        redisTemplate.delete(keys);
                        log.info("已清空缓存");
                    }
                    printEntry(message.getEntries());
                }
                // 提交确认
                connector.ack(batchId);
                // 处理失败, 回滚数据
                // connector.rollback(batchId);
            }
        } finally {
            connector.disconnect();
        }
    }

    private boolean portIsOpen(InetSocketAddress address) {
        try (Socket socket = new Socket()) {
            socket.connect(address, 1000);
            log.info("canal success : 连接成功！");
            return true;
        } catch (Exception e) {
            log.info("canal error : 端口关闭或连接超时！");
            return false;
        }
    }

    private void printEntry(List<CanalEntry.Entry> entrys) {
        for (CanalEntry.Entry entry : entrys) {
            if (entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONBEGIN || entry.getEntryType() == CanalEntry.EntryType.TRANSACTIONEND) {
                continue;
            }

            CanalEntry.RowChange rowChage = null;
            try {
                rowChage = CanalEntry.RowChange.parseFrom(entry.getStoreValue());
            } catch (Exception e) {
                throw new RuntimeException("ERROR ## parser of eromanga-event has an error , data:" + entry.toString(),
                        e);
            }

            CanalEntry.EventType eventType = rowChage.getEventType();
            System.out.println(String.format("================> binlog[%s:%s] , name[%s,%s] , eventType : %s",
                    entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                    entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                    eventType));

            // 根据数据库操作类型进行增删改
            for (CanalEntry.RowData rowData : rowChage.getRowDatasList()) {
                if (eventType == CanalEntry.EventType.DELETE) {
                    List<CanalEntry.Column> columns = rowData.getBeforeColumnsList();
//                    printColumn(columns);
                    String id = columns.get(0).getValue();
                    postEsMapper.deleteById(id);

                    log.info("存在更新，操作为删除数据");
                } else if (eventType == CanalEntry.EventType.INSERT) {
                    List<CanalEntry.Column> columns = rowData.getAfterColumnsList();
//                    printColumn(columns);
                    String id = columns.get(0).getValue();
                    savePostEs(id);

                    log.info("存在更新，操作为新增数据");
                } else {
//                    printColumn(rowData.getBeforeColumnsList());

                    List<CanalEntry.Column> columns = rowData.getAfterColumnsList();
                    // 如果存在至少一个元素的 updated 属性为 true => hasUpdate = ture;
                    boolean hasUpdate = columns.stream().anyMatch(CanalEntry.Column::getUpdated);
                    // 不存在更新，即 hasUpdate = false，return
                    if (!hasUpdate) {
                        log.info("不存在更新，操作为查询数据");
                        return;
                    }
//                    printColumn(columns);

                    String id = columns.get(0).getValue();
                    savePostEs(id);

                    log.info("存在更新，操作为修改数据");
                }
            }
        }
    }

    private void savePostEs(String id) {
        BmsPost bmsPost = bmsPostMapper.selectById(id);
        PostEsDTO postEsDTO = new PostEsDTO();
        if (bmsPost != null) {
            BeanUtils.copyProperties(bmsPost, postEsDTO);
        }
        postEsMapper.save(postEsDTO);
    }

    private void printColumn(List<CanalEntry.Column> columns) {
        for (CanalEntry.Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
        }
    }

}