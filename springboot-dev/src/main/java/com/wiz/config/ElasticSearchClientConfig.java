package com.wiz.config;

import org.apache.http.HttpHost;
import org.apache.http.protocol.HTTP;
import org.elasticsearch.action.*;
import org.elasticsearch.client.ElasticsearchClient;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.threadpool.ThreadPool;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.config.AbstractElasticsearchConfiguration;

/**
 * @Description:
 * @Create: 2022-04-07-22:35
 * @Author: Hey
 */
// @Configuration
// public class ElasticSearchClientConfig  {
//
//     @Bean
//     public RestClient restClient(){
//         RestClient client = RestClient.builder(
//                 new HttpHost("localhost", 9200)
//         ).build();
//         return client;
//     }
//
// }
