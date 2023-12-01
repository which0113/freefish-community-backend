package com.which.freefish.mapper;

import com.which.freefish.model.dto.PostEsDTO;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

@Mapper
public interface PostEsMapper extends ElasticsearchRepository<PostEsDTO, String> {


}