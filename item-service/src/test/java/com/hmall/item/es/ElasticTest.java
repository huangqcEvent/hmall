package com.hmall.item.es;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.hmall.common.utils.CollUtils;
import com.hmall.item.domain.dto.ItemDoc;
import com.hmall.item.domain.po.Item;
import com.hmall.item.service.IItemService;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.client.indices.GetIndexResponse;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;
@SpringBootTest(properties = {"spring.profiles.active=local"})
@Slf4j
public class ElasticTest {

    @Resource
    private IItemService itemService;
    private RestHighLevelClient client;
    private static final String MAPPING_TEMPLATE ="{\n" +
            "  \"mappings\": {\n" +
            "    \"properties\": {\n" +
            "      \"id\": {\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"name\":{\n" +
            "        \"type\": \"text\",\n" +
            "        \"analyzer\": \"ik_max_word\"\n" +
            "      },\n" +
            "      \"price\":{\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"image\":{\n" +
            "        \"type\": \"keyword\",\n" +
            "        \"index\": false\n" +
            "      },\n" +
            "      \"category\":{\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"brand\":{\n" +
            "        \"type\": \"keyword\"\n" +
            "      },\n" +
            "      \"sold\":{\n" +
            "        \"type\": \"integer\"\n" +
            "      },\n" +
            "      \"commentCount\":{\n" +
            "        \"type\": \"integer\",\n" +
            "        \"index\": false\n" +
            "      },\n" +
            "      \"isAD\":{\n" +
            "        \"type\": \"boolean\"\n" +
            "      },\n" +
            "      \"updateTime\":{\n" +
            "        \"type\": \"date\"\n" +
            "      }\n" +
            "    }\n" +
            "  }\n" +
            "}";
    @BeforeEach
    void setUp() {
        this.client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.31.128:9200")
        ));
        System.out.println("成功连接");
    }

    @Test
    void testConnect() {
        System.out.println(client);
    }

    @AfterEach
    void tearDown() throws IOException {
        this.client.close();
    }

    //创建索引库
    @Test
    void testCreatIndex() throws IOException {
        //创建请求对象
        CreateIndexRequest request = new CreateIndexRequest("items");
        //构建请求体
        request.source(MAPPING_TEMPLATE, XContentType.JSON);
        //发送请求
        client.indices().create(request, RequestOptions.DEFAULT);
    }

    //删除索引库
    @Test
    void testDeleteIndex() throws IOException{
        //创建请求对象
        DeleteIndexRequest request = new DeleteIndexRequest("items");
        client.indices().delete(request, RequestOptions.DEFAULT);
    }

    //判断索引库是否存在
    @Test
    void testExistsIndex() throws IOException{
        //创建请求对象
        GetIndexRequest request = new GetIndexRequest("items");
        boolean exists = client.indices().exists(request, RequestOptions.DEFAULT);
        System.out.println("索引库存吗"+exists);
    }

    //获取索引库
    @Test
    void testGetIndex() throws IOException {
        GetIndexRequest request = new GetIndexRequest("items");
        GetIndexResponse getIndexResponse = client.indices().get(request, RequestOptions.DEFAULT);
        Map<String, MappingMetadata> mappings = getIndexResponse.getMappings();
        String s = JSONUtil.toJsonStr(mappings);
        System.out.println("获取到的索引库是"+s);
    }

    //新增文档
    @Test
    void testAddDocument() throws IOException {
        // 1.根据id查询商品数据
        Item item = itemService.getById(100002644680L);
        if (item != null) {
            // 2.转换为文档类型
            ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
            // 3.将ItemDTO转json
            String doc = JSONUtil.toJsonStr(itemDoc);

            // 1.准备Request对象
            IndexRequest request = new IndexRequest("items").id(itemDoc.getId());
            // 2.准备Json文档
            request.source(doc, XContentType.JSON);
            // 3.发送请求
            client.index(request, RequestOptions.DEFAULT);
        }
    }

    //查询文档
    @Test
    void testGetDocument() throws IOException {
        //1.准备request对象
        GetRequest request = new GetRequest("items").id("100002644680");
        //2.发送请求
        GetResponse response = client.get(request, RequestOptions.DEFAULT);
        String index = response.getSourceAsString();
        ItemDoc itemDoc = JSONUtil.toBean(index, ItemDoc.class);
        System.out.println("itemDoc:"+itemDoc);
    }

    //删除文档
    @Test
    void testDeleteDocument() throws IOException {
        //1.准备request对象
        DeleteRequest request = new DeleteRequest("items").id("100002644680");
        //2.发送请求
        client.delete(request, RequestOptions.DEFAULT);
    }

    //修改文档
    @Test
    void testUpdateDocument(){
        //1.准备request对象
        UpdateRequest request = new UpdateRequest("items","100002644680");
        //2.准备请求体
        request.doc(
                "name","黑马手机"
        );
        //3.发送请求
        try {
            client.update(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Test
    void testLoadItemDocs() throws IOException {
        // 分页查询商品数据
        int pageNo = 1;
        int size = 1000;
        while (true) {
            Page<Item> page = itemService.lambdaQuery().eq(Item::getStatus, 1).page(new Page<Item>(pageNo, size));
            // 非空校验
            List<Item> items = page.getRecords();
            if (CollUtils.isEmpty(items)) {
                return;
            }
            log.info("加载第{}页数据，共{}条", pageNo, items.size());
            // 1.创建Request
            BulkRequest request = new BulkRequest("items");
            // 2.准备参数，添加多个新增的Request
            for (Item item : items) {
                // 2.1.转换为文档类型ItemDTO
                ItemDoc itemDoc = BeanUtil.copyProperties(item, ItemDoc.class);
                // 2.2.创建新增文档的Request对象
                request.add(new IndexRequest()
                        .id(itemDoc.getId())
                        .source(JSONUtil.toJsonStr(itemDoc), XContentType.JSON));
            }
            // 3.发送请求
            client.bulk(request, RequestOptions.DEFAULT);

            // 翻页
            pageNo++;
        }
    }
}