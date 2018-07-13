package org.liuyehcf.spring.cloud.feign.consumer;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author hechenfeng
 * @date 2018/7/13
 */
@FeignClient(value = "ZookeeperProvider")
public interface GreetService {
    @RequestMapping(value = "hi", method = RequestMethod.GET)
    String sayHi(@RequestParam("name") String name);
}
