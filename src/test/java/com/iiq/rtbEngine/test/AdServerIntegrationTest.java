package com.iiq.rtbEngine.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.HashSet;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AdServerIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    public void testProfileAttributeRequest() {

      //collecting attributes
        String url = "http://localhost:" + port + "/attribute?act=0&pid=3&atid=20";
        String expectedResponse = "Saved"; // Assuming a successful response for profile attribute request
        String response = restTemplate.getForObject(url, String.class);
        assertEquals(expectedResponse, response);
        url = "http://localhost:" + port + "/attribute?act=0&pid=3&atid=21";
        response = restTemplate.getForObject(url, String.class);
        assertEquals(expectedResponse, response);
        url = "http://localhost:" + port + "/attribute?act=0&pid=3&atid=22";
        response = restTemplate.getForObject(url, String.class);
        assertEquals(expectedResponse, response);
        //running bid requests
        url = "http://localhost:" + port + "/bid?act=1&pid=3";

        response = restTemplate.getForObject(url, String.class);
        assertEquals( "103" , response);
        response = restTemplate.getForObject(url, String.class);
        assertEquals( "103" , response);
        response = restTemplate.getForObject(url, String.class);
        assertEquals( "102" , response);
        response = restTemplate.getForObject(url, String.class);
        assertEquals( "102" , response);
        response = restTemplate.getForObject(url, String.class);
        assertEquals( "101" , response);
        response = restTemplate.getForObject(url, String.class);
        assertEquals( "101" , response);
        response = restTemplate.getForObject(url, String.class);
        assertEquals( "101" , response);
        response = restTemplate.getForObject(url, String.class);
        assertEquals( "capped" , response);
        response = restTemplate.getForObject(url, String.class);
        assertEquals( "capped" , response);


        //Adding another campaign
        url = "http://localhost:" + port + "/attribute?act=0&pid=3&atid=25";
        response = restTemplate.getForObject(url, String.class);
        assertEquals( "Saved" , response);

        url = "http://localhost:" + port + "/bid?act=1&pid=3";
        response = restTemplate.getForObject(url, String.class);
        assertEquals( "capped" , response);

        url = "http://localhost:" + port + "/attribute?act=0&pid=3&atid=26";
        response = restTemplate.getForObject(url, String.class);
        assertEquals( "Saved" , response);

        url = "http://localhost:" + port + "/bid?act=1&pid=3";
        response = restTemplate.getForObject(url, String.class);
        assertEquals( "104" , response);
        response = restTemplate.getForObject(url, String.class);
        assertEquals( "104" , response);
        response = restTemplate.getForObject(url, String.class);
        assertEquals( "capped" , response);


        //Add another profile to have campaigns 101,102,103
        restTemplate.getForObject("http://localhost:" + port + "/attribute?act=0&pid=1&atid=20", String.class);
        restTemplate.getForObject("http://localhost:" + port + "/attribute?act=0&pid=1&atid=21", String.class);
        restTemplate.getForObject("http://localhost:" + port + "/attribute?act=0&pid=1&atid=22", String.class);

        url = "http://localhost:" + port + "/bid?act=1&pid=1";
        response = restTemplate.getForObject(url, String.class);
        assertEquals( "103" , response);
    }

    @Test
    public void multipleRequestParallel() throws ExecutionException, InterruptedException {

        restTemplate.getForObject("http://localhost:" + port + "/attribute?act=0&pid=5&atid=20", String.class);
        restTemplate.getForObject("http://localhost:" + port + "/attribute?act=0&pid=5&atid=21", String.class);
        restTemplate.getForObject("http://localhost:" + port + "/attribute?act=0&pid=5&atid=22", String.class);


        String url = "http://localhost:" + port + "/bid?act=1&pid=5";

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10); // Set the core pool size
        executor.setMaxPoolSize(20); // Set the max pool size
        executor.setQueueCapacity(25); // Set the queue capacity
        executor.initialize();

        HashSet<Future<String>> futures = new HashSet<>();
        for(int i = 0; i < 20 ; i++)
            futures.add(executor.submit(()->restTemplate.getForObject(url, String.class)));

        int countPosBid = 0;
        for(Future<String> f : futures)
            if(f.get().equals("101") || f.get().equals("102") || f.get().equals("103"))
                countPosBid++;

        assertEquals( 7 , countPosBid);
    }

}
