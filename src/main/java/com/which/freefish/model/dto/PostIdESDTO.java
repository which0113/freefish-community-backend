package com.which.freefish.model.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.io.Serializable;

@Document(indexName = "bms_post")
@Data
public class PostIdESDTO implements Serializable {

    /**
     * id
     */
    @Id
    private String id;

    private static final long serialVersionUID = 1L;

}
