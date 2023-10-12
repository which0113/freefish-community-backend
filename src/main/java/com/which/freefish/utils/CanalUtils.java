package com.which.freefish.utils;

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
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.net.InetSocketAddress;
import java.util.List;

@Component
@Slf4j
public class CanalUtils {

    @Resource
    private BmsPostMapper bmsPostMapper;

    @Resource
    private PostEsMapper postEsMapper;

    public void startCanal() {
        // 创建链接
        CanalConnector connector = CanalConnectors.newSingleConnector(new InetSocketAddress(AddressUtils.getHostIp(),
                11111), "example", "", "");
        int batchSize = 1000;
        int emptyCount = 0;
        try {
            connector.connect();
            connector.subscribe(".*\\..*");
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
            System.out.println(String.format("================&gt; binlog[%s:%s] , name[%s,%s] , eventType : %s",
                    entry.getHeader().getLogfileName(), entry.getHeader().getLogfileOffset(),
                    entry.getHeader().getSchemaName(), entry.getHeader().getTableName(),
                    eventType));

            // 根据数据库操作类型进行增删改
            for (CanalEntry.RowData rowData : rowChage.getRowDatasList()) {
                if (eventType == CanalEntry.EventType.DELETE) {
                    List<CanalEntry.Column> columns = rowData.getBeforeColumnsList();
                    printColumn(columns);

                    String id = columns.get(0).getValue();
                    postEsMapper.deleteById(id);
                } else if (eventType == CanalEntry.EventType.INSERT) {
                    savaData(rowData);
                } else {
                    printColumn(rowData.getBeforeColumnsList());
                    savaData(rowData);
                }
            }
        }
    }

    private void savaData(CanalEntry.RowData rowData) {
        List<CanalEntry.Column> columns = rowData.getAfterColumnsList();
        printColumn(columns);

        String id = columns.get(0).getValue();
        BmsPost bmsPost = bmsPostMapper.selectById(id);

        PostEsDTO postEsDTO = new PostEsDTO();
        BeanUtils.copyProperties(bmsPost, postEsDTO);
        postEsMapper.save(postEsDTO);
    }

    private void printColumn(List<CanalEntry.Column> columns) {
        for (CanalEntry.Column column : columns) {
            System.out.println(column.getName() + " : " + column.getValue() + "    update=" + column.getUpdated());
        }
    }

}