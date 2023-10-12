package com.which.freefish.model.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;

@Document(indexName = "bms_post")
@Data
public class PostEsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    private String id;

    private String title;

    private String content;
}
